package com.vibemusic.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 与 VibeAgent 聊天的请求体
 */
@Data
public class AgentChatDTO {

    /**
     * 会话 id，用于区分不同用户的对话记忆
     */
    private String conversationId;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String query;

}
