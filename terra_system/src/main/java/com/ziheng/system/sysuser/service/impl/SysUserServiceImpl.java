package com.ziheng.system.sysuser.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.enums.DeleteFlagEnum;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.file.FileService;
import com.ziheng.common.file.FileType;
import com.ziheng.common.util.PasswordUtils;
import com.ziheng.common.util.excel.ExcelExportUtils;
import com.ziheng.system.security.util.LoginUserUtils;
import com.ziheng.system.sysdept.domain.SysDept;
import com.ziheng.system.sysdept.service.SysDeptService;
import com.ziheng.system.sysloginlog.domain.SysLoginLog;
import com.ziheng.system.sysloginlog.service.SysLoginLogService;
import com.ziheng.system.sysuser.domain.SysUser;
import com.ziheng.system.sysuser.domain.dto.SysUserAddDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserQuery;
import com.ziheng.system.sysuser.domain.dto.SysUserResetPasswordDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserUpdateProfileDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserChangePasswordDTO;
import com.ziheng.system.sysuser.domain.vo.SysUserEditVO;
import com.ziheng.system.sysuser.domain.vo.SysUserExportVO;
import com.ziheng.system.sysuser.domain.vo.SysUserProfileVO;
import com.ziheng.system.sysuser.domain.vo.SysUserVo;
import com.ziheng.system.sysuser.mapper.SysUserMapper;
import com.ziheng.system.sysuser.service.SysUserService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【sys_user(系统用户表)】的数据库操作Service实现
* @createDate 2026-06-12 17:10:57
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService {

    private final SysDeptService sysDeptService;
    private final SysLoginLogService sysLoginLogService;
    private final FileService fileService;

    public SysUserServiceImpl(SysDeptService sysDeptService, SysLoginLogService sysLoginLogService,
                              FileService fileService) {
        this.sysDeptService = sysDeptService;
        this.sysLoginLogService = sysLoginLogService;
        this.fileService = fileService;
    }

    @Override
    public PageResult<SysUserVo> listUsers(SysUserQuery query) {
        Page<SysUser> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDelFlag, "0")
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getPhone()), SysUser::getPhone, query.getPhone())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
                .ge(StringUtils.hasText(query.getStartTime()), SysUser::getCreateTime, query.getStartTime())
                .le(StringUtils.hasText(query.getEndTime()), SysUser::getCreateTime, resolveEnd(query.getEndTime()))
                .orderByDesc(SysUser::getCreateTime)
                .orderByDesc(SysUser::getUserId);

        Page<SysUser> result = page(page, wrapper);
        List<SysUser> records = result.getRecords();
        // 批量查询部门名称
        Map<Long, String> deptNameMap = queryDeptNames(records);
        List<SysUserVo> rows = records.stream()
                .map(user -> {
                    SysUserVo vo = SysUserVo.from(user);
                    vo.setDeptName(deptNameMap.get(user.getDeptId()));
                    return vo;
                })
                .toList();
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 批量查询部门ID对应的部门名称
     */
    private Map<Long, String> queryDeptNames(List<SysUser> records) {
        Set<Long> deptIds = records.stream()
                .map(SysUser::getDeptId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        return sysDeptService.listByIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName, (a, b) -> a));
    }

    /**
     * 解析结束时间，仅日期(yyyy-MM-dd)时补 23:59:59
     */
    private String resolveEnd(String endTime) {
        if (endTime == null) {
            return null;
        }
        if (endTime.length() == 10) {
            return endTime + " 23:59:59";
        }
        return endTime;
    }

    /**
     * 新增或修改用户（userId 为空表示新增，不为空表示修改）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUser(SysUserAddDTO dto) {
        Long userId = dto.getUserId();
        if (userId == null) {
            userId = insertUser(dto);
        } else {
            updateUser(dto);
        }
        // 重新维护用户-角色关联
        saveUserRole(userId, dto.getRoleIds());
        return userId;
    }

    /**
     * 查询用户详情（编辑回显，含角色ID集合）
     */
    @Override
    public SysUserEditVO getUserDetail(Long userId) {
        SysUser user = getById(userId);
        if (user == null || "1".equals(user.getDelFlag())) {
            throw new BusinessException("用户不存在");
        }
        SysUserEditVO vo = new SysUserEditVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleIds(baseMapper.selectRoleIdsByUserId(userId));
        return vo;
    }



    /**
     * 删除用户（支持单条或批量），逻辑删除并清理用户角色关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }
        Date now = new Date();
        for (Long userId : userIds) {
            SysUser user = getById(userId);
            if (user == null || "1".equals(user.getDelFlag())) {
                throw new BusinessException("用户不存在：" + userId);
            }
            SysUser update = new SysUser();
            update.setUserId(userId);
            update.setDelFlag("1");
            update.setUpdateTime(now);
            updateById(update);
            // 清理用户角色关联
            baseMapper.deleteUserRole(userId);
        }
    }

    /**
     * 新增用户
     */
    private Long insertUser(SysUserAddDTO dto) {
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        checkUsernameUnique(dto.getUsername());
        checkDeptExists(dto.getDeptId());
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        // 生成盐值并加密密码（三次MD5，与登录校验保持一致）
        String salt = PasswordUtils.generateSalt();
        user.setSalt(salt);
        user.setPassword(PasswordUtils.encryptTripleMd5(dto.getPassword(), salt));
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus("1");
        }
        if (!StringUtils.hasText(user.getSex())) {
            user.setSex("0");
        }
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setDelFlag("0");
        save(user);
        return user.getUserId();
    }

    /**
     * 修改用户
     * 仅允许修改昵称、部门、手机号、邮箱、性别、状态、角色、备注；
     * 用户名与密码不允许通过本接口修改，若提交了与原值不一致的用户名或非空密码会直接报错
     */
    private void updateUser(SysUserAddDTO dto) {
        Long userId = dto.getUserId();
        SysUser old = getById(userId);
        if (old == null || "1".equals(old.getDelFlag())) {
            throw new BusinessException("用户不存在");
        }
        // 用户名不允许修改
        if (StringUtils.hasText(dto.getUsername())
                && !dto.getUsername().equals(old.getUsername())) {
            throw new BusinessException("不允许修改用户名");
        }
        // 密码不允许通过编辑接口修改（如需要重置密码请走独立的重置接口）
        if (StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("不允许修改密码");
        }
        checkDeptExists(dto.getDeptId());
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        // 用户名与密码（含盐值）一律以数据库原值为准
        user.setUsername(old.getUsername());
        user.setPassword(old.getPassword());
        user.setSalt(old.getSalt());
        user.setUpdateTime(new Date());
        updateById(user);
    }

    /**
     * 校验用户名唯一（仅新增时使用，用户名修改场景不允许变更故无需排除自身）
     */
    private void checkUsernameUnique(String username) {
        Long count = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    /**
     * 校验部门是否存在
     */
    private void checkDeptExists(Long deptId) {
        if (deptId != null && sysDeptService.getById(deptId) == null) {
            throw new BusinessException("部门不存在");
        }
    }

    /**
     * 重置用户密码
     * 校验用户存在且未删除、用户名与库中一致后，重新生成盐值并加密存储
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(SysUserResetPasswordDTO dto) {
        Long userId = dto.getUserId();
        SysUser user = getById(userId);
        if (user == null || "1".equals(user.getDelFlag())) {
            throw new BusinessException("用户不存在");
        }
        // 用户名二次确认，防止误操作他人账号
        if (!dto.getUsername().equals(user.getUsername())) {
            throw new BusinessException("用户名与用户不匹配");
        }
        // 重新生成盐值并加密（三次MD5，与登录校验保持一致）
        String salt = PasswordUtils.generateSalt();
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setSalt(salt);
        update.setPassword(PasswordUtils.encryptTripleMd5(dto.getPassword(), salt));
        update.setUpdateTime(new Date());
        updateById(update);
    }

    @Override
    public void export(SysUserQuery query, HttpServletResponse response) {
        List<SysUserExportVO> vo = baseMapper.selectUserExportList(query);
        try {
            ExcelExportUtils.export(vo, SysUserExportVO.class, "用户列表", response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException("导出失败");
        }
    }

    /**
     * 保存用户角色关联（先删后插）
     */
    private void saveUserRole(Long userId, List<Long> roleIds) {
        baseMapper.deleteUserRole(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            baseMapper.insertUserRole(userId, roleIds);
        }
    }

    private static final DateTimeFormatter LOGIN_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 查询用户资料详情（个人中心）。
     *
     * <p>基础资料取自 sys_user，最近登录取自最近一条成功登录日志并拼成
     * "浏览器 · 系统 · IP · 时间 · 地点" 的展示串。</p>
     */
    @Override
    public SysUserProfileVO getUserProfile(Long userId) {
        SysUser user = getById(userId);
        if (user == null || "1".equals(user.getDelFlag())) {
            throw new BusinessException("用户不存在");
        }
        SysUserProfileVO vo = SysUserProfileVO.from(user);
        SysLoginLog latest = sysLoginLogService.getLatestSuccess(userId);
        if (latest != null) {
            vo.setLastLoginInfo(buildLastLoginInfo(latest));
            vo.setLastLoginTime(latest.getLoginTime());
        }
        return vo;
    }

    /**
     * 拼接最近登录信息：浏览器 · 操作系统 · IP · 时间 · 登录地点。
     * 任一段缺失时用"未知"兜底，避免前端展示出现 null。
     */
    private String buildLastLoginInfo(SysLoginLog log) {
        String browser = StringUtils.hasText(log.getBrowser()) ? log.getBrowser() : "未知浏览器";
        String os = StringUtils.hasText(log.getOs()) ? log.getOs() : "未知系统";
        String ip = StringUtils.hasText(log.getIpAddress()) ? log.getIpAddress() : "未知IP";
        String time = log.getLoginTime() != null
                ? LOGIN_TIME_FORMAT.format(LocalDateTime.ofInstant(log.getLoginTime().toInstant(), ZoneId.systemDefault()))
                : "";
        String location = StringUtils.hasText(log.getLoginLocation()) ? log.getLoginLocation() : "未知地点";
        return String.join(" · ", browser, os, ip, time, location);
    }

    /**
     * 上传头像并更新当前登录用户的 avatar 字段。
     * 仅允许 jpg、png（由 FileService 内部校验），返回头像相对存储路径。
     */
    @Override
    public String updateAvatar(MultipartFile file) {
        Long userId = LoginUserUtils.getUserId();
        String avatar = fileService.upload(file, FileType.IMAGE);
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setAvatar(avatar);
        updateById(update);
        return avatar;
    }

    /**
     * 修改当前登录用户个人信息，仅允许更新昵称、性别、备注三个字段。
     */
    @Override
    public void updateProfile(SysUserUpdateProfileDTO dto) {
        Long userId = LoginUserUtils.getUserId();
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setNickname(dto.getNickname());
        update.setSex(dto.getSex());
        update.setRemark(dto.getRemark());
        updateById(update);
    }

    /**
     * 修改当前登录用户密码：
     * 1. 校验原密码是否正确；2. 校验两次新密码一致；3. 使用新盐值重新加密存储。
     */
    @Override
    public void changePassword(SysUserChangePasswordDTO dto) {
        Long userId = LoginUserUtils.getUserId();
        SysUser user = getById(userId);
        if (user == null || "1".equals(user.getDelFlag())) {
            throw new BusinessException("用户不存在");
        }
        // 校验原密码是否正确
        if (!PasswordUtils.matches(dto.getOldPassword(), user.getPassword(), user.getSalt())) {
            throw new BusinessException("原密码不正确");
        }
        // 校验两次输入的新密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        // 使用新的盐值重新加密（三次MD5，与登录校验保持一致）
        String salt = PasswordUtils.generateSalt();
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setSalt(salt);
        update.setPassword(PasswordUtils.encryptTripleMd5(dto.getNewPassword(), salt));
        update.setUpdateTime(new Date());
        updateById(update);

        // 当前用户退出登录
        StpUtil.logout();
    }
}
