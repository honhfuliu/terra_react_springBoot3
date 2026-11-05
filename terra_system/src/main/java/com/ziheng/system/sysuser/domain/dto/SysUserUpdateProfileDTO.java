package com.ziheng.system.sysuser.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人信息 DTO（个人中心）
 * 仅允许修改昵称、性别、个人备注三个字段
 */
@Data
public class SysUserUpdateProfileDTO {

    /**
     * 用户昵称
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    /**
     * 性别 0未知 1男 2女
     */
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^[012]$", message = "性别取值不合法（0未知 1男 2女）")
    private String sex;

    /**
     * 个人备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
