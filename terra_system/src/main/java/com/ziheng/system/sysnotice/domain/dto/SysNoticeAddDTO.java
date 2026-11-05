package com.ziheng.system.sysnotice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公告新增/修改 DTO
 * 与前端表单保持一致（id 为空表示新增，不为空表示修改）
 */
@Data
public class SysNoticeAddDTO {

    /**
     * 公告ID，为空表示新增，不为空表示修改
     */
    private Long id;

    /**
     * 公告标题（必填）
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题长度不能超过200个字符")
    private String title;

    /**
     * 公告类型：NOTICE通知、ANNOUNCEMENT公告（必填）
     */
    @NotBlank(message = "公告类型不能为空")
    @Size(max = 32, message = "公告类型长度不能超过32个字符")
    private String noticeType;

    /**
     * 公告内容（必填）
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /**
     * 公告摘要
     */
    @Size(max = 500, message = "公告摘要长度不能超过500个字符")
    private String summary;

}
