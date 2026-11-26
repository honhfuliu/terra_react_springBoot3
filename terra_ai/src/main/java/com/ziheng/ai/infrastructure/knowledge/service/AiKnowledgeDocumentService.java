package com.ziheng.ai.infrastructure.knowledge.service;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocument;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeDocumentQuery;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeDocumentUploadDTO;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.DoclingParseResult;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDocumentDetailVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDocumentVO;
import com.ziheng.common.core.domain.PageResult;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
* @author Administrator
* @description 针对表【ai_knowledge_document(AI知识库文档表)】的数据库操作Service
* @createDate 2026-11-17 15:47:08
*/
public interface AiKnowledgeDocumentService extends IService<AiKnowledgeDocument> {
    /**
     * 上传知识库文档
     * @param dto 上传参数
     */
    void upload(AiKnowledgeDocumentUploadDTO dto);

    /**
     * 分页查询文档列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<AiKnowledgeDocumentVO> page(AiKnowledgeDocumentQuery query);

    /**
     * 根据ID查询文档详情
     *
     * @param id 文档ID
     * @return 文档详情
     */
    AiKnowledgeDocumentDetailVO detail(Long id);

    /**
     * 删除知识库文档（同时删除本地文件并更新知识库文档数量）
     *
     * @param id 文档ID
     */
    void delete(Long id);


    /**
     * 解析成功，更新文档和任务状态
     */
    void markParseSuccess(Long documentId, Long taskId, DoclingParseResult result) throws IOException;

    /**
     * 解析失败，更新文档和任务状态
     */
    void markParseFailed(Long documentId, Long taskId, String errorMessage);

    /**
     * 标记文档切片任务已提交
     *
     * @param documentId 文档ID
     * @param taskId 本地任务ID
     * @param externalTaskId Docling任务ID
     */
    void markChunkProcessing(Long documentId, Long taskId, String externalTaskId);

    /**
     * 切片成功，更新文档和任务状态，并创建向量化任务
     */
    void markChunkSuccess(Long documentId, Long taskId, int chunkCount);

    /**
     * 切片失败，更新文档和任务状态
     */
    void markChunkFailed(Long documentId, Long taskId, String errorMessage);

    /**
     * 向量化成功，更新文档和任务状态
     * @param documentId 文档ID
     * @param taskId 任务ID
     */
    void markEmbeddingSuccess(Long documentId, Long taskId);

    /**
     * 切片失败，更新文档和任务状态
     */
    void markEmbeddingFailed(Long documentId, Long taskId, String errorMessage);


    /**
     * 将知识库文档原始文件写入响应，供前端以 blob 下载
     *
     * @param documentId 文档ID
     * @param response   HTTP 响应
     */
    void downloadFile(Long documentId, HttpServletResponse response);

    /**
     * 将解析后的 Markdown 文件写入响应，供前端预览
     *
     * @param documentId 文档ID
     * @param response   HTTP 响应
     */
    void downloadMarkdown(Long documentId, HttpServletResponse response);

    /**
     * 重新解析文档
     * @param documentId 文档ID
     */
    void reparse(Long documentId);
}
