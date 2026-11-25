package com.ziheng.ai.infrastructure.knowledge.parser.docling.enums;

import lombok.Getter;

/**
 * 支持解析的文档格式
 */
@Getter
public enum DocumentFormat {
    PDF("pdf"),
    DOCX("docx"),
    MD("md"),
    XLSX("xlsx"),
    XLS("xls");


    private final String value;

    DocumentFormat(String value) {
        this.value = value;
    }

    public static DocumentFormat fromExtension(String value) {
        for (DocumentFormat format : values()) {
            if (format.getValue().equalsIgnoreCase(value)) {
                return format;
            }
        }
        return null;
    }


}
