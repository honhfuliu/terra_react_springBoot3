package com.ziheng.framework.file;

import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.file.FileService;
import com.ziheng.common.file.FileType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileServiceImpl implements FileService {

    @Value("${file.upload-path:./upload}")
    private String uploadPath;

    /**
     * 图片类型允许上传的扩展名（jpg 兼容 jpeg，png）
     */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    /**
     * 上传文件
     * @param file 文件
     * @param fileType 文件类型
     * @return 文件相对路径（{directory}/{fileName}）
     */
    @Override
    public String upload(MultipartFile file, FileType fileType) {
        if (fileType == null) {
            throw new BusinessException("文件类型不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String extension = getExtension(file.getOriginalFilename()).toLowerCase();
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException("文件缺少扩展名，无法识别类型");
        }

        // 图片类型需校验扩展名，仅允许 jpg / png
        if (fileType == FileType.IMAGE
                && !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("图片仅支持 jpg、png 格式");
        }

        // 按类型分目录保存，文件名使用 UUID 防冲突
        String directory = fileType.getDirectory();
        String fileName = UUID.randomUUID() + "." + extension;

        Path targetDir = Paths.get(uploadPath, directory);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new BusinessException("创建文件存储目录失败");
        }

        Path targetFile = targetDir.resolve(fileName);
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }

        // 返回相对存储路径，供后续拼接访问地址使用
        return directory + "/" + fileName;
    }

    /**
     * 获取文件扩展名（不含点号），无扩展名返回空串
     */
    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
