package com.ziheng.system.sysoperationlog.mask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OperateLogDataMasker {
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "salt"
    );

    private final ObjectMapper objectMapper;

    public OperateLogDataMasker(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 操作日志数据脱敏
     */
    public JsonNode mask(Object data) {
        if (data == null) {
            return null;
        }
        JsonNode node = objectMapper.valueToTree(data);
        maskNode(node);
        return node;
    }

    /**
     * 递归处理 JSON
     */
    private void maskNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldValue = objectNode.get(fieldName);
                if (SENSITIVE_FIELDS.contains(fieldName)) {
                    objectNode.put(fieldName, "******");
                    return;
                }
                maskNode(fieldValue);
            });

            return;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                maskNode(item);
            }
        }
    }
}

