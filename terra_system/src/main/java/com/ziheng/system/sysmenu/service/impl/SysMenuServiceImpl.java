package com.ziheng.system.sysmenu.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.common.core.enums.DeleteFlagEnum;
import com.ziheng.common.core.enums.EnableStatusEnum;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.system.sysmenu.domain.SysMenu;
import com.ziheng.system.sysmenu.domain.enums.MenuCache;
import com.ziheng.system.sysmenu.domain.enums.MenuType;
import com.ziheng.system.sysmenu.domain.enums.MenuVisible;
import com.ziheng.system.sysmenu.domain.dto.SysMenuAddDTO;
import com.ziheng.system.sysmenu.domain.dto.SysMenuQuery;
import com.ziheng.system.sysmenu.domain.dto.SysMenuSortDTO;
import com.ziheng.system.sysmenu.domain.vo.MetaVo;
import com.ziheng.system.sysmenu.domain.vo.RouterVO;
import com.ziheng.system.sysmenu.domain.vo.SysMenuListVO;
import com.ziheng.system.sysmenu.domain.vo.SysMenuTreeVO;
import com.ziheng.system.sysmenu.mapper.SysMenuMapper;
import com.ziheng.system.sysmenu.service.SysMenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service实现
* @createDate 2026-06-15 21:41:11
*/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService {

    /**
     * 根据用户ID查询系统菜单列表
     * @param userId 用户ID
     * @return 系统菜单列表
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        return buildTree(baseMapper.findMenuTreeByUserId(userId), 0L);
    }

    /**
     * 动态构建路由树
     * @param sysMenuList
     * @return
     */
    @Override
    public RouterVO convertToRouteTree(List<SysMenu> sysMenuList) {
        RouterVO root = new RouterVO();
        root.setName("Root");
        root.setPath("/");
        root.setComponent("Layout");
        //root.setRedirect("/index");
        root.setChildren(new ArrayList<>());
        root.setMeta(new MetaVo("", "", true, false));

        root.setChildren(newBuildMenuTree(sysMenuList));
//        System.out.println(root);
//        System.out.println(objectMapper.writeValueAsString(root));
        return root;
    }

    public List<RouterVO> newBuildMenuTree(List<SysMenu> menus) {
        List<RouterVO> routers = new ArrayList<>();
        menus.forEach(menu ->{
            RouterVO route = new RouterVO();
            route.setName(menu.getName());
            route.setPath(menu.getPath());
            route.setComponent(menu.getComponent());
            route.setRedirect(menu.getRedirect());
            // meta
            MetaVo meta = new MetaVo();
            meta.setTitle(menu.getMenuName());
            meta.setIcon(menu.getIcon());
            meta.setHidden(!MenuVisible.VISIBLE.getCode().equals(menu.getVisible()));
            meta.setKeepAlive(MenuCache.CACHE.getCode().equals(menu.getKeepAlive()));
            route.setMeta(meta);

            // 递归处理子路由
            if (CollectionUtils.isNotEmpty(menu.getChildren())) {
                List<RouterVO> children = newBuildMenuTree(menu.getChildren());
                route.setChildren(children);
            }else {
                route.setChildren(new ArrayList<>());
            }
            routers.add(route);
        });
        return routers;
    }

    /**
     * 获取菜单树（用于选择上级菜单，needRoot=true 时带顶级根节点"主类目"）
     * @param needRoot 是否需要构建 root 顶级节点
     * @return 菜单树
     */
    @Override
    public List<SysMenuTreeVO> listMenuTree(Boolean needRoot) {
        // 查询所有未删除的菜单
        List<SysMenu> menuList = lambdaQuery()
                .eq(SysMenu::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .orderByAsc(SysMenu::getMenuId)
                .list();
        if (needRoot) {
            // 构建 root 顶级节点（id=0，名称"主类目"）
            SysMenuTreeVO root = new SysMenuTreeVO();
            root.setMenuId(0L);
            root.setMenuName("主类目");
            root.setChildren(buildMenuTree(menuList, 0L));
            List<SysMenuTreeVO> tree = new ArrayList<>();
            tree.add(root);
            return tree;
        }
        // 不构建 root，直接返回顶级菜单树
        return buildMenuTree(menuList, 0L);
    }

    /**
     * 查询菜单列表树（用于菜单管理表格展示）
     * @param query 查询条件（菜单名称模糊、状态）
     * @return 菜单列表树
     */
    @Override
    public List<SysMenuListVO> listMenuList(SysMenuQuery query) {
        // 查询所有未删除的菜单，按显示顺序升序
        List<SysMenu> allMenuList = lambdaQuery()
                .eq(SysMenu::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .orderByAsc(SysMenu::getMenuSort)
                .list();

        // 按菜单名称（模糊）和状态过滤
        List<SysMenu> matchedList = allMenuList.stream()
                .filter(menu -> !StringUtils.hasText(query.getMenuName())
                        || menu.getMenuName().contains(query.getMenuName()))
                .filter(menu -> query.getStatus() == null || query.getStatus().equals(menu.getStatus()))
                .collect(Collectors.toList());

        // 过滤后补充祖先节点，保证树结构完整
        if (matchedList.size() < allMenuList.size()) {
            Set<Long> keepIds = matchedList.stream()
                    .map(SysMenu::getMenuId)
                    .collect(Collectors.toSet());
            Map<Long, SysMenu> menuMap = allMenuList.stream()
                    .collect(Collectors.toMap(SysMenu::getMenuId, menu -> menu));
            for (SysMenu menu : matchedList) {
                Long pid = menu.getParentId();
                while (pid != null && pid != 0L && !keepIds.contains(pid)) {
                    SysMenu parent = menuMap.get(pid);
                    if (parent == null) {
                        break;
                    }
                    keepIds.add(pid);
                    pid = parent.getParentId();
                }
            }
            matchedList = allMenuList.stream()
                    .filter(menu -> keepIds.contains(menu.getMenuId()))
                    .collect(Collectors.toList());
        }
        return buildMenuList(matchedList, 0L);
    }

    /**
     * 递归构建菜单列表树
     * @param menuList 菜单列表
     * @param parentId 父菜单ID
     * @return 菜单列表树
     */
    private List<SysMenuListVO> buildMenuList(List<SysMenu> menuList, Long parentId) {
        List<SysMenuListVO> tree = new ArrayList<>();
        for (SysMenu menu : menuList) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                SysMenuListVO vo = SysMenuListVO.from(menu);
                vo.setChildren(buildMenuList(menuList, menu.getMenuId()));
                tree.add(vo);
            }
        }
        return tree;
    }

    /**
     * 递归构建菜单树
     * @param menuList 菜单列表
     * @param parentId 父菜单ID
     * @return 菜单树
     */
    private List<SysMenuTreeVO> buildMenuTree(List<SysMenu> menuList, Long parentId) {
        List<SysMenuTreeVO> tree = new ArrayList<>();
        for (SysMenu menu : menuList) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                SysMenuTreeVO vo = new SysMenuTreeVO();
                vo.setMenuId(menu.getMenuId());
                vo.setMenuName(menu.getMenuName());
                vo.setChildren(buildMenuTree(menuList, menu.getMenuId()));
                tree.add(vo);
            }
        }
        return tree;
    }

    /**
     * 构建树结构
     * @param menuList 菜单列表
     * @param parentId 父级ID
     * @return 树结构
     */
    public List<SysMenu> buildTree(List<SysMenu> menuList, Long parentId) {
        List<SysMenu> children = new ArrayList<>();
        for (SysMenu menu : menuList) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                menu.setChildren(
                        buildTree(menuList, menu.getMenuId())
                );
                children.add(menu);
            }
        }
        return children;
    }

    /**
     * 新增或修改菜单（dto.menuId 为空则新增，不为空则修改）
     * @param dto 菜单信息
     * @return 菜单ID
     */
    @Override
    public Long addMenu(SysMenuAddDTO dto) {
        Long menuId = dto.getMenuId();
        if (menuId == null) {
            return insertMenu(dto);
        }
        updateMenu(dto);
        return menuId;
    }

    /**
     * 查询菜单详情（编辑回显）
     * @param menuId 菜单ID
     * @return 菜单
     */
    @Override
    public SysMenu getMenuById(Long menuId) {
        SysMenu menu = getById(menuId);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    /**
     * 删除菜单
     * @param menuId 菜单ID
     */
    @Override
    public void deleteMenu(Long menuId) {
        SysMenu menu = getById(menuId);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        // 校验是否存在子菜单
        long childCount = lambdaQuery()
                .eq(SysMenu::getParentId, menuId)
                .eq(SysMenu::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .count();
        if (childCount > 0) {
            throw new BusinessException("存在下级菜单，不允许删除");
        }
        // 逻辑删除
        SysMenu update = new SysMenu();
        update.setMenuId(menuId);
        update.setDelFlag(DeleteFlagEnum.DELETED.getCode());
        update.setUpdateTime(new Date());
        updateById(update);
    }

    /**
     * 批量保存菜单排序（接收树形结构，保存前端传入的 menuSort 值）
     * @param sortList 排序树
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenuSort(List<SysMenuSortDTO> sortList) {
        if (sortList == null || sortList.isEmpty()) {
            return;
        }
        // 递归按层级保存前端传入的排序值
        applySort(sortList);
    }

    /**
     * 递归保存排序：直接保存前端传入的 menuSort 值
     */
    private void applySort(List<SysMenuSortDTO> sortList) {
        Date now = new Date();
        for (SysMenuSortDTO dto : sortList) {
            if (dto.getMenuId() == null || dto.getMenuSort() == null) {
                continue;
            }
            SysMenu update = new SysMenu();
            update.setMenuId(dto.getMenuId());
            update.setMenuSort(dto.getMenuSort());
            update.setUpdateTime(now);
            updateById(update);
            // 递归处理子菜单
            if (dto.getChildren() != null && !dto.getChildren().isEmpty()) {
                applySort(dto.getChildren());
            }
        }
    }

    /**
     * 新增菜单
     */
    private Long insertMenu(SysMenuAddDTO dto) {
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        // 校验父节点类型规则
        validateParentType(parentId, dto.getMenuType());
        // 校验同级菜单名唯一
        checkMenuNameUnique(parentId, dto.getMenuName(), null);
        // 校验父菜单存在
        if (parentId != 0L && getById(parentId) == null) {
            throw new BusinessException("父菜单不存在");
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menu.setParentId(parentId);
        // keepAlive 前端为 Integer，转为字符串
        if (dto.getKeepAlive() != null) {
            menu.setKeepAlive(dto.getKeepAlive() == 1 ? MenuCache.CACHE.getCode() : MenuCache.NO_CACHE.getCode());
        }
        if (!StringUtils.hasText(menu.getStatus())) {
            menu.setStatus(EnableStatusEnum.ENABLED.getCode());
        }
        if (!StringUtils.hasText(menu.getVisible())) {
            menu.setVisible(MenuVisible.VISIBLE.getCode());
        }
        menu.setDelFlag(DeleteFlagEnum.EXIST.getCode());
        Date now = new Date();
        menu.setCreateTime(now);
        menu.setUpdateTime(now);
        save(menu);
        return menu.getMenuId();
    }

    /**
     * 修改菜单
     */
    private void updateMenu(SysMenuAddDTO dto) {
        SysMenu oldMenu = getById(dto.getMenuId());
        if (oldMenu == null) {
            throw new BusinessException("菜单不存在");
        }
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        // 防环路：父节点不能是自己或自己的子节点
        if (isSelfOrChild(dto.getMenuId(), parentId)) {
            throw new BusinessException("上级菜单不能选择自己或当前菜单的子菜单");
        }
        // 校验父节点类型规则
        validateParentType(parentId, dto.getMenuType());
        // 校验同级菜单名唯一（排除自身）
        checkMenuNameUnique(parentId, dto.getMenuName(), dto.getMenuId());
        // 校验父菜单存在
        if (parentId != 0L && getById(parentId) == null) {
            throw new BusinessException("父菜单不存在");
        }
        // 修改类型时校验现有子节点是否仍然兼容
        if (!Objects.equals(oldMenu.getMenuType(), dto.getMenuType())) {
            validateChildrenType(dto.getMenuId(), dto.getMenuType());
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menu.setParentId(parentId);
        if (dto.getKeepAlive() != null) {
            menu.setKeepAlive(dto.getKeepAlive() == 1 ? MenuCache.CACHE.getCode() : MenuCache.NO_CACHE.getCode());
        }
        menu.setUpdateTime(new Date());
        updateById(menu);
    }

    /**
     * 校验父节点类型与子菜单类型是否匹配
     * 规则：根节点/目录(M)下可加目录(M)、菜单(C)，不可加按钮(F)；菜单(C)下只可加按钮(F)；按钮(F)下不可加任何子节点
     * @param parentId 父菜单ID
     * @param menuType 子菜单类型
     */
    private void validateParentType(Long parentId, String menuType) {
        if (!MenuType.isValid(menuType)) {
            throw new BusinessException("菜单类型不正确");
        }
        String parentType = null;
        if (parentId != null && parentId != 0L) {
            SysMenu parent = getById(parentId);
            if (parent == null) {
                throw new BusinessException("父菜单不存在");
            }
            parentType = parent.getMenuType();
        }
        if (MenuType.BUTTON.getCode().equals(menuType)) {
            // 按钮只能挂在菜单(C)下
            if (!MenuType.MENU.getCode().equals(parentType)) {
                throw new BusinessException("按钮只能添加在【菜单】类型的节点下");
            }
        } else {
            // 目录(M)、菜单(C)只能挂在根节点或目录(M)下
            if (parentType != null && !MenuType.DIRECTORY.getCode().equals(parentType)) {
                throw new BusinessException("目录或菜单只能添加在根节点或【目录】类型的节点下");
            }
        }
    }

    /**
     * 修改菜单类型时校验现有子节点是否仍然兼容
     */
    private void validateChildrenType(Long menuId, String newType) {
        List<SysMenu> children = lambdaQuery()
                .eq(SysMenu::getParentId, menuId)
                .eq(SysMenu::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .list();
        if (children.isEmpty()) {
            return;
        }
        if (MenuType.BUTTON.getCode().equals(newType)) {
            throw new BusinessException("当前菜单存在子节点，不允许修改为【按钮】类型");
        }
        if (MenuType.MENU.getCode().equals(newType)) {
            boolean hasInvalid = children.stream().anyMatch(c -> !MenuType.BUTTON.getCode().equals(c.getMenuType()));
            if (hasInvalid) {
                throw new BusinessException("当前菜单存在非【按钮】子节点，不允许修改为【菜单】类型");
            }
        }
        if (MenuType.DIRECTORY.getCode().equals(newType)) {
            boolean hasInvalid = children.stream().anyMatch(c -> MenuType.BUTTON.getCode().equals(c.getMenuType()));
            if (hasInvalid) {
                throw new BusinessException("当前菜单存在【按钮】子节点，不允许修改为【目录】类型");
            }
        }
    }

    /**
     * 校验同级菜单名唯一
     */
    private void checkMenuNameUnique(Long parentId, String menuName, Long excludeId) {
        boolean exists = lambdaQuery()
                .eq(SysMenu::getParentId, parentId)
                .eq(SysMenu::getMenuName, menuName)
                .ne(excludeId != null, SysMenu::getMenuId, excludeId)
                .count() > 0;
        if (exists) {
            throw new BusinessException("同级下已存在同名菜单");
        }
    }

    /**
     * 判断 parentId 是否是 menuId 自身或其子节点（防止形成环路）
     */
    private boolean isSelfOrChild(Long menuId, Long parentId) {
        if (parentId == null || parentId == 0L) {
            return false;
        }
        if (Objects.equals(menuId, parentId)) {
            return true;
        }
        Map<Long, Long> parentMap = lambdaQuery()
                .eq(SysMenu::getDelFlag, DeleteFlagEnum.EXIST.getCode())
                .list().stream()
                .collect(Collectors.toMap(SysMenu::getMenuId, SysMenu::getParentId, (a, b) -> a));
        Long current = parentMap.get(parentId);
        while (current != null) {
            if (Objects.equals(current, menuId)) {
                return true;
            }
            current = parentMap.get(current);
        }
        return false;
    }
}




