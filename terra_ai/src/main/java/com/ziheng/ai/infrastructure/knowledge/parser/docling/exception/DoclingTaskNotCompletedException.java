package com.ziheng.ai.infrastructure.knowledge.parser.docling.exception;

/**
 * Docling 解析任务尚未完成异常
 */
public class DoclingTaskNotCompletedException extends RuntimeException {

    public DoclingTaskNotCompletedException(String message) {
        super(message);
    }
}