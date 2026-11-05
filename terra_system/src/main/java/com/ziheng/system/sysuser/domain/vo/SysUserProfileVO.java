package com.ziheng.system.sysuser.domain.vo;

import com.ziheng.system.sysuser.domain.SysUser;
import lombok.Data;

import java.util.Date;

/**
 * 用户资料详情（个人中心）。
 *
 * <p>在编辑回显（{@link SysUserEditVO}）之外，补充了头像、最近登录等展示字段。
 * 性别/状态返回的是原始字典值（0/1/2、0/1），前端自行映射中文。</p>
 */
@Data
public class SysUserProfileVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别：0未知 1男 2女
     */
    private String sex;

    /**
     * 账号状态：0禁用 1正常
     */
    private String status;

    /**
     * 个人备注
     */
    private String remark;

    /**
     * 用户头像地址。当前 sys_user 表还没有 avatar 列，先返回 null，
     * 等补上该字段后直接在此赋值即可。
     */
    private String avatar;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 最近登录信息拼接串：浏览器 · 操作系统 · IP · 时间 · 登录地点
     */
    private String lastLoginInfo;

    /**
     * 最近登录时间（取自最近一条成功登录记录）
     */
    private Date lastLoginTime;

    public static SysUserProfileVO from(SysUser user) {
        if (user == null) {
            return null;
        }
        SysUserProfileVO vo = new SysUserProfileVO();
        vo.setUserId(user.getUserId());
        vo.setAvatar(user.getAvatar());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setSex(user.getSex());
        vo.setStatus(user.getStatus());
        vo.setRemark(user.getRemark());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}
