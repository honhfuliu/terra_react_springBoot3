package com.ziheng.framework.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 绑定连接工厂，RedisTemplate 本身不管理连接
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 这里的构造参数见 createObjectMapper()，序列化规则是配置的核心
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(createObjectMapper());

        // key 与 Hash 的 field 都用普通字符串
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value 与 Hash 的 value 都用 JSON
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 校验并初始化序列化器。Bean 交给容器创建时本来也会调用一次，
        // 这里显式调用是让"序列化器已配置完成"这件事在代码里明确，重复调用是幂等的。
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Redis 专用的 ObjectMapper。
     *
     * <p>刻意不复用 Spring MVC 那个 ObjectMapper，因为两边诉求不同：
     * Web 层关心的是"前端好不好用"，缓存层关心的是"取出来还得是原来那个对象"。</p>
     *
     * <p>整体只做四件事：字段可见性、反序列化白名单、类型信息、时间格式。
     * 每一项的取舍见方法内注释。</p>
     *
     * @return 供 Redis JSON 序列化使用的 ObjectMapper
     */
    private ObjectMapper createObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        /*
         * 字段可见性
         *
         * Jackson 默认只识别 public 的字段和 getter，
         * 而本项目的实体（SysUser、SysRole 等）是私有字段 + Lombok 生成的 getter。
         * 放开为 ANY 之后，即使某个字段忘了加 getter，也能被正常序列化，
         * 避免出现"字段莫名丢失、缓存对象不完整"这类难查的问题。
         */
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        /*
         * 反序列化白名单（本类最关键的配置）
         *
         * GenericJackson2JsonRedisSerializer 默认使用 LaissezFaireSubTypeValidator，
         * 它的语义是"JSON 里的类型信息写的是哪个类，就实例化哪个类"，等于完全放开。
         * 一旦 Redis 中的数据被人工篡改，理论上就给了攻击者一个任意类实例化的入口。
         *
         * 换成正向白名单后，只有下面这些类型允许被还原：
         *
         *   com.ziheng.  本项目自己的实体、VO、DTO
         *   java.util.   ArrayList、LinkedHashMap、HashSet、Date 等集合与日期
         *   java.sql.    Timestamp 等，MyBatis 映射时间字段时可能出现
         *   java.lang.   String、Integer、Long 等包装类型
         *   java.math.   BigDecimal，金额等精确数值
         *   java.time.   LocalDateTime、LocalDate 等 Java 8 时间类型
         *   NullValue    Spring Cache 缓存 null 时的占位对象，@Cacheable 缓存空值必需
         *
         * 使用时务必记住两点：
         *   1) 白名单只在反序列化（读取）时校验，写入不受任何限制。
         *      所以把不在白名单里的对象写进 Redis 不会立刻报错，而是读取时才抛异常；
         *   2) 以后新增的缓存对象如果不在 com.ziheng 包下（例如某个第三方 SDK 的类），
         *      必须回到这里补一行，否则读取阶段会失败。
         */
        BasicPolymorphicTypeValidator validator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.ziheng.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.sql.")
                        .allowIfSubType("java.lang.")
                        .allowIfSubType("java.math.")
                        .allowIfSubType("java.time.")
                        .allowIfSubType(NullValue.class)
                        /*
                         * 数组需要单独放行。
                         *
                         * 数组的类型信息是 JVM 描述符形式，例如：
                         *   [Lcom.ziheng.system.sysuser.domain.SysUser;
                         * 以 "[L" 开头，匹配不上上面任何一个"包名前缀"，
                         * 不显式放行的话，缓存 SysUser[] 时读取会抛 InvalidTypeIdException，
                         * 而写入时毫无异常，属于典型的"写行读不行"。
                         */
                        .allowIfSubTypeIsArray()
                        .build();

        /*
         * 开启 Default Typing（即写入类型信息）
         *
         * 为什么必须开启：
         *   RedisTemplate 声明为 RedisTemplate<String, Object>，
         *   泛型在运行时被擦除，反序列化的目标类型永远是 Object.class。
         *   Spring Data Redis 的 resolveType() 在这种情况下的逻辑是：
         *     类型信息已开启 -> 依据 JSON 中的 @class 还原成真实类型
         *     类型信息未开启 -> 直接按 Object 读成 LinkedHashMap（不会报错）
         *   也就是说，不开的话业务侧 (SysUser) get(key) 会在运行时抛 ClassCastException，
         *   而编译期完全没有提示。
         *
         * 三个参数分别的含义：
         *   validator: 上面定义的白名单，决定"允许还原哪些类"，这是安全边界所在。
         *   ObjectMapper.DefaultTyping.NON_FINAL
         *       只有非 final 类型才写入类型信息。
         *       String、Integer 这类 final 的自然类型不写，JSON 更小更干净，
         *       比 Spring Data 默认使用的 EVERYTHING 更克制。
         *       代价：final 实体类或 record 不会被写入类型信息，读取时会退化成 Map，
         *       所以以后若出现这类缓存对象，需要改回 DefaultTyping.EVERYTHING，
         *       或者去掉该类上的 final。
         *   JsonTypeInfo.As.PROPERTY
         *       类型信息以普通属性的形式写入，即 {"@class": "..."}。
         *       对 List、Map 这类本身没有属性的结构，Jackson 会自动退化为
         *       ["java.util.ArrayList", [...]] 的包装数组形式，
         *       这是正常表现，与 Spring Data Redis 官方默认行为一致。
         */
        objectMapper.activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        /*
         * Java 8 时间类型
         * 不注册这个模块，一旦缓存对象里出现 LocalDateTime、LocalDate，
         * 会直接抛 InvalidDefinitionException: Java 8 date/time type not supported by default。
         * 当前实体统一使用 java.util.Date，暂时不会触发，
         */
        objectMapper.registerModule(new JavaTimeModule());

        // 时间输出为 ISO-8601 字符串（2026-09-10T10:00:00），便于在 redis-cli 中直接阅读
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }
}
