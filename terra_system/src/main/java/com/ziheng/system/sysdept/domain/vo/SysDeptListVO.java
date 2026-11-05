package com.ziheng.system.sysdept.domain.vo;

import com.ziheng.system.sysdept.domain.SysDept;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 部门列表树VO（用于部门管理表格展示）
 */
@Data
public class SysDeptListVO implements Serializable {

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示排序
     */
    private Integer sortOrder;

    /**
     * 状态：1正常，0停用
     */
    private String status;

    /**
     * 创建时间
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 子部门
     */
    private List<SysDeptListVO> children = new ArrayList<>();

    public static SysDeptListVO from(SysDept dept) {
        SysDeptListVO vo = new SysDeptListVO();
        vo.setDeptId(dept.getDeptId());
        vo.setDeptName(dept.getDeptName());
        vo.setSortOrder(dept.getSortOrder());
        vo.setStatus(dept.getStatus());
        vo.setCreateTime(dept.getCreateTime());
        return vo;
    }
}
