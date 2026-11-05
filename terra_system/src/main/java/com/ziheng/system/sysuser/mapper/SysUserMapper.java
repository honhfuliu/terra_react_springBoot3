package com.ziheng.system.sysuser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ziheng.system.sysuser.domain.SysUser;
import com.ziheng.system.sysuser.domain.dto.SysUserQuery;
import com.ziheng.system.sysuser.domain.vo.SysUserExportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_user(系统用户表)】的数据库操作Mapper
* @createDate 2026-06-12 17:10:57
* @Entity generator.domain.SysUser
*/
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 删除用户的角色关联
     * @param userId 用户ID
     */
    void deleteUserRole(@Param("userId") Long userId);

    /**
     * 批量插入用户角色关联
     * @param userId 用户ID
     * @param roleIds 角色ID集合
     */
    void insertUserRole(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 查询用户拥有的角色ID集合
     * @param userId 用户ID
     * @return 角色ID集合
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户列表（导出）
     * @param query
     * @return
     */
    List<SysUserExportVO> selectUserExportList(@Param("query") SysUserQuery query);
}




