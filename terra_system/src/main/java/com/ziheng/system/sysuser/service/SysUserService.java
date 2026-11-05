package com.ziheng.system.sysuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.system.sysuser.domain.SysUser;
import com.ziheng.system.sysuser.domain.dto.SysUserAddDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserQuery;
import com.ziheng.system.sysuser.domain.dto.SysUserResetPasswordDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserUpdateProfileDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserChangePasswordDTO;
import com.ziheng.system.sysuser.domain.vo.SysUserEditVO;
import com.ziheng.system.sysuser.domain.vo.SysUserProfileVO;
import com.ziheng.system.sysuser.domain.vo.SysUserVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


/**
* @author Administrator
* @description 针对表【sys_user(系统用户表)】的数据库操作Service
* @createDate 2026-06-12 17:10:57
*/
public interface SysUserService extends IService<SysUser> {
    /**
     * 分页查询用户列表
     * @param query
     * @return
     */
    PageResult<SysUserVo> listUsers(SysUserQuery query);

    /**
     * 新增或修改用户（userId 为空表示新增，不为空表示修改）
     * @param dto 用户信息
     * @return 用户ID
     */
    Long addUser(SysUserAddDTO dto);

    /**
     * 查询用户详情（编辑回显，含角色ID集合）
     * @param userId 用户ID
     * @return 用户详情
     */
    SysUserEditVO getUserDetail(Long userId);

    /**
     * 查询用户资料详情（个人中心）：基础资料 + 最近一次成功登录信息
     * @param userId 用户ID
     * @return 用户资料
     */
    SysUserProfileVO getUserProfile(Long userId);


    /**
     * 删除用户（支持单条或批量），逻辑删除并清理用户角色关联
     * @param userIds 用户ID集合
     */
    void deleteUsers(List<Long> userIds);

    /**
     * 上传并更新当前登录用户头像
     * @param file 头像文件（仅允许 jpg、png）
     * @return 头像存储相对路径
     */
    String updateAvatar(MultipartFile file);

    /**
     * 修改当前登录用户个人信息（仅昵称、性别、备注）
     * @param dto 待更新信息
     */
    void updateProfile(SysUserUpdateProfileDTO dto);

    /**
     * 修改当前登录用户密码
     * @param dto 原密码、新密码、确认新密码
     */
    void changePassword(SysUserChangePasswordDTO dto);

    /**
     * 重置用户密码（重新生成盐值并加密存储）
     * @param dto 重置密码参数
     */
    void resetPassword(SysUserResetPasswordDTO dto);

    /**
     * 导出用户列表
     * @param query 查询参数
     * @param response 响应对象
     */
    void export(SysUserQuery query, HttpServletResponse response);
}
