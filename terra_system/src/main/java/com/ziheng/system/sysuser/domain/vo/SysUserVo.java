package com.ziheng.system.sysuser.domain.vo;

import com.ziheng.system.sysuser.domain.SysUser;
import lombok.Data;

import java.util.Date;
@Data
public class SysUserVo {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 状态：0禁用 1正常
     */
    private String status;
    /**
     * 创建时间（yyyy-MM-dd HH:mm:ss）
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;



    public static SysUserVo from(SysUser user) {
        SysUserVo vo = new SysUserVo();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setDeptId(user.getDeptId());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

}
