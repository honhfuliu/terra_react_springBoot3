package com.ziheng.system.sysrole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.enums.DeleteFlagEnum;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.util.excel.ExcelExportUtils;
import com.ziheng.system.sysrole.domain.SysRole;
import com.ziheng.system.sysrole.domain.dto.SysRoleAddDTO;
import com.ziheng.system.sysrole.domain.dto.SysRoleQuery;
import com.ziheng.system.sysrole.domain.vo.SysRoleExportVO;
import com.ziheng.system.sysrole.domain.vo.SysRoleListVO;
import com.ziheng.system.sysrole.domain.vo.SysRoleOptionVO;
import com.ziheng.system.sysrole.service.SysRoleService;
import com.ziheng.system.sysrole.mapper.SysRoleMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_role(角色表)】的数据库操作Service实现
* @createDate 2026-08-16 20:32:42
*/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
    implements SysRoleService{

    /**
     * 新增或修改角色（dto.roleId 为空则新增，不为空则修改），同时维护角色菜单关联
     * @param dto 角色信息
     * @return 角色ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRole(SysRoleAddDTO dto) {
        Long roleId = dto.getRoleId();
        if (roleId == null) {
            roleId = insertRole(dto);
        } else {
            updateRole(dto);
        }
        // 维护角色菜单关联：先删除再插入
        saveRoleMenu(roleId, dto.getMenuPermissions());
        return roleId;
    }

    /**
     * 查询角色详情（编辑回显，含菜单权限ID集合）
     * @param roleId 角色ID
     * @return 角色详情
     */
    @Override
    public SysRoleAddDTO getRoleDetail(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        SysRoleAddDTO dto = new SysRoleAddDTO();
        BeanUtils.copyProperties(role, dto);
        dto.setMenuPermissions(baseMapper.selectMenuIdsByRoleId(roleId));
        return dto;
    }

    /**
     * 分页查询角色列表
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<SysRoleListVO> listRoles(SysRoleQuery query) {
        Page<SysRole> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDelFlag, "0")
                .like(StringUtils.hasText(query.getName()), SysRole::getRoleName, query.getName())
                .like(StringUtils.hasText(query.getRoleKey()), SysRole::getRoleKey, query.getRoleKey())
                .eq(StringUtils.hasText(query.getStatus()), SysRole::getStatus, query.getStatus())
                .ge(StringUtils.hasText(query.getStartTime()), SysRole::getCreateTime, query.getStartTime())
                .le(StringUtils.hasText(query.getEndTime()), SysRole::getCreateTime, resolveEnd(query.getEndTime()))
                .orderByAsc(SysRole::getSort)
                .orderByDesc(SysRole::getCreateTime);

        Page<SysRole> result = page(page, wrapper);
        List<SysRoleListVO> rows = result.getRecords().stream()
                .map(SysRoleListVO::from)
                .toList();
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 查询角色选项列表（用于前端角色选择框）
     * @return 角色选项列表（仅含角色ID与名称）
     */
    @Override
    public List<SysRoleOptionVO> listRoleOptions() {
        return lambdaQuery()
                .eq(SysRole::getDelFlag, "0")
                .orderByAsc(SysRole::getSort)
                .list()
                .stream()
                .map(role -> {
                    SysRoleOptionVO vo = new SysRoleOptionVO();
                    vo.setRoleId(role.getRoleId());
                    vo.setRoleName(role.getRoleName());
                    return vo;
                })
                .toList();
    }

    /**
     * 导出角色列表（Excel）
     * @param query 查询条件
     * @param response 响应输出流
     */
    @Override
    public void export(SysRoleQuery query, HttpServletResponse response) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .select(SysRole::getRoleId, SysRole::getRoleName, SysRole::getSort,
                        SysRole::getRoleKey, SysRole::getStatus, SysRole::getCreateTime, SysRole::getRemark)
                .eq(SysRole::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .like(StringUtils.hasText(query.getName()), SysRole::getRoleName, query.getName())
                .like(StringUtils.hasText(query.getRoleKey()), SysRole::getRoleKey, query.getRoleKey())
                .eq(StringUtils.hasText(query.getStatus()), SysRole::getStatus, query.getStatus())
                .ge(StringUtils.hasText(query.getStartTime()), SysRole::getCreateTime, query.getStartTime())
                .le(StringUtils.hasText(query.getEndTime()), SysRole::getCreateTime, resolveEnd(query.getEndTime()))
                .orderByAsc(SysRole::getSort)
                .orderByDesc(SysRole::getCreateTime);
        List<SysRole> sysRoles = baseMapper.selectList(wrapper);

        List<SysRoleExportVO> voList = SysRoleExportVO.fromList(sysRoles);
        try {
            ExcelExportUtils.export(voList, SysRoleExportVO.class, "角色列表", response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException("导出失败");
        }
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
     * 删除角色（支持单条或批量），逻辑删除并清理角色菜单关联
     * @param roleIds 角色ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("请选择要删除的角色");
        }
        Date now = new Date();
        for (Long roleId : roleIds) {
            SysRole role = getById(roleId);
            if (role == null) {
                throw new BusinessException("角色不存在：" + roleId);
            }
            // 校验角色是否已分配给用户
            Long userCount = baseMapper.countUserByRoleId(roleId);
            if (userCount != null && userCount > 0) {
                throw new BusinessException("角色【" + role.getRoleName() + "】已分配给用户，不允许删除");
            }
            // 逻辑删除
            SysRole update = new SysRole();
            update.setRoleId(roleId);
            update.setDelFlag("1");
            update.setUpdateTime(now);
            updateById(update);
            // 清理角色菜单关联
            baseMapper.deleteRoleMenu(roleId);
        }
    }

    /**
     * 新增角色
     */
    private Long insertRole(SysRoleAddDTO dto) {
        // 校验角色名/角色标识唯一
        checkRoleNameUnique(dto.getRoleName(), null);
        checkRoleKeyUnique(dto.getRoleKey(), null);

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        if (!StringUtils.hasText(role.getStatus())) {
            role.setStatus("1");
        }
        Date now = new Date();
        role.setCreateTime(now);
        role.setUpdateTime(now);
        role.setDelFlag("0");
        save(role);
        return role.getRoleId();
    }

    /**
     * 修改角色
     */
    private void updateRole(SysRoleAddDTO dto) {
        SysRole oldRole = getById(dto.getRoleId());
        if (oldRole == null) {
            throw new BusinessException("角色不存在");
        }
        // 校验角色名/角色标识唯一（排除自身）
        checkRoleNameUnique(dto.getRoleName(), dto.getRoleId());
        checkRoleKeyUnique(dto.getRoleKey(), dto.getRoleId());

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setUpdateTime(new Date());
        updateById(role);
    }

    /**
     * 保存角色菜单关联（先删除旧关联，再插入新关联）
     */
    private void saveRoleMenu(Long roleId, List<Long> menuIds) {
        baseMapper.deleteRoleMenu(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        baseMapper.insertRoleMenu(roleId, menuIds);
    }

    /**
     * 校验角色名称唯一
     */
    private void checkRoleNameUnique(String roleName, Long excludeId) {
        boolean exists = lambdaQuery()
                .eq(SysRole::getRoleName, roleName)
                .ne(excludeId != null, SysRole::getRoleId, excludeId)
                .count() > 0;
        if (exists) {
            throw new BusinessException("角色名称已存在");
        }
    }

    /**
     * 校验角色标识唯一
     */
    private void checkRoleKeyUnique(String roleKey, Long excludeId) {
        boolean exists = lambdaQuery()
                .eq(SysRole::getRoleKey, roleKey)
                .ne(excludeId != null, SysRole::getRoleId, excludeId)
                .count() > 0;
        if (exists) {
            throw new BusinessException("角色标识已存在");
        }
    }
}
