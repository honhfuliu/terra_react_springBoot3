package com.ziheng.ai.infrastructure.knowledge.parser.docling;

import com.fasterxml.jackson.databind.JsonNode;
import com.ziheng.ai.infrastructure.knowledge.parser.docling.enums.DocumentFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;

/**
 * DoclingClient
 */
@Component
public class DoclingClient {
    private static final Logger log = LoggerFactory.getLogger(DoclingClient.class);
    private final RestClient restClient;

    public DoclingClient(@Value("${terra.ai.docling.base-url}") String baseUrl) {
        // Docling(FastAPI) 无法解析 JDK HttpClient 的 chunked multipart，改用缓冲请求发送 Content-Length
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 提交 Docling HybridChunker 异步切片任务
     *
     * @param resource 已解析的 Docling JSON 文件
     * @param fileName 文件名称
     * @param chunkSize Chunk 大小
     * @return Docling 异步任务信息
     */
    public JsonNode submitChunkAsync(Resource resource, String fileName, Integer chunkSize) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        // 文件
        builder.part("files", resource)
                .filename(fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        // 基础参数
        builder.part("include_converted_doc", false);
        builder.part("target_type", "inbody");
        builder.part("convert_from_formats", new HttpEntity<>("json_docling"));

//        builder.part("convert_image_export_mode", "placeholder");  // 转换图片为占位符
//        builder.part("convert_do_ocr", true);
//        builder.part("convert_force_ocr", false);
//        builder.part("convert_ocr_preset", "auto");
//        builder.part("convert_pdf_backend", "docling_parse"); // 指定 PDF 底层解析引擎
//        builder.part("convert_table_mode", "accurate");
//        builder.part("convert_table_cell_matching", true);
//        builder.part("convert_pipeline", "standard");
//        builder.part("convert_do_table_structure", true);
//        builder.part("convert_do_pdf_heading_hierarchy", false);
//        builder.part("convert_pdf_heading_hierarchy_options", "{\"enabled\": false, \"use_bookmarks\": false, \"use_numbering\": true, \"use_style\": true, \"use_font_style\": true, \"style_size_tolerance\": 0.05, \"numbering_schemes\": null, \"max_level\": 4, \"bookmark_match_threshold\": 0.8}");
//        builder.part("convert_include_images", true);
//        builder.part("convert_include_page_images", false);
//        builder.part("convert_images_scale", 2);
//        builder.part("convert_md_page_break_placeholder", "<!-- page-break -->");
//        builder.part("convert_md_compact_tables", false);
//        builder.part("convert_table_structure_preset", "default");
//        builder.part("convert_layout_preset", "default");
        // Chunk 配置
        builder.part("chunking_use_markdown_tables", true);
        builder.part("chunking_use_markdown_images", false);
        builder.part("chunking_image_placeholder", "![IMAGE]");
        builder.part("chunking_include_raw_text", true);
        builder.part("chunking_merge_peers", true);
        builder.part("chunking_overlap", 128); // Chunk 重叠长度测试

        // 动态配置，来自知识库
        builder.part("chunking_max_tokens", chunkSize);
        // builder.part("chunking_tokenizer", "Qwen/Qwen3-Embedding-0.6B");
        MultiValueMap<String, HttpEntity<?>> parts = builder.build();
        parts.add("convert_ocr_lang", new HttpEntity<>("ch"));
        parts.add("convert_ocr_lang", new HttpEntity<>("en"));


        // 提交异步任务
        return restClient.post()
                .uri("/v1/chunk/hybrid/file/async")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .body(JsonNode.class);

    }


    public JsonNode submitAsync(Resource resource, String fileName, DocumentFormat format) throws IOException {
        log.info("Docling from_formats: {}", format.getValue());


        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // 文件
        builder.part("files", resource)
                .filename(fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM);
        // 基础参数
        builder.part("target_type", "inbody");
        // 输入格式
        builder.part("from_formats", format.getValue());
        // 图片处理
        builder.part("image_export_mode", "placeholder");
        // OCR
        builder.part("do_ocr", true);
        builder.part("force_ocr", false);
        // 表格处理
        builder.part("do_table_structure", true);
        builder.part("table_mode", "accurate");
        builder.part("table_cell_matching", true);
        // PDF 标题层级
        builder.part("do_pdf_heading_hierarchy", true);
        // 图片输出
        builder.part("include_images", true);
        builder.part("include_page_images", false);
        // Markdown 输出
        builder.part("md_page_break_placeholder", "<!-- page-break -->");
        builder.part("md_compact_tables", false);

        MultiValueMap<String, HttpEntity<?>> parts = builder.build();
        // 输出格式
        parts.add("to_formats", new HttpEntity<>("md"));
        parts.add("to_formats", new HttpEntity<>("json"));

        log.info("Docling parts: {}", parts);
        // 提交异步任务
        return restClient.post()
                .uri("/v1/convert/file/async")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * 查询 Docling 任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    public JsonNode getTaskStatus(String taskId) {
        return restClient.get()
                .uri("/v1/status/poll/{taskId}", taskId)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * 查询 Docling 异步任务结果
     * @param taskId 任务ID
     * @return 任务结果
     */
    public JsonNode getResult(String taskId) {
        return restClient.get()
                .uri("/v1/result/{taskId}", taskId)
                .retrieve()
                .body(JsonNode.class);
    }






}
