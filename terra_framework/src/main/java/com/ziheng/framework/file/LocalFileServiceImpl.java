package com.ziheng.framework.file;

import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.file.FileService;
import com.ziheng.common.file.FileType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
     * 根据指定目录上传
     *
     * @param file 文件
     * @param directory 存储目录（相对于上传根目录）
     * @return 文件相对路径
     */
    @Override
    public String upload(MultipartFile file, String directory) {
        if (!StringUtils.hasText(directory)) {
            throw new BusinessException("文件存储目录不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String extension = getExtension(file.getOriginalFilename()).toLowerCase();

        if (!StringUtils.hasText(extension)) {
            throw new BusinessException("文件缺少扩展名，无法识别类型");
        }

        // 防止扩展名中出现路径字符
        if (!extension.matches("[a-z0-9]{1,20}")) {
            throw new BusinessException("文件扩展名不合法");
        }

        String fileName = UUID.randomUUID() + "." + extension;

        Path rootPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path targetDir = rootPath.resolve(directory).normalize();

        // 防止目录穿越
        if (!targetDir.startsWith(rootPath)) {
            throw new BusinessException("文件存储目录不合法");
        }

        try {
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(fileName);
            file.transferTo(targetFile);

            return rootPath.relativize(targetFile)
                    .toString()
                    .replace("\\", "/");

        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 删除文件
     *
     * @param relativePath 文件相对路径
     */
    @Override
    public void delete(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }
        Path rootPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(relativePath).normalize();
        // 防止删除上传目录以外的文件
        if (!targetPath.startsWith(rootPath) || targetPath.equals(rootPath)) {
            throw new BusinessException("文件路径不合法");
        }
        try {
            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
            }
        } catch (IOException e) {
            throw new BusinessException("文件删除失败");
        }
    }
    /**
     * 根据相对路径获取文件资源
     *
     * @param relativePath 文件相对路径
     * @return 文件资源
     */
    @Override
    public Resource getResource(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new BusinessException("文件路径不能为空");
        }

        // 上传根目录
        Path rootPath = Paths.get(uploadPath)
                .toAbsolutePath()
                .normalize();

        // 根据相对路径获取实际文件路径
        Path filePath = rootPath.resolve(relativePath).normalize();

        // 防止目录穿越
        if (!filePath.startsWith(rootPath)) {
            throw new BusinessException("文件路径不合法");
        }

        // 检查文件是否存在
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException("文件不存在");
        }

        return new FileSystemResource(filePath);
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
