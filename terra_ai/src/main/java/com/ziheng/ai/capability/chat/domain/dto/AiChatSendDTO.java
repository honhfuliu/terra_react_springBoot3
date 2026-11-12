package com.ziheng.ai.capability.chat.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatSendDTO {

    /**
     * 会话ID
     * 第一次发送消息时为空
     */
    private Long sessionId;

    /**
     * 当前选择的模型ID
     */
    @NotNull(message = "请选择AI模型")
    private Long modelId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 20000, message = "消息内容不能超过20000个字符")
    private String message;
}
