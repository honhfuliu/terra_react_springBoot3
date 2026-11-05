package com.ziheng.system.sysrole.domain.vo;

import com.ziheng.common.core.enums.EnableStatusEnum;
import com.ziheng.common.util.excel.annotation.ExcelColumn;
import com.ziheng.system.sysrole.domain.SysRole;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * 角色导出 VO
 */
@Data
public class SysRoleExportVO {

    @ExcelColumn(value = "角色编号", order = 0, width = 15)
    private Long roleId;

    @ExcelColumn(value = "角色名称", order = 1, width = 20)
    private String roleName;

    @ExcelColumn(value = "角色权限字符", order = 2, width = 25)
    private String roleKey;

    @ExcelColumn(value = "显示顺序", order = 3, width = 12)
    private Integer sort;

    @ExcelColumn(value = "角色状态", order = 4, width = 12)
    private String status;

    @ExcelColumn(value = "备注", order = 5, width = 30)
    private String remark;

    @ExcelColumn(value = "创建时间", order = 6, width = 22, format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public static List<SysRoleExportVO> fromList(List<SysRole> roles) {
        if (roles == null || roles.isEmpty()) return Collections.emptyList();
        List<SysRoleExportVO> result = new ArrayList<>(roles.size());
        for (SysRole role : roles) {
            SysRoleExportVO vo = new SysRoleExportVO();
            vo.setRoleId(role.getRoleId());
            vo.setRoleName(role.getRoleName());
            vo.setRoleKey(role.getRoleKey());
            vo.setSort(role.getSort());
            vo.setStatus(EnableStatusEnum.getDescription(role.getStatus()));
            vo.setRemark(role.getRemark());
            vo.setCreateTime(role.getCreateTime());
            result.add(vo);
        }
        return result;
    }
}
