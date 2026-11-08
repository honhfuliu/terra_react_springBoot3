package com.ziheng.admin;



import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ziheng.ai.infrastructure.security.AiApiKeyEncryptor;
import com.ziheng.system.auth.service.AuthService;
//import com.ziheng.system.sysmenu.domain.vo.SysMenuVO;
import com.ziheng.system.sysmenu.domain.SysMenu;
import com.ziheng.system.sysmenu.domain.vo.MetaVo;
import com.ziheng.system.sysmenu.domain.vo.RouterVO;
import com.ziheng.system.sysmenu.mapper.SysMenuMapper;
import com.ziheng.system.sysmenu.service.SysMenuService;
import com.ziheng.system.sysuser.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootTest
class TerraApplicationTest {
    @Autowired
    private AuthService authService;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AiApiKeyEncryptor aiApiKeyEncryptor;

    @Test
    void test00() throws NoSuchAlgorithmException {
        String encrypted = aiApiKeyEncryptor.encrypt("123456");
        System.out.println(encrypted);

        String decrypted = aiApiKeyEncryptor.decrypt(encrypted);
        System.out.println(decrypted);
    }
    @Test
    void test000(){
        stringRedisTemplate.opsForValue().set("terra:test", "hello redis");
    }
    @Test
    void test0000(){
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        System.out.println(HexFormat.of().formatHex(salt).toUpperCase());
    }

//    @Test
//    void test10(){
//        System.out.println("运行");
//        System.out.println(sysUserService.testFindUserInfo());
//    }
    @Test
    void test12() throws JsonProcessingException {
        List<SysMenu> menus = sysMenuService.selectMenuTreeByUserId(1L);
        for (SysMenu menu : menus) {
            System.out.println(menu);
        }
        RouterVO root = new RouterVO();
        root.setName("Root");
        root.setPath("/");
        root.setComponent("Layout");
        root.setRedirect("/index");
        root.setChildren(new ArrayList<>());
        root.setMeta(new MetaVo("", "", true, false));

        root.setChildren(buildMenuTree(menus));
        System.out.println(root);
        System.out.println(objectMapper.writeValueAsString(root));
    }

    List<RouterVO> buildMenuTree(List<SysMenu> menus) {
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
            meta.setHidden(!"1".equals(menu.getVisible()));
            meta.setKeepAlive("1".equals(menu.getKeepAlive()));
            route.setMeta(meta);

            // 递归处理子路由
            if (CollectionUtils.isNotEmpty(menu.getChildren())) {
                List<RouterVO> children = buildMenuTree(menu.getChildren());
                route.setChildren(children);
            }else {
                route.setChildren(new ArrayList<>());
            }
            routers.add(route);
        });
        return routers;
    }

//    @Test
//    void test011() {
//        String path = "D:\\ProjectManagement\\terra_vue3_springBoot3\\terra_admin\\src\\main\\resources\\物料类别所有数据 (1).xlsx";
////        System.out.println("你好");
//        List<MdmModelCategoryImportVO> lists = EasyExcel.read(path)
//                .head(MdmModelCategoryImportVO.class)
//                .sheet()
//                .doReadSync();
//
//        Map<String, MdmModelCategoryImportVO> nodeMap =
//                lists.stream()
//                        .collect(Collectors.toMap(
//                                MdmModelCategoryImportVO::getCategoryCode,
//                                Function.identity()
//                        ));
//        for (MdmModelCategoryImportVO node : lists) {
//
//            node.setIsLeaf("0"); // 默认没有子节点
//
//            node.setChildren(new ArrayList<>());
//        }
////        nodeMap.forEach((k,v)->{System.out.println(k+"==="+v);});
//        List<MdmModelCategoryImportVO> roots = new ArrayList<>();
//
//        for (MdmModelCategoryImportVO node : lists) {
//            String code = node.getCategoryCode();
//            if (code.length() == 2) {
//                roots.add(node);
//                continue;
//            }
//
//            String parentCode =
//                    code.substring(0, code.length() - 2);
//            node.setPid(parentCode);
//
//            MdmModelCategoryImportVO parent =
//                    nodeMap.get(parentCode);
//
//            if (parent != null) {
//                parent.getChildren().add(node);
//                // 父节点有子节点
//                parent.setIsLeaf("1");
//            }
//        }
//        roots.forEach(System.out::println);
//    }

//    @Test
//    void test02() {
//        String upperCase = UUID.randomUUID().toString().toUpperCase();
//        System.out.println(upperCase);
//        authService.validatePassword("admin123", "admin", upperCase);
//    }

    @Test
    void test01() {
//        List<SysMenuVO> menuList = sysMenuMapper.findMenuTreeByUserId(null);

//        Map<Long, SysMenuVO> menuMap = menuList.stream()
//                .collect(Collectors.toMap(
//                        SysMenuVO::getMenuId,
//                        menu -> menu));
//        List<SysMenuVO> rootMenus = new ArrayList<>();
//        for (SysMenuVO menu : menuList) {
//            if (menu.getParentId() == 0) {
//                rootMenus.add(menu);
//                continue;
//            }
//            SysMenuVO parent = menuMap.get(menu.getParentId());
//            if (parent != null) {
//                if (parent.getChildren() == null) {
//                    parent.setChildren(new ArrayList<>());
//                }
//
//                parent.getChildren().add(menu);
//            }
//        }
//        rootMenus.sort(
//                Comparator.comparing(SysMenuVO::getMenuSort)
//        );
//        rootMenus.forEach(System.out::println);

    }
    /*
    @Test
    void menu() {
        List<SysMenu> menuList = sysMenuMapper.findMenuTreeByUserId(null);
        List<SysMenu> sysMenuList = buildTree(menuList, 0L);
//        printTree(sysMenuList);
        List<RouterVO> routerList = buildRouters(sysMenuList);
        routerList.forEach(System.out::println);
//        System.out.println(routerList);
    }
    RouterVO buildRouterNode(SysMenu menu) {
        RouterVO router = new RouterVO();
        router.setPath(menu.getPath());
        router.setName(StringUtils.capitalize(menu.getPath()));
        router.setVisible(menu.getVisible());
        router.setMeta(
                new MetaVo(
                        menu.getMenuName(),
                        menu.getIcon()
                )
        );
        return router;
    }

    List<RouterVO> buildRouters(List<SysMenu> menuList){
        List<RouterVO> routers = new ArrayList<>();
        for (SysMenu menu : menuList) {
            RouterVO router = new RouterVO();
            // 处理一级菜单，并且是组件
            if (Objects.equals(menu.getParentId(), 0L)
                    && "C".equals(menu.getMenuType())) {
                router.setPath("/");
                router.setComponent("Layout");
                List<RouterVO> vo = new ArrayList<>();
                RouterVO routerVO = buildRouterNode(menu);
                routerVO.setComponent(menu.getComponent());
                vo.add(routerVO);
                router.setChildren(vo);
                routers.add(router);
                continue;
            }
            router = buildRouterNode(menu);
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
            if (menu.getComponent() == null && "M".equals(menu.getMenuType())) {
                router.setComponent("Layout");
            }else {
                router.setComponent(menu.getComponent());
            }

            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                router.setChildren(buildRouters(menu.getChildren()));
            }
            routers.add(router);
        }
        return routers;
    }

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


    public void printTree(List<SysMenu> menus) {
        for (int i = 0; i < menus.size(); i++) {
            printNode(menus.get(i), "", i == menus.size() - 1);
        }
    }

    private void printNode(SysMenu menu, String prefix, boolean isLast) {

        System.out.println(
                prefix +
                        (isLast ? "└── " : "├── ") +
                        menu.getMenuName()
        );

        List<SysMenu> children = menu.getChildren();

        if (children == null || children.isEmpty()) {
            return;
        }

        String childPrefix =
                prefix +
                        (isLast ? "    " : "│   ");

        for (int i = 0; i < children.size(); i++) {
            printNode(
                    children.get(i),
                    childPrefix,
                    i == children.size() - 1
            );
        }
    }

    */

}