package com.ziheng.system.sysoperationlog.annotation;

import com.ziheng.system.sysoperationlog.constant.IdSource;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 业务模块
     */
    OperateModule module();

    /**
     * 操作类型
     */
    OperateType type();

    /**
     * ID 来源
     */
    IdSource idSource() default IdSource.AUTO;

    /**
     * ID 字段名称
     */
    String idField() default "id";

    /**
     * 业务Service
     */
    Class<?> service();

    /**
     * 是否记录请求参数
     */
    boolean recordRequest() default true;

    /**
     * 是否记录操作前数据
     */
    boolean recordBefore() default true;

    /**
     * 是否记录操作后数据
     */
    boolean recordAfter() default true;
}
