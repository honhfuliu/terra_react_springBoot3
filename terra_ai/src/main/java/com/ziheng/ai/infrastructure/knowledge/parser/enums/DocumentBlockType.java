package com.ziheng.ai.infrastructure.knowledge.parser.enums;


/**
 * 文档内容块类型
 */
public enum DocumentBlockType {

    /**
     * 标题
     */
    HEADING,

    /**
     * 普通段落
     */
    PARAGRAPH,

    /**
     * 代码块
     */
    CODE,

    /**
     * 表格
     */
    TABLE,

    /**
     * 列表
     */
    LIST,

    /**
     * 引用
     */
    BLOCKQUOTE,

    /**
     * 其他内容
     */
    OTHER
}

