package com.ziheng.system.sysdept.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.core.enums.DeleteFlagEnum;
import com.ziheng.common.core.enums.EnableStatusEnum;
import com.ziheng.system.sysdept.domain.SysDept;
import com.ziheng.system.sysdept.domain.dto.SysDeptAddDTO;
import com.ziheng.system.sysdept.domain.dto.SysDeptQuery;
import com.ziheng.system.sysdept.domain.dto.SysDeptSortDTO;
import com.ziheng.system.sysdept.domain.vo.SysDeptListVO;
import com.ziheng.system.sysdept.domain.vo.SysDeptTreeVO;
import com.ziheng.system.sysdept.service.SysDeptService;
import com.ziheng.system.sysdept.mapper.SysDeptMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【sys_dept(部门表)】的数据库操作Service实现
* @createDate 2026-08-23 13:15:18
*/
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
    implements SysDeptService{

    @Override
    public Long addDept(SysDeptAddDTO dto) {
        Long deptId = dto.getDeptId();
        if (deptId == null) {
            // ============ 新增 ============
            return insertDept(dto);
        }
        // ============ 修改 ============
        updateDept(dto);
        return deptId;
    }

    @Override
    public SysDept getDeptById(Long deptId) {
        SysDept dept = getById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    /**
     * 新增部门
     */
    private Long insertDept(SysDeptAddDTO dto) {
        // 父部门ID为空时默认为顶级部门(0)
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        // 校验父部门是否存在
        if (parentId != 0L && getById(parentId) == null) {
            throw new BusinessException("父部门不存在");
        }
        // 校验同级下部门名称是否重复
        if (checkDeptNameExist(parentId, dto.getDeptName(), null)) {
            throw new BusinessException("同级下已存在同名部门");
        }
        // 校验部门编码是否唯一
        if (checkDeptCodeExist(dto.getDeptCode(), null)) {
            throw new BusinessException("部门编码已存在");
        }

        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        dept.setParentId(parentId);
        if (!StringUtils.hasText(dept.getStatus())) {
            dept.setStatus(EnableStatusEnum.ENABLED.getCode());
        }
        dept.setDelFlag(DeleteFlagEnum.EXIST.getCode());
        Date now = new Date();
        dept.setCreateTime(now);
        dept.setUpdateTime(now);
        save(dept);
        return dept.getDeptId();
    }

    /**
     * 修改部门
     */
    private void updateDept(SysDeptAddDTO dto) {
        Long deptId = dto.getDeptId();
        SysDept oldDept = getById(deptId);
        if (oldDept == null) {
            throw new BusinessException("部门不存在");
        }
        // 父部门ID为空时默认为顶级部门(0)
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        // 父部门不能是自己或其子部门，防止形成环路
        if (isSelfOrChild(deptId, parentId)) {
            throw new BusinessException("父部门不能选择自己或当前部门的子部门");
        }
        // 校验父部门是否存在
        if (parentId != 0L && getById(parentId) == null) {
            throw new BusinessException("父部门不存在");
        }
        // 校验同级下部门名称是否重复（排除自身）
        if (checkDeptNameExist(parentId, dto.getDeptName(), deptId)) {
            throw new BusinessException("同级下已存在同名部门");
        }
        // 校验部门编码是否唯一（排除自身）
        if (checkDeptCodeExist(dto.getDeptCode(), deptId)) {
            throw new BusinessException("部门编码已存在");
        }

        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        dept.setParentId(parentId);
        if (!StringUtils.hasText(dept.getStatus())) {
            dept.setStatus(EnableStatusEnum.ENABLED.getCode());
        }
        dept.setUpdateTime(new Date());
        updateById(dept);
    }

    /**
     * 校验同级下部门名称是否重复
     */
    private boolean checkDeptNameExist(Long parentId, String deptName, Long excludeId) {
        return lambdaQuery()
                .eq(SysDept::getParentId, parentId)
                .eq(SysDept::getDeptName, deptName)
                .ne(excludeId != null, SysDept::getDeptId, excludeId)
                .count() > 0;
    }

    /**
     * 校验部门编码是否唯一
     */
    private boolean checkDeptCodeExist(String deptCode, Long excludeId) {
        return lambdaQuery()
                .eq(SysDept::getDeptCode, deptCode)
                .ne(excludeId != null, SysDept::getDeptId, excludeId)
                .count() > 0;
    }

    /**
     * 判断 parentId 是否为 deptId 本身或其子孙部门ID
     */
    private boolean isSelfOrChild(Long deptId, Long parentId) {
        if (parentId == null || parentId == 0L || Objects.equals(deptId, parentId)) {
            return Objects.equals(deptId, parentId);
        }
        List<SysDept> deptList = lambdaQuery().eq(SysDept::getDelFlag, DeleteFlagEnum.EXIST.getCode()).list();
        Map<Long, Long> parentMap = deptList.stream()
                .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getParentId, (a, b) -> a));
        Long current = parentMap.get(parentId);
        while (current != null) {
            if (Objects.equals(current, deptId)) {
                return true;
            }
            current = parentMap.get(current);
        }
        return false;
    }

    @Override
    public void deleteDept(Long deptId) {
        SysDept dept = getById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        // 校验是否存在子部门
        long childCount = lambdaQuery()
                .eq(SysDept::getParentId, deptId)
                .eq(SysDept::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .count();
        if (childCount > 0) {
            throw new BusinessException("存在下级部门，不允许删除");
        }
        // 逻辑删除
        SysDept update = new SysDept();
        update.setDeptId(deptId);
        update.setDelFlag(DeleteFlagEnum.DELETED.getCode());
        update.setUpdateTime(new Date());
        updateById(update);
    }

    @Override
    public List<SysDeptTreeVO> listDeptTree() {
        // 查询所有未删除的部门，按显示排序升序
        List<SysDept> deptList = lambdaQuery()
                .eq(SysDept::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .orderByAsc(SysDept::getSortOrder)
                .list();
        return buildDeptTree(deptList, 0L);
    }

    @Override
    public List<SysDeptListVO> listDeptList(SysDeptQuery query) {
        // 查询所有未删除的部门，按显示排序升序
        List<SysDept> allDeptList = lambdaQuery()
                .eq(SysDept::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .orderByAsc(SysDept::getSortOrder)
                .list();

        // 按部门名称（模糊）和状态过滤
        List<SysDept> matchedList = allDeptList.stream()
                .filter(dept -> !StringUtils.hasText(query.getDeptName())
                        || dept.getDeptName().contains(query.getDeptName()))
                .filter(dept -> query.getStatus() == null || query.getStatus().equals(dept.getStatus()))
                .collect(Collectors.toList());

        // 过滤后补充祖先节点，保证树结构完整
        if (matchedList.size() < allDeptList.size()) {
            Set<Long> keepIds = matchedList.stream()
                    .map(SysDept::getDeptId)
                    .collect(Collectors.toSet());
            Map<Long, SysDept> deptMap = allDeptList.stream()
                    .collect(Collectors.toMap(SysDept::getDeptId, dept -> dept));
            for (SysDept dept : matchedList) {
                Long pid = dept.getParentId();
                while (pid != null && pid != 0L && !keepIds.contains(pid)) {
                    SysDept parent = deptMap.get(pid);
                    if (parent == null) {
                        break;
                    }
                    keepIds.add(pid);
                    pid = parent.getParentId();
                }
            }
            matchedList = allDeptList.stream()
                    .filter(dept -> keepIds.contains(dept.getDeptId()))
                    .collect(Collectors.toList());
        }
        return buildDeptList(matchedList, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeptSort(List<SysDeptSortDTO> sortList) {
        if (sortList == null || sortList.isEmpty()) {
            return;
        }
        // 递归按层级保存前端传入的排序值
        applySort(sortList);
    }

    /**
     * 递归保存排序：直接保存前端传入的 sortOrder 值
     */
    private void applySort(List<SysDeptSortDTO> sortList) {
        Date now = new Date();
        for (SysDeptSortDTO dto : sortList) {
            if (dto.getDeptId() == null || dto.getSortOrder() == null) {
                continue;
            }
            SysDept update = new SysDept();
            update.setDeptId(dto.getDeptId());
            update.setSortOrder(dto.getSortOrder());
            update.setUpdateTime(now);
            updateById(update);
            // 递归处理子部门
            if (dto.getChildren() != null && !dto.getChildren().isEmpty()) {
                applySort(dto.getChildren());
            }
        }
    }

    /**
     * 递归构建部门树
     *
     * @param deptList 部门列表
     * @param parentId 父部门ID
     * @return 部门树
     */
    private List<SysDeptTreeVO> buildDeptTree(List<SysDept> deptList, Long parentId) {
        List<SysDeptTreeVO> tree = new ArrayList<>();
        for (SysDept dept : deptList) {
            if (Objects.equals(dept.getParentId(), parentId)) {
                SysDeptTreeVO vo = new SysDeptTreeVO();
                vo.setDeptId(dept.getDeptId());
                vo.setDeptName(dept.getDeptName());
                vo.setChildren(buildDeptTree(deptList, dept.getDeptId()));
                tree.add(vo);
            }
        }
        return tree;
    }

    /**
     * 递归构建部门列表树
     *
     * @param deptList 部门列表
     * @param parentId 父部门ID
     * @return 部门列表树
     */
    private List<SysDeptListVO> buildDeptList(List<SysDept> deptList, Long parentId) {
        List<SysDeptListVO> tree = new ArrayList<>();
        for (SysDept dept : deptList) {
            if (Objects.equals(dept.getParentId(), parentId)) {
                SysDeptListVO vo = SysDeptListVO.from(dept);
                vo.setChildren(buildDeptList(deptList, dept.getDeptId()));
                tree.add(vo);
            }
        }
        return tree;
    }
}




