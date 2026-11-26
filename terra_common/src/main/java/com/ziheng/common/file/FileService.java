package com.ziheng.common.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件
     * @param file 文件
     * @param fileType 文件类型
     * @return 文件路径
     */
    String upload(MultipartFile file, FileType fileType);

    /**
     * 根据指定目录上传
     *
     * @param file 文件
     * @param directory 存储目录（相对于上传根目录）
     * @return 文件相对路径
     */
    String upload(MultipartFile file, String directory);

    /**
     * 删除文件
     *
     * @param relativePath 文件相对路径
     */
    void delete(String relativePath);

    /**
     * 根据相对路径获取文件资源
     *
     * @param relativePath 文件相对路径
     * @return 文件资源
     */
    Resource getResource(String relativePath);

    /**
     * 保存文本文件内容
     *
     * @param directory 存储目录（相对于上传根目录）
     * @param fileName 文件名（不含路径）
     * @param content 文本内容
     * @return 文件相对路径
     */
    String saveText(String directory, String fileName, String content);

    /**
     * 复制文件
     * String result = copy("2024/01/a.jpg", "2024/02", "b.jpg");
     * @param sourceRelativePath 源文件相对路径
     * @param targetDirectory 目标目录（相对于上传根目录）
     * @param targetFileName 目标文件名（不含路径）
     * @return 目标文件相对路径
     */
    String copy(String sourceRelativePath, String targetDirectory, String targetFileName);

}
