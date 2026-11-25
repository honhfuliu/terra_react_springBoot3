package com.ziheng.ai.infrastructure.knowledge.parser.docling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziheng.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文档存储服务
 */

@Component
public class DoclingMarkdownStorage {
    private static final Logger log = LoggerFactory.getLogger(DoclingMarkdownStorage.class);

    @Value("${terra.ai.docling.storage-path}")
    private String storagePath;

    private final ObjectMapper objectMapper;

    public DoclingMarkdownStorage(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    /**
     * 保存 Markdown 文件
     *
     * @param documentId 文档ID
     * @param mdContent  Markdown 内容
     * @return 相对存储根目录的文件路径
     */
    public String saveMarkdown(Long documentId, String mdContent)
            throws IOException {
        return saveTextFile(documentId, "md", mdContent);
    }

    /**
     * 保存 JSON 文件
     *
     * @param documentId 文档ID
     * @param jsonContent JSON 内容
     * @return 相对存储根目录的文件路径
     */
    public String saveJson(Long documentId, String jsonContent)
            throws IOException {
        return saveTextFile(documentId, "json", jsonContent);
    }

    /**
     * 保存文本文件
     */
    private String saveTextFile(Long documentId, String extension, String content) throws IOException {
        // 解析存储根目录
        Path directory = Paths.get(storagePath)
                .toAbsolutePath()
                .normalize();
        // 创建目录
        Files.createDirectories(directory);
        // 生成文件名
        String fileName = documentId + "." + extension;
        // 实际磁盘路径
        Path filePath = directory.resolve(fileName).normalize();

        // 写入文件
        Files.writeString(
                filePath,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        // 返回相对路径
        return fileName;
    }

    /**
     * 根据相对路径获取解析文件资源
     *
     * @param relativePath 文件相对路径
     * @return 文件资源
     */
    public Resource getResource(String relativePath) {

        Path rootPath = Paths.get(storagePath)
                .toAbsolutePath()
                .normalize();

        Path filePath = rootPath.resolve(relativePath).normalize();

        // 防止路径穿越
        if (!filePath.startsWith(rootPath)) {
            throw new IllegalArgumentException("非法的文件路径");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("解析文件不存在：" + relativePath);
        }

        return new FileSystemResource(filePath);
    }

    /**
     * 根据相对路径删除解析文件（文件不存在时视为删除成功）
     *
     * @param relativePath 文件相对存储根目录的路径
     */
    public void delete(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }
        Path rootPath = Paths.get(storagePath)
                .toAbsolutePath()
                .normalize();
        Path targetPath = rootPath.resolve(relativePath).normalize();

        // 防止删除存储目录以外的文件
        if (!targetPath.startsWith(rootPath) || targetPath.equals(rootPath)) {
            throw new BusinessException("解析文件路径不合法：" + relativePath);
        }
        try {
            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
            }
        } catch (IOException e) {
            log.error("删除解析文件失败，relativePath={}", relativePath, e);
            throw new BusinessException("解析文件删除失败：" + relativePath);
        }
    }

    /**
     * 获取 Docling 文档页数
     *
     * @param jsonPath JSON 文件相对存储根目录的路径
     */
    public Integer getPageCount(String jsonPath) {
        try {
            Resource resource = getResource(jsonPath);
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            JsonNode pages = root.path("pages");
            return pages.isObject() ? pages.size() : null;
        } catch (Exception e) {
            log.warn("获取 Docling 文档页数失败，jsonPath={}", jsonPath, e);
            return null;
        }
    }
}
