package com.ziheng.ai.infrastructure.knowledge.parser.docling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.DoclingChunk;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.DoclingParseResult;
import com.ziheng.ai.infrastructure.knowledge.parser.docling.enums.DocumentFormat;
import com.ziheng.ai.infrastructure.knowledge.parser.docling.enums.TaskStatus;
import com.ziheng.ai.infrastructure.knowledge.parser.docling.enums.TaskType;
import com.ziheng.ai.infrastructure.knowledge.parser.docling.exception.DoclingTaskNotCompletedException;
import com.ziheng.common.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoclingResultService {
    private static final Logger log = LoggerFactory.getLogger(DoclingResultService.class);

    private final DoclingClient doclingClient;

    private final DoclingMarkdownStorage markdownStorage;

    private final ObjectMapper objectMapper;

    private final FileService fileService;

    public DoclingResultService(DoclingClient doclingClient, DoclingMarkdownStorage markdownStorage, ObjectMapper objectMapper, FileService fileService) {
        this.doclingClient = doclingClient;
        this.markdownStorage = markdownStorage;
        this.objectMapper = objectMapper;
        this.fileService = fileService;
    }

    /**
     * 提交分块文件解析任务
      * @param filePath 文件路径
      * @param chunkSize 分块大小
      * @param documentId 文档ID
     * @return Docling 任务 ID
     * @throws IOException IO 异常
     */
    public String submitChunkTask(String filePath, Integer chunkSize,Long documentId) throws IOException {
        Resource resource = markdownStorage.getResource(filePath);
        JsonNode response = doclingClient.submitChunkAsync(resource, resource.getFilename(), chunkSize == null ? 1024 : chunkSize);
        String taskId = response.path("task_id").asText();
        if (taskId.isBlank()) {
            throw new IllegalStateException("Docling 未返回有效的 task_id");
        }
        log.info("Docling 文件分块解析任务提交成功，documentId={}, taskId={}", documentId, taskId);
        return taskId;
    }

    /**
     * 提交文件解析任务
     */
    public String submitParseTask(String filePath, String fileName, Long documentId, DocumentFormat format) throws IOException {
        // 从磁盘获取文件
        Resource resource = fileService.getResource(filePath);
        // 调用 Docling 异步解析接口
        JsonNode response = doclingClient.submitAsync(resource, fileName, format);
        // 获取 Docling 任务 ID
        String taskId = response.path("task_id").asText();
        if (taskId.isBlank()) {
            throw new IllegalStateException("Docling 未返回有效的 task_id");
        }
        log.info("Docling 文件解析任务提交成功，documentId={}, taskId={}", documentId, taskId);
        return taskId;
    }

    /**
     * 检查 Docling 任务状态和任务类型
     *
     * @param taskId       Docling 任务 ID
     * @param expectedType 预期任务类型
     */
    private JsonNode checkTaskStatus(String taskId, TaskType expectedType) {

        JsonNode taskStatusResult = doclingClient.getTaskStatus(taskId);

        // 获取实际任务类型
        TaskType taskType = TaskType.fromValue(taskStatusResult.path("task_type").asText());

        // 判断任务类型
        if (taskType != expectedType) {
            throw new IllegalStateException("当前任务类型不正确，预期：" + expectedType.getValue() + "，实际：" + taskType.getValue());
        }

        // 获取任务状态
        TaskStatus taskStatus = TaskStatus.fromValue(taskStatusResult.path("task_status").asText());

        // 判断任务状态
        switch (taskStatus) {
            case PENDING, STARTED -> throw new DoclingTaskNotCompletedException("Docling 任务尚未完成");
            case FAILURE -> throw new IllegalStateException("Docling 任务执行失败：" + taskStatusResult.path("error_message").asText());
            case SKIPPED -> throw new IllegalStateException("Docling 任务已跳过");
            case PARTIAL_SUCCESS -> throw new IllegalStateException("Docling 任务部分成功，暂不自动处理");
            case SUCCESS -> {}// 任务成功，继续处理
            default -> throw new IllegalStateException("Docling 任务状态未知：" + taskStatus.getValue());
        }

        return taskStatusResult;
    }

    /**
     * 查询解析结果并保存 Markdown
     */
    public DoclingParseResult fetchAndSaveMarkdownAndJson(String taskId, Long documentId) throws IOException {
        checkTaskStatus(taskId, TaskType.CONVERT);
        // 获取转换结果
        JsonNode result = doclingClient.getResult(taskId);
        // 统一校验结果状态
        checkResultStatus(result);
        JsonNode mdNode = result.path("document").path("md_content");
        JsonNode jsonNode = result.path("document").path("json_content");
        JsonNode filenameNode = result.path("document").path("filename");
        JsonNode statusNode = result.path("status");
        log.info("filenameNode：{}", filenameNode);
        log.info("statusNode：{}", statusNode);
        if (mdNode.isMissingNode() || mdNode.isNull()) {
            throw new IllegalStateException("Docling 返回结果中不存在 document.md_content");
        }
        String mdContent = mdNode.asText();
        if (mdContent.isBlank()) {
            throw new IllegalStateException("Docling Markdown 内容为空");
        }
        // 检查 JSON
        if (jsonNode.isMissingNode() || jsonNode.isNull()) {
            throw new IllegalStateException("Docling 返回结果中不存在 document.json_content");
        }

        String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(jsonNode);
        // 保存 Markdown 文件
        String mdPath = markdownStorage.saveMarkdown(documentId, mdContent);
        String jsonPath = markdownStorage.saveJson(documentId, jsonContent);

        log.info("Markdown 文件保存路径：{}", mdPath);
        log.info("JSON 文件保存路径：{}", jsonPath);
        return new DoclingParseResult(mdPath, jsonPath);
    }


    /**
     * 查询 Docling 切分结果
     *
     * @param taskId Docling 外部任务 ID
     * @return 切分结果
     */
    public List<DoclingChunk> fetchChunkResult(String taskId) {
        // 查询任务状态
        checkTaskStatus(taskId, TaskType.CHUNK);
        // 获取任务结果
        JsonNode result = doclingClient.getResult(taskId);
        // 统一校验结果状态
        // checkResultStatus(result);
        // 提取 chunks
        JsonNode chunksNode = result.path("chunks");

        if (!chunksNode.isArray()) {
            throw new IllegalStateException("Docling 切分结果格式异常，chunks 不存在");
        }
        // 4. 转换为 DTO
        return objectMapper.convertValue(
                chunksNode,
                new TypeReference<>() {}
        );
    }

    /**
     * 校验 Docling 任务结果状态
     *
     * @param result Docling 返回结果
     */
    private void checkResultStatus(JsonNode result) {

        String status = result.path("status").asText("");

        if (TaskStatus.SUCCESS.getValue().equalsIgnoreCase(status)) {
            return;
        }

        // 提取错误信息
        String errorMessage = result.path("errors")
                .findValuesAsText("error_message")
                .stream()
                .filter(message -> message != null && !message.isBlank())
                .collect(Collectors.joining("; "));

        if (errorMessage.isBlank()) {
            errorMessage = "Docling 任务处理未成功，状态：" + status;
        }

        // 仍在处理中
        if (TaskStatus.PENDING.getValue().equalsIgnoreCase(status) || TaskStatus.STARTED.getValue().equalsIgnoreCase(status)) {
            throw new DoclingTaskNotCompletedException(errorMessage);
        }

        // failure、partial_success、skipped、未知状态
        throw new IllegalStateException("Docling 任务处理失败，状态：" + status + "，原因：" + errorMessage);
    }

}
