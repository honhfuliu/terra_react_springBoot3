package com.ziheng.common.file;

import lombok.Getter;

@Getter
public enum FileType {
    /**
     * 图片
     */
    IMAGE("image"),

    /**
     * 普通文件
     */
    FILE("file");

    private final String directory;

    FileType(String directory) {
        this.directory = directory;
    }
}
