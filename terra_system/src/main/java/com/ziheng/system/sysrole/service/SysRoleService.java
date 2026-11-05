package com.ziheng.system.sysrole.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.system.sysrole.domain.SysRole;
import com.ziheng.system.sysrole.domain.dto.SysRoleAddDTO;
import com.ziheng.system.sysrole.domain.dto.SysRoleQuery;
import com.ziheng.system.sysrole.domain.vo.SysRoleListVO;
import com.ziheng.system.sysrole.domain.vo.SysRoleOptionVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_role(角色表)】的数据库操作Service
* @createDate 2026-08-16 20:32:42
*/
public interface SysRoleService extends IService<SysRole> {

    /**
     * 新增或修改角色（dto.roleId 为空则新增，不为空则修改），同时维护角色菜单关联
     * @param dto 角色信息
     * @return 角色ID
     */
    Long addRole(SysRoleAddDTO dto);

    /**
     * 分页查询角色列表
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SysRoleListVO> listRoles(SysRoleQuery query);

    /**
     * 查询角色选项列表（用于前端角色选择框）
     *
     * @return 角色选项列表（仅含角色ID与名称）
     */
    List<SysRoleOptionVO> listRoleOptions();

    /**
     * 查询角色详情（编辑回显，含菜单权限ID集合）
     * @param roleId 角色ID
     * @return 角色详情
     */
    SysRoleAddDTO getRoleDetail(Long roleId);

    /**
     * 删除角色（支持单条或批量），逻辑删除并清理角色菜单关联
     * @param roleIds 角色ID集合
     */
    void deleteRoles(List<Long> roleIds);

    /**
     * 导出角色列表（Excel）
     * @param query 查询条件
     * @param response 响应输出流
     */
    void export(SysRoleQuery query, HttpServletResponse response);

}
