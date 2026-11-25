package com.ziheng.ai.infrastructure.knowledge.parser;
/**
 * 文档解析器
 */
public interface  DocumentParser {

    /**
     * 解析文档
     *
     * @param filePath 文档存储路径
     * @return 文档解析结果
     */
    ParsedDocument parse(String filePath);

    /**
     * 判断当前解析器是否支持该文件格式
     *
     * @param fileExt 文件扩展名
     * @return 是否支持
     */
    boolean supports(String fileExt);

}
