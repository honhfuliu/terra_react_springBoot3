package com.ziheng.common.util.excel.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {

    /**
     * Excel 表头名称
     */
    String value();

    /**
     * 列顺序，从 0 开始
     */
    int order() default 0;

    /**
     * 列宽
     */
    int width() default 20;

    /**
     * 单元格格式，例如 yyyy-MM-dd
     */
    String format() default "";
}
