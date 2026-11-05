package com.ziheng.common.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件
     * @param file 文件
     * @param fileType 文件类型
     * @return 文件路径
     */
    String upload(MultipartFile file, FileType fileType);

}
