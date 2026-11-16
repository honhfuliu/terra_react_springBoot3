package com.ziheng.admin;



import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ziheng.ai.infrastructure.security.AiApiKeyEncryptor;
import com.ziheng.ai.infrastructure.vector.domain.AiDocumentChunk;
import com.ziheng.ai.infrastructure.vector.domain.AiDocumentEmbedding;
import com.ziheng.ai.infrastructure.vector.domain.dto.AiChunkSearchResult;
import com.ziheng.ai.infrastructure.vector.mapper.AiDocumentEmbeddingMapper;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentChunkService;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentEmbeddingService;
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

    @Autowired
    private AiDocumentChunkService aiDocumentChunkService;

    @Autowired
    private AiDocumentEmbeddingService aiDocumentEmbeddingService;



    @Test
    void testVector() {
        double[] embeddingArray = {-0.011737576,-0.011041461,0.028443182,0.014226086,0.009117128,0.031703472,-0.033789992,-0.037628878,0.027134866,-0.091433235,-0.030375,-0.044319693,-0.0075546596,-0.04022824,0.06286631,0.03128528,0.07019534,0.005554069,-0.000743194,0.025783023,-0.07843374,-0.0038658606,0.028573515,0.039714478,-0.047504373,0.017945843,0.03562317,-0.05371664,-0.04824755,0.2617315,-0.019155929,-0.04708925,0.0243553,-0.0101209115,0.05374581,-0.009163019,-0.04860914,-0.045342326,0.015939444,-0.019467976,0.008954736,0.020706428,0.016654849,-0.008680039,0.009108401,-0.033605848,-0.010935979,-0.008237809,-0.030558322,0.00066523056,0.04695564,-0.042620517,-0.021153884,0.009341768,0.021891402,0.00041349544,0.013216215,-0.024996076,0.00952771,0.013008519,0.03843177,0.030972598,0.029369928,0.07497203,-0.04231836,-0.019132636,0.006504315,-0.043369483,0.071141094,-0.0036195973,0.028088102,0.0268034,0.023269353,0.009367714,-0.0715328,-0.022203017,-0.1288655,0.084412105,-0.02496582,0.019011723,-0.018896524,-0.061703254,-0.030067397,-0.013639198,0.003116965,-0.049403183,-0.0636732,0.029191092,-0.05580902,0.023446167,0.010056401,-0.013907044,-0.011766837,-0.0020510955,-0.000842366,-0.0578267,-0.05169286,-0.0639283,0.026430907,0.03648848,-0.020550286,0.043648805,0.03244668,-0.0026577043,-0.01729434,0.002835873,0.02687673,0.018106367,0.034880314,0.015028189,-0.011611395,-0.2891733,0.0038161166,0.023888327,-0.036159296,0.1689809,-0.018426675,-0.029820161,0.055856813,0.037530746,-0.04428972,0.082581945,0.008367617,-0.009048752,-0.029541045,0.036462024,0.03701186,0.006662767,-0.011723422,-0.004629311,0.01937747,0.05749362,0.017794948,-0.006914037,-0.010732209,-0.045533363,-0.010711016,-0.008411911,0.05744419,0.003269947,0.0124381175,-0.008669795,-0.017396092,-0.0037458148,-0.049119104,0.028373526,0.008716475,0.026563536,0.02488841,-0.040690597,0.00290776,-0.045246813,-0.036615558,0.0004324905,-0.017204596,-0.030253014,0.029590698,-0.028488295,-0.0047523538,-0.005283457,-0.0070366687,0.03182522,-0.013314644,0.037869688,-0.035079166,0.028110959,0.023289101,-0.013816517,-0.02223971,-0.010503579,-0.042977743,0.021470267,-0.009524441,0.021505944,0.058844507,0.005838085,-0.00724209,0.04266782,-0.011722984,0.01408462,-0.0037525936,-0.050686035,0.013297682,-0.02729125,-0.0060789497,0.00058969687,-0.03114577,-0.047519434,-0.09882841,-0.018273916,-0.04717896,0.029391842,-0.00236613,-0.023719184,0.011678789,-0.026768697,0.025278531,-0.04773274,-0.035613284,0.02605429,-0.025770368,0.0147071555,0.04310869,0.040926345,0.034930393,-0.059550732,0.035021376,0.020478787,-0.016785862,0.044474266,0.08096841,0.019883052,-0.035287034,-0.047065556,-0.010335677,0.052623946,-0.06520611,0.07159051,-0.0034758146,-0.029393986,0.0018281505,0.038576473,0.0045597404,0.037862528,0.0118902,0.01168397,0.085742995,-0.0009672894,-0.08166013,0.050114933,0.02546855,-0.0057464093,0.024320578,-0.0061369953,0.055929296,-0.04403958,-0.030052165,0.033374768,0.018998414,0.0743185,-0.027378099,-0.03836405,-0.011199102,-0.020724656,0.029823383,-0.048582576,0.034899935,0.00055534573,0.026570845,-0.0151462685,-0.018032469,-0.053469796,0.004305108,0.022675142,0.07255913,0.0034397738,0.020540081,0.010558056,0.016827876,0.000047285776,-0.004149404,-0.015310595,0.018185692,0.02138983,0.017516475,0.008434429,0.02432545,0.018228224,-0.003191324,-0.034714438,-0.016739726,0.03849471,0.0088482695,-0.034090865,0.010113629,0.015919775,-0.0029365602,-0.016246349,-0.0029496688,0.006080435,0.022218643,0.023522912,-0.02868748,-0.0007080589,-0.011384632,-0.019870294,-0.031245122,0.033213124,0.055065095,0.0066823326,-0.056994855,-0.007935385,-0.024820691,0.0002591648,-0.03308739,0.03564457,-0.038956456,-0.013776254,0.009734032,0.00090891466,0.020110345,0.049267203,-0.009712601,0.002535172,-0.008339902,0.040899567,-0.0045626108,-0.0088510215,0.00635485,0.0021826471,0.024472017,0.005195413,-0.005111619,-0.008732672,0.002947167,0.006899972,-0.014549978,0.00295679,-0.001707264,-0.10737995,-0.00063820987,0.005942396,-0.0018435487,0.017687999,0.008532762,-0.032686368,-0.011156447,0.014200183,0.002870262,-0.008614314,0.015169321,0.026956812,-0.019642686,0.03554629,0.005205779,-0.009272987,-0.032520417,-0.02377046,0.02710905,0.03504282,-0.0063095093,-0.15287805,-0.024704155,0.043764092,-0.026722645,-0.026640944,-0.04884078,-0.014623851,-0.021789134,0.03700632,0.027204957,0.037914224,-0.017246883,0.0046458067,0.00022178948,-0.0051954035,0.0018140378,-0.04404904,-0.011733084,0.023662254,-0.010623432,-0.029286763,-0.023462985,-0.017626295,-0.00090494164,0.022712585,-0.016319565,0.040559813,-0.02988562,0.0008552095,-0.016226906,-0.020229256,0.017707199,0.03063381,-0.019897763,0.062276248,0.0087934155,0.016016614,-0.013478453,-0.0004511554,0.015489822,-0.01042827,-0.040093526,0.009364227,0.031496204,-0.050569993,0.05911939,0.037320517,-0.035058983,0.06850216,0.020969968,0.17263032,-0.05493511,0.034896407,-0.054280672,0.032864694,-0.030578418,0.034956004,0.013941022,-0.058406442,0.009662531,0.0147928,0.06035081,-0.019809213,-0.011689467,0.022741837,0.016009547,-0.031435017,0.03260603,0.005718073,-0.005497796,-0.017420208,0.0127024725,-0.011483786,-0.010442993,-0.010905049,-0.061240315,-0.037217397,-0.04014106,0.034070525,-0.0018635913,0.010283137,0.019417608,0.0011110774,0.06089727,0.0036962721,-0.022944286,-0.016541481,0.0014377765,0.015693322,-0.0028340917,-0.014413018,0.0084659215,-0.04610923,0.007187982,-0.008917098,0.052527875,-0.03258459,-0.048965365,-0.04880332,0.0041816365,-0.035625927,-0.019348979,-0.030925058,-0.015073212,-0.011454838,0.011713316,-0.0016493271,-0.023631144,0.0008352588,-0.033407297,-0.002178386,-0.013962873,0.008641344,-0.00093788677,0.020615572,0.02709253,-0.043361146,0.009394285,0.029090844,0.012646529,-0.01878471,-0.009780692,0.020202711,0.023391953,-0.025018895,-0.011280165,-0.04880804,-0.033847913,-0.00017024136,-0.01866987,0.009393057,-0.049197998,-0.056404218,0.000256579,-0.0053196205,-0.005249027,-0.027569575,-0.03529858,0.016595576,0.039827883,-0.02585848,0.0032436661,-0.07291235,0.03096184,0.0027102076,0.0056353463,0.040060416,-0.021780442,0.019382846,-0.006162023,-0.02181451,-0.021219125,0.0077742315,0.0005490293,-0.017111098,0.030060578,-0.044035826,-0.0042988574,-0.032992836,0.015838733,0.030852506,-0.0009635274,0.010593261,0.01387462,0.034104645,-0.024521073,-0.18070856,0.0009488388,0.019230437,0.0011308348,-0.010932502,0.03931707,0.021047715,0.027486399,-0.039110318,-0.0018834887,-0.0005147278,0.00004952973,-0.020156622,-0.026170772,0.057596482,0.021312661,-0.021385314,-0.032013465,0.009710339,-0.018558811,-0.014114382,-0.029477332,0.012562086,0.030221539,0.055683464,-0.051894158,-0.008920439,-0.06240969,-0.033175442,-0.012185084,-0.019499443,0.012033533,-0.009105654,0.004015387,-0.030853916,0.0011682642,-0.019793425,0.003313812,0.0335371,0.01717969,0.03315179,0.016250592,0.02715295,0.007385024,-0.010373019,-0.024354583,0.0066163554,-0.16378269,0.012182122,-0.06294814,0.010419262,-0.053394567,-0.01561098,-0.030708482,-0.01543407,-0.0148391025,-0.04354449,-0.017930139,0.017964445,-0.06305649,-0.0041137356,-0.06933855,-0.0034223248,0.009839238,0.040014584,0.0015288398,-0.021586396,0.07767916,-0.016881296,0.002313906,-0.014252011,0.010105454,0.0010922507,0.0004937643,-0.020236002,0.002786999,0.023219766,-0.002197379,-0.00066038413,-0.004065843,-0.012960711,-0.041168954,0.04363732,-0.0032135183,-0.021683797,0.08026055,0.019489903,-0.016056491,-0.06865645,0.038489122,-0.012732365,0.008286648,0.010934288,0.017486816,-0.026418129,0.0023914874,0.016695527,-0.0029550942,-0.030822523,-0.016067717,-0.01637469,0.021084456,-0.026846956,0.015470617,-0.010179083,0.0014460775,0.008986717,-0.018391874,0.028538542,0.044799935,-0.01752354,0.02858182,-0.020648373,-0.048685774,-0.012269167,0.02286297,0.011369407,-0.02405835,-0.004590195,-0.01057337,0.019590171,-0.012017309,-0.024360487,-0.0123570515,-0.011986109,0.0007232599,0.0103438245,-0.001350402,0.009396537,-0.018960293,0.023750605,-0.033308648,0.00020849434,-0.015824163,-0.0007720949,-0.04672984,0.008334787,0.023026437,0.05195966,0.023406995,-0.0040414724,-0.016417341,0.002942263,-0.018427065,0.00095660175,0.012883934,-0.020444658,-0.017107738,0.006244953,-0.0158848,0.007070767,-0.035124145,0.00996937,-0.006831108,0.017197853,0.027491359,-0.016296903,-0.03258219,-0.017314505,0.032214094,0.022227341,0.008528422,0.007903508,-0.00298517,-0.020227162,0.034531184,-0.012637007,-0.017358176,-0.0043291706,0.0550158,-0.040398322,-0.019511491,-0.008073513,0.03347159,0.043166965,0.007193417,0.038669854,-0.011432035,0.03958137,0.007466011,-0.016818307,-0.053713486,0.036052257,0.03783336,0.042648025,0.018874152,-0.023520648,0.012135201,0.00465278,0.017237948,-0.043588195,0.04550541,-0.005899001,0.054563154,0.0018820714,-0.042102132,0.004984335,0.032559358,-0.00003042071,0.02350836,-0.008401894,0.06356334,0.0006103622,-0.010383417,-0.036335386,-0.007730572,-0.02582363,-0.014725379,0.040590998,-0.010109116,0.034170836,-0.008087031,0.011105219,-0.015919805,0.03815733,-0.02721588,0.038429517,-0.016463252,-0.022522798,-0.024944201,0.036810253,-0.026949912,0.025208961,0.01698296,0.019270757,0.15104593,0.03898921,-0.008435353,-0.029567236,-0.04588537,0.0069964454,0.02165415,-0.004016412,-0.012169779,-0.0087227905,0.006819044,0.025188603,0.004030452,-0.0050489986,-0.012994123,-0.016564455,0.024475863,-0.036337983,0.031248432,-0.014262056,-0.030885596,-0.008373426,-0.010982549,0.018122153,-0.04873356,0.008127737,-0.031057935,0.007913399,-0.037503727,-0.003455101,0.011865272,-0.005395621,-0.023120085,0.024955863,0.012126754,0.0066197375};
        float[] f = new float[embeddingArray.length];
        for (int i = 0; i < f.length; i++) f[i] = (float) embeddingArray[i];
        List<AiChunkSearchResult> aiChunkSearchResults = aiDocumentEmbeddingService.searchSimilar(1L, 1L, f, 3);
        aiChunkSearchResults.forEach(System.out::println);
//        AiDocumentChunk aiDocumentChunk = aiDocumentChunkService.insertTestChunk();
//        AiDocumentEmbedding aiDocumentEmbedding = aiDocumentEmbeddingService.insertTestEmbedding();
//        System.out.println(aiDocumentEmbedding.toString());
//        System.out.println(aiDocumentChunk.toString());

    }

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