package com.ziheng.ai.infrastructure.options;

import com.ziheng.ai.common.enums.AiModelType;
import com.ziheng.ai.common.enums.AiProtocolType;
import com.ziheng.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/options")
public class AiOptionsController {

    /**
     * 获取 AI 接入协议选项
     */
    @GetMapping("/protocols")
    @Operation(summary = "获取 AI 接入协议选项")
    public R<List<Map<String, String>>> getProtocols() {
        List<Map<String, String>> options = Arrays.stream(AiProtocolType.values())
                .map(item -> Map.of(
                        "label", item.getDescription(),
                        "value", item.getCode()
                ))
                .toList();
        return R.ok(options);
    }

    @GetMapping("/model-types")
    public R<List<Map<String, String>>> getModelTypes() {
        List<Map<String, String>> options = Arrays.stream(AiModelType.values())
                .map(item -> Map.of(
                        "dictLabel", item.getDescription(),
                        "dictValue", item.getCode()
                ))
                .toList();

        return R.ok(options);
    }
}
