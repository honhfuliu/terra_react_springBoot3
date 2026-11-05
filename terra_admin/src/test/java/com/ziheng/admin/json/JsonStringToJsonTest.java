package com.ziheng.admin.json;

import cn.hutool.json.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JsonStringToJsonTest {
    @Autowired
    private ObjectMapper objectMapper;

//    /**
//     * 将 JSON 中嵌套的 JSON 字符串转换成 JSON 对象
//     */
//     JsonNode convert(String json) throws Exception {
//
//        JsonNode root = objectMapper.readTree(json);
//
//        return convertNode(root);

    @Test
    void Test(){
        System.out.println("测试运行");
    }
}
