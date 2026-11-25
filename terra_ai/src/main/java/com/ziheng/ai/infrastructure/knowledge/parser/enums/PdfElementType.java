package com.ziheng.ai.infrastructure.knowledge.parser.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OpenDataLoader PDF 2.5.20 官方文档中定义的元素类型
 */
@Getter
@RequiredArgsConstructor
public enum PdfElementType {

    // 顶层内容类型
    PARAGRAPH("paragraph"),
    HEADING("heading"),
    CAPTION("caption"),
    TABLE("table"),
    TEXT_BLOCK("text block"),
    LIST("list"),
    IMAGE("image"),
    HEADER("header"),
    FOOTER("footer"),

    // 嵌套结构类型
    TABLE_ROW("table row"),
    TABLE_CELL("table cell"),
    LIST_ITEM("list item");

    private final String value;

    /**
     * 根据 JSON 中的 type 获取枚举
     */
    public static PdfElementType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (PdfElementType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }

        return null;
    }
}