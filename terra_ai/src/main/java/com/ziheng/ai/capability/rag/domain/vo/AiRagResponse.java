package com.ziheng.ai.capability.rag.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiRagResponse {

    /**
     * AI 回答
     */
    private String answer;

    /**
     * 引用来源
     */
    private List<AiRagSource> sources;
}