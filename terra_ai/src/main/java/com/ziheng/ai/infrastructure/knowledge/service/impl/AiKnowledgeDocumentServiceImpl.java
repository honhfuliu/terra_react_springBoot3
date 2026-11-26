package com.ziheng.ai.infrastructure.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledge;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocument;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentTask;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeDocumentQuery;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeDocumentUploadDTO;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.DoclingParseResult;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDocumentDetailVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDocumentVO;

import com.ziheng.ai.infrastructure.knowledge.enums.AiDocumentProcessStatus;
import com.ziheng.ai.infrastructure.knowledge.enums.DocumentTaskStatus;
import com.ziheng.ai.infrastructure.knowledge.mapper.AiKnowledgeMapper;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentCleanupTaskService;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentService;
import com.ziheng.ai.infrastructure.knowledge.mapper.AiKnowledgeDocumentMapper;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentTaskService;
import com.ziheng.ai.infrastructure.knowledge.service.impl.delete.AiKnowledgeDocumentDeleteTransactionServiceImpl;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.model.service.AiModelService;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentChunkService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.file.FileDirectory;
import com.ziheng.common.file.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
* @author Administrator
* @description 针对表【ai_knowledge_document(AI知识库文档表)】的数据库操作Service实现
* @createDate 2026-11-17 15:47:08
*/
@Service
public class AiKnowledgeDocumentServiceImpl extends ServiceImpl<AiKnowledgeDocumentMapper, AiKnowledgeDocument>
    implements AiKnowledgeDocumentService{
    private static final Logger log = LoggerFactory.getLogger(AiKnowledgeDocumentServiceImpl.class);
    private final AiKnowledgeMapper aiKnowledgeMapper;
    private final AiModelService aiModelService;
    private final FileService fileService;
    private final AiKnowledgeDocumentTaskService taskService;
    private final AiKnowledgeDocumentDeleteTransactionServiceImpl deleteTransactionService;
    private final AiDocumentChunkService aiDocumentChunkService;
    private final AiKnowledgeDocumentCleanupTaskService cleanupTaskService;



    public AiKnowledgeDocumentServiceImpl(AiKnowledgeMapper aiKnowledgeMapper, AiModelService aiModelService,
                                          FileService fileService, AiKnowledgeDocumentTaskService taskService,
                                          AiKnowledgeDocumentDeleteTransactionServiceImpl deleteTransactionService,
                                          AiDocumentChunkService aiDocumentChunkService,
                                          AiKnowledgeDocumentCleanupTaskService cleanupTaskService) {
        this.aiKnowledgeMapper = aiKnowledgeMapper;
        this.aiModelService = aiModelService;
        this.fileService = fileService;
        this.taskService = taskService;
        this.deleteTransactionService = deleteTransactionService;
        this.aiDocumentChunkService = aiDocumentChunkService;
        this.cleanupTaskService = cleanupTaskService;

    }

    /**
     * 允许的文件扩展名
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "md", "xlsx", "xls");

    /**
     * 允许的文件大小（字节）
     */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;

    /**
     * 分页查询文档列表
     */
    @Override
    public PageResult<AiKnowledgeDocumentVO> page(AiKnowledgeDocumentQuery query) {
        if (query.getKnowledgeId() == null) {
            throw new BusinessException("知识库ID不能为空");
        }
        Page<AiKnowledgeDocument> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiKnowledgeDocument> wrapper = new LambdaQueryWrapper<AiKnowledgeDocument>()
                .eq(AiKnowledgeDocument::getKnowledgeId, query.getKnowledgeId())
                .like(StringUtils.hasText(query.getFileName()), AiKnowledgeDocument::getFileName, query.getFileName())
                .orderByDesc(AiKnowledgeDocument::getUpdateTime)
                .orderByDesc(AiKnowledgeDocument::getId);

        Page<AiKnowledgeDocument> result = page(page, wrapper);
        List<AiKnowledgeDocumentVO> rows = new ArrayList<>(result.getRecords().size());
        for (AiKnowledgeDocument item : result.getRecords()) {
            AiKnowledgeDocumentVO vo = new AiKnowledgeDocumentVO();
            BeanUtils.copyProperties(item, vo);
            AiDocumentProcessStatus parseStatus = AiDocumentProcessStatus.fromCode(item.getParseStatus());
            vo.setParseStatusName(parseStatus == null ? null : parseStatus.getDescription());

            AiDocumentProcessStatus embeddingStatus = AiDocumentProcessStatus.fromCode(item.getEmbeddingStatus());
            vo.setEmbeddingStatusName(embeddingStatus == null ? null : embeddingStatus.getDescription());

            AiDocumentProcessStatus chunkStatus = AiDocumentProcessStatus.fromCode(item.getChunkStatus());
            vo.setChunkStatusName(chunkStatus == null ? null : chunkStatus.getDescription());

            rows.add(vo);
        }
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 根据ID查询文档详情
     */
    @Override
    public AiKnowledgeDocumentDetailVO detail(Long id) {
        AiKnowledgeDocument document = getById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        AiKnowledgeDocumentDetailVO vo = new AiKnowledgeDocumentDetailVO();
        BeanUtils.copyProperties(document, vo);

        AiDocumentProcessStatus parseStatus = AiDocumentProcessStatus.fromCode(document.getParseStatus());
        vo.setParseStatusName(parseStatus == null ? null : parseStatus.getDescription());

        AiDocumentProcessStatus embeddingStatus = AiDocumentProcessStatus.fromCode(document.getEmbeddingStatus());
        vo.setEmbeddingStatusName(embeddingStatus == null ? null : embeddingStatus.getDescription());

        AiDocumentProcessStatus chunkStatus = AiDocumentProcessStatus.fromCode(document.getChunkStatus());
        vo.setChunkStatusName(chunkStatus == null ? null : chunkStatus.getDescription());

        AiKnowledge knowledge = aiKnowledgeMapper.selectById(document.getKnowledgeId());
        if (knowledge != null) {
            vo.setKnowledgeName(knowledge.getName());
            vo.setEmbeddingModelId(knowledge.getEmbeddingModelId());
            if (StringUtils.hasText(knowledge.getEmbeddingModelName())) {
                vo.setEmbeddingModelName(knowledge.getEmbeddingModelName());
            } else if (knowledge.getEmbeddingModelId() != null) {
                AiModel model = aiModelService.getById(knowledge.getEmbeddingModelId());
                if (model != null) {
                    vo.setEmbeddingModelName(model.getModelName());
                }
            }
        }
        vo.setTasks(taskService.listByDocumentId(id));
        return vo;
    }

    /**
     * 删除知识库文档（数据库记录 + 本地文件 + 知识库文档计数）
     */
    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("文档ID不能为空");
        }
        // 执行文档删除事务
        deleteTransactionService.deleteDocumentTransaction(id);

        // 删除 chunk 和向量数据：第一阶段已提交，此处失败不向外抛出，记录到清理任务等待补偿
        try {
            aiDocumentChunkService.deleteByDocumentId(id);
        } catch (Exception e) {
            log.error("删除文档 chunk/embedding 失败，documentId={}，转入清理任务补偿", id, e);
        }
    }

    /**
     * 标记文档解析成功
     * @param documentId 文档ID
     * @param taskId 任务ID
     * @param result 解析结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markParseSuccess(Long documentId, Long taskId, DoclingParseResult result)  {
        AiKnowledgeDocument document = getById(documentId);
        Integer pageCount = result.getPageCount();
        document.setPageCount(pageCount);
        document.setParseStatus(AiDocumentProcessStatus.SUCCESS.getCode());
        document.setParsedMdPath(result.getMarkdownPath());
        document.setParsedJsonPath(result.getJsonPath());
        document.setProcessEndTime(new Date());

        if (baseMapper.updateById(document) != 1) {
            throw new IllegalStateException("更新文档解析状态失败，documentId=" + documentId);
        }

        // 更新任务表
        AiKnowledgeDocumentTask task = new AiKnowledgeDocumentTask();
        task.setId(taskId);
        task.setTaskStatus(DocumentTaskStatus.SUCCESS.getCode());
        task.setEndTime(new Date());
        task.setErrorMessage(null);

        if (!taskService.updateById(task)) {
            throw new IllegalStateException("更新解析任务状态失败，taskId=" + taskId);
        }
    }


    /**
     * 标记文档解析失败
     * @param documentId 文档ID
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markParseFailed(Long documentId, Long taskId, String errorMessage) {
        // 更新任务表
        DocumentTaskStatus taskStatus = taskService.markFailed(taskId, errorMessage);

        // 更新文档表
        AiKnowledgeDocument document = new AiKnowledgeDocument();
        document.setId(documentId);
        if (taskStatus == DocumentTaskStatus.RETRYING) {
            document.setParseStatus(AiDocumentProcessStatus.RETRYING.getCode());
        } else {
            document.setParseStatus(AiDocumentProcessStatus.FAILED.getCode());
            document.setProcessEndTime(new Date());
        }

        if (baseMapper.updateById(document) != 1) {
            throw new BusinessException("更新文档解析状态失败");
        }

    }

    /**
     * 标记文档分片处理中
     * @param documentId 文档ID
     * @param taskId 任务ID
     * @param externalTaskId 外部任务ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markChunkProcessing(Long documentId, Long taskId, String externalTaskId) {
        // 更新文档切片状态
        AiKnowledgeDocument document = new AiKnowledgeDocument();
        document.setId(documentId);
        document.setChunkStatus(AiDocumentProcessStatus.PROCESSING.getCode());

        if (baseMapper.updateById(document) != 1) {
            throw new BusinessException("更新文档切片状态失败");
        }
        // 更新任务表
        taskService.markProcessing(taskId, externalTaskId);
    }

    /**
     * 标记文档切片成功，并同步更新任务状态、创建向量化任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markChunkSuccess(Long documentId, Long taskId, int chunkCount) {
        // 根据文档ID查询文档信息
        AiKnowledgeDocument document = getById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        document.setChunkStatus(AiDocumentProcessStatus.SUCCESS.getCode());
        document.setEmbeddingStatus(AiDocumentProcessStatus.PROCESSING.getCode());
        document.setChunkCount(chunkCount);
        document.setProcessEndTime(new Date());

        if (baseMapper.updateById(document) != 1) {
            throw new BusinessException("更新文档切片状态失败");
        }
        // 更新知识库chunk数量
        aiKnowledgeMapper.incrementChunkCount(document.getKnowledgeId(), chunkCount);
        // 更新任务表
        AiKnowledgeDocumentTask task = new AiKnowledgeDocumentTask();
        task.setId(taskId);
        task.setTaskStatus(DocumentTaskStatus.SUCCESS.getCode());
        task.setEndTime(new Date());
        task.setErrorMessage(null);

        if (!taskService.updateById(task)) {
            throw new BusinessException("更新切片任务状态失败");
        }

        // 与文档/任务状态在同一事务内创建向量化任务，失败时一起回滚
        taskService.createEmbeddingTask(documentId);
    }

    /**
     * 标记文档向量化成功，并同步更新任务状态
     * @param documentId 文档ID
     * @param taskId 任务ID
     */
    @Override
    public void markEmbeddingSuccess(Long documentId, Long taskId) {
        AiKnowledgeDocument document = getById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        document.setEmbeddingStatus(AiDocumentProcessStatus.SUCCESS.getCode());
        document.setProcessEndTime(new Date());

        if (baseMapper.updateById(document) != 1) {
            throw new BusinessException("更新文档向量化状态失败");
        }
        // 更新任务表
        AiKnowledgeDocumentTask task = new AiKnowledgeDocumentTask();
        task.setId(taskId);
        task.setTaskStatus(DocumentTaskStatus.SUCCESS.getCode());
        task.setEndTime(new Date());
        task.setErrorMessage(null);

        if (!taskService.updateById(task)) {
            throw new BusinessException("更新向量化任务状态失败");
        }
    }
    /**
     * 标记文档向量化失败，并同步更新任务状态
     * @param documentId 文档ID
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    @Override
    public void markEmbeddingFailed(Long documentId, Long taskId, String errorMessage) {
        DocumentTaskStatus taskStatus = taskService.markFailed(taskId, errorMessage);

        AiKnowledgeDocument document = new AiKnowledgeDocument();
        document.setId(documentId);
        if (taskStatus == DocumentTaskStatus.RETRYING) {
            document.setEmbeddingStatus(AiDocumentProcessStatus.RETRYING.getCode());
        } else {
            document.setEmbeddingStatus(AiDocumentProcessStatus.FAILED.getCode());
            document.setProcessEndTime(new Date());
        }

        if (baseMapper.updateById(document) != 1) {
            throw new BusinessException("更新文档向量化状态失败");
        }
    }

    /**
     * 标记文档切片失败，并同步更新任务状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markChunkFailed(Long documentId, Long taskId, String errorMessage) {
        DocumentTaskStatus taskStatus = taskService.markFailed(taskId, errorMessage);

        AiKnowledgeDocument document = new AiKnowledgeDocument();
        document.setId(documentId);
        if (taskStatus == DocumentTaskStatus.RETRYING) {
            document.setChunkStatus(AiDocumentProcessStatus.RETRYING.getCode());
        } else {
            document.setChunkStatus(AiDocumentProcessStatus.FAILED.getCode());
            document.setProcessEndTime(new Date());
        }

        if (baseMapper.updateById(document) != 1) {
            throw new BusinessException("更新文档切片状态失败");
        }

    }

    @Override
    public void downloadFile(Long documentId, HttpServletResponse response) {
        AiKnowledgeDocument document = getById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        Resource resource = fileService.getResource(document.getFilePath());

        String contentType = StringUtils.hasText(document.getMimeType())
                ? document.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        response.setContentType(contentType);
        String fileName = StringUtils.hasText(document.getFileName())
                ? document.getFileName() : "document." + document.getFileExt();
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        try (InputStream in = resource.getInputStream(); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        } catch (IOException e) {
            log.error("下载知识库文档失败，documentId={}", documentId, e);
            throw new BusinessException("读取文档文件失败");
        }
    }

    @Override
    public void downloadMarkdown(Long documentId, HttpServletResponse response) {
        AiKnowledgeDocument document = getById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        if (!StringUtils.hasText(document.getParsedMdPath())) {
            throw new BusinessException("文档尚未解析完成，Markdown 文件不存在");
        }
        Resource resource = fileService.getResource(document.getParsedMdPath());

        response.setContentType("text/markdown;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "inline; filename=" + URLEncoder.encode(documentId + ".md", StandardCharsets.UTF_8));

        try (InputStream in = resource.getInputStream(); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        } catch (IOException e) {
            log.error("读取文档 Markdown 失败，documentId={}", documentId, e);
            throw new BusinessException("读取文档 Markdown 失败");
        }
    }

    /**
     * 重新解析文档
     * @param documentId 文档ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reparse(Long documentId) {
        // 1. SELECT FOR UPDATE 查询并锁定文档
        LambdaQueryWrapper<AiKnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDocument::getId, documentId);
        wrapper.last("FOR UPDATE");
        AiKnowledgeDocument document = baseMapper.selectOne(wrapper);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        // 2. 校验 PARSE、CHUNK、EMBEDDING 状态
        validateProcessStatus("文档解析", document.getParseStatus());
        validateProcessStatus("文档切分", document.getChunkStatus());
        validateProcessStatus("文档向量化", document.getEmbeddingStatus());

        // 3.效验task任务是否全部完结
        taskService.validateTaskFinished(documentId);
        // 3.1 删除任务
        taskService.deleteByDocumentId(documentId);

        // 4. 创建新文档记录
        AiKnowledgeDocument newDocument = AiKnowledgeDocument.copy(document);
        baseMapper.insert(newDocument);
        if (newDocument.getId() == null) {
            throw new BusinessException("文档重新执行失败");
        }

        // 7. 更新知识库 documentCount(无需处理)、chunkCount（减少指定数量）
        aiKnowledgeMapper.decrementChunkCount(document.getKnowledgeId(), document.getChunkCount() == null ? 0 : document.getChunkCount());

        // 8. 创建 FILE_DELETE 任务
        document.setFilePath(null);
        cleanupTaskService.createFileDeleteTask(document);
        // 9. 创建 CHUNK_EMBEDDING_DELETE 任务
        cleanupTaskService.createChunkEmbeddingDeleteTask(document.getKnowledgeId(), document.getId());
        // 10. 创建新解析任务
        taskService.createParseTask(newDocument.getId());
        // 11. 删除原文档记录
        baseMapper.deleteById(documentId);

        log.info("文档重新执行成功，documentId={}", documentId);
    }

    /**
     * 校验处理阶段是否已经结束
     */
    private void validateProcessStatus(String stageName, String status) {
        if (!AiDocumentProcessStatus.SUCCESS.name().equals(status)
                && !AiDocumentProcessStatus.PENDING.name().equals(status)
                && !AiDocumentProcessStatus.FAILED.name().equals(status)) {

            throw new BusinessException(stageName + "尚未完成，暂时不能删除文档");
        }
    }

    /**
     * 上传知识库文档
     *
     * @param dto 上传参数
     */
    @Override
    public void upload(AiKnowledgeDocumentUploadDTO dto) {
        // 参数校验
        validateUploadParams(dto);

        // 知识库校验
        AiKnowledge knowledge = validateKnowledge(dto.getKnowledgeId());

        // 文件扩展名校验
        String extension = validateFileExtension(dto.getFile());

        // 文件大小校验
        if (dto.getFile().getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过50MB");
        }
        // 计算 MD5
        String fileMd5 = calculateMd5(dto.getFile());
        // 重复文件校验
        validateDuplicate(dto.getKnowledgeId(), fileMd5);
        // 上传文件
        String directory = FileDirectory.KNOWLEDGE_DOCUMENT + "/" + dto.getKnowledgeId();
        String filePath = fileService.upload(dto.getFile(), directory);


        try {
            // 保存文档记录
            AiKnowledgeDocument document = buildDocument(dto, extension, fileMd5, filePath);
            // 1. 保存文档记录
            int rows = baseMapper.insert(document);
            if (rows != 1) {
                throw new BusinessException("文档记录保存失败");
            }
            // 2. 创建解析任务
            AiKnowledgeDocumentTask task = taskService.createParseTask(document.getId());

            // 3. 更新知识库文档数量
            int countRows = aiKnowledgeMapper.incrementDocumentCount(document.getKnowledgeId());
            if (countRows != 1) {
                throw new BusinessException("更新知识库文档数量失败");
            }
        } catch (RuntimeException e) {
            // 数据库操作失败，补偿删除已经上传的文件
            try {
                fileService.delete(filePath);
            } catch (Exception deleteException) {
                log.error("删除上传文件失败，filePath={}", filePath, deleteException);
            }
            throw e;
        }
    }


    /**
     * 构建文档记录
     * @param dto 上传参数
     * @param extension 文件扩展名
     * @param fileMd5 文件MD5
     * @param filePath 文件路径
     * @return 文档记录
     */
    private AiKnowledgeDocument buildDocument(AiKnowledgeDocumentUploadDTO dto, String extension, String fileMd5, String filePath) {
        MultipartFile file = dto.getFile();
        AiKnowledgeDocument document = new AiKnowledgeDocument();
        document.setKnowledgeId(dto.getKnowledgeId());
        document.setFileName(file.getOriginalFilename());
        document.setFileExt(extension);
        document.setFileSize(file.getSize());
        document.setFilePath(filePath);
        document.setFileMd5(fileMd5);
        document.setMimeType(file.getContentType());
        document.setPageCount(0);
        document.setChunkCount(0);
        document.setParseStatus(AiDocumentProcessStatus.PROCESSING.getCode());
        document.setChunkStatus(AiDocumentProcessStatus.PENDING.getCode());
        document.setEmbeddingStatus(AiDocumentProcessStatus.PENDING.getCode());

        return document;
    }

    /**
     * 校验知识库中是否存在相同内容的文档
     *
     * @param knowledgeId 知识库ID
     * @param fileMd5 文件MD5
     */
    private void validateDuplicate(Long knowledgeId, String fileMd5) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<AiKnowledgeDocument>()
                        .eq(AiKnowledgeDocument::getKnowledgeId, knowledgeId)
                        .eq(AiKnowledgeDocument::getFileMd5, fileMd5)
        );

        if (count > 0) {
            throw new BusinessException("该文件已存在于当前知识库中，请勿重复上传");
        }
    }


    /**
     * 计算文件 MD5
     *
     * @param file 上传文件
     * @return 文件 MD5 值（32位十六进制字符串）
     */
    private String calculateMd5(MultipartFile file) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    messageDigest.update(buffer, 0, length);
                }
            }
            byte[] digest = messageDigest.digest();
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new BusinessException("文件 MD5 计算失败");
        }
    }
    /**
     * 校验文件扩展名
     *
     * @param file 上传文件
     * @return 文件扩展名（小写）
     */
    private String validateFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BusinessException("文件格式不正确，无法识别扩展名");
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("暂不支持该文件格式，仅支持 PDF、DOC、DOCX、TXT、MD");
        }
        return extension;
    }
    /**
     * 校验文档上传参数
     */
    private void validateUploadParams(AiKnowledgeDocumentUploadDTO dto) {
        if (dto == null) {
            throw new BusinessException("上传参数不能为空");
        }

        if (dto.getKnowledgeId() == null || dto.getKnowledgeId() <= 0) {
            throw new BusinessException("知识库ID不合法");
        }

        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
    }


    /**
     * 校验知识库
     */
    private AiKnowledge validateKnowledge(Long knowledgeId) {
        AiKnowledge knowledge = aiKnowledgeMapper.selectById(knowledgeId);
        if (knowledge == null) {
            throw new BusinessException("知识库不存在");
        }
        if (!"1".equals(knowledge.getStatus())) {
            throw new BusinessException("知识库已停用，无法上传文档");
        }
        return knowledge;
    }
}




