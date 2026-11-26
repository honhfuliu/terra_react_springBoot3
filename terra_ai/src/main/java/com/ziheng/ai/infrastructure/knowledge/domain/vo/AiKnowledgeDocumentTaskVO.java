package com.ziheng.ai.infrastructure.knowledge.domain.vo;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentTask;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AI知识库文档任务VO
 */
@Data
public class AiKnowledgeDocumentTaskVO {

    private Long id;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    private Date startTime;

    private Date endTime;

    private Date createTime;



    public static List<AiKnowledgeDocumentTaskVO> from(List<AiKnowledgeDocumentTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        List<AiKnowledgeDocumentTaskVO> vos = new ArrayList<>();
        for (AiKnowledgeDocumentTask task : tasks) {
            AiKnowledgeDocumentTaskVO vo = new AiKnowledgeDocumentTaskVO();
            vo.setId(task.getId());
            vo.setTaskType(task.getTaskType());
            vo.setTaskStatus(task.getTaskStatus());
            vo.setRetryCount(task.getRetryCount());
            vo.setMaxRetryCount(task.getMaxRetryCount());
            vo.setErrorMessage(task.getErrorMessage());
            vo.setStartTime(task.getStartTime());
            vo.setEndTime(task.getEndTime());
            vo.setCreateTime(task.getCreateTime());
            vos.add(vo);
        }
        return vos;
    }


}