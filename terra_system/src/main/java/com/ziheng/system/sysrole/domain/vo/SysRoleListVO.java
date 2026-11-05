package com.ziheng.system.sysrole.domain.vo;

import com.ziheng.system.sysrole.domain.SysRole;
import lombok.Data;

import java.util.Date;

/**
 * 角色列表 VO
 */
@Data
public class SysRoleListVO {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识（权限字符）
     */
    private String roleKey;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态 0禁用 1正常
     */
    private String status;

    /**
     * 创建时间（yyyy-MM-dd HH:mm:ss）
     */

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;



    public static SysRoleListVO from(SysRole role) {
        SysRoleListVO vo = new SysRoleListVO();
        vo.setRoleId(role.getRoleId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleKey(role.getRoleKey());
        vo.setSort(role.getSort());
        vo.setStatus(role.getStatus());
        vo.setCreateTime(role.getCreateTime());
        return vo;
    }


}
