package com.vibemusic.controller.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.vibemusic.agent.MusicAgentTools;
import com.vibemusic.model.dto.AgentChatDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import static com.vibemusic.agent.AgentPrompt.DEFAULT_PROMPT;

@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {


    private final ChatClient dashScopeChatClient;

    public AgentController(ChatClient.Builder chatClientBuilder,
                           MusicAgentTools musicAgentTools) {
        log.info("AgentController constructor");
        this.dashScopeChatClient = chatClientBuilder
                .defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build(),
                        new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .topP(0.7)
                                .build()
                )
                // 注册 VibeAgent 可调用的音乐数据工具
                .defaultTools(musicAgentTools)
                .build();
    }

    /**
     * VibeAgent 聊天接口，供前端聊天窗使用
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody AgentChatDTO dto, HttpServletResponse response) {
        log.info("chatController chat");
        response.setCharacterEncoding("UTF-8");

        Flux<String> content = this.dashScopeChatClient.prompt(dto.getQuery())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, dto.getConversationId())
                ).stream().content();

        return content.map(delta -> ServerSentEvent.<String>builder()
                .event("text")
                .data(delta)
                .build());
    }

    /**
     * ChatClient 简单调用（GET，便于手动测试）
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {

        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * ChatClient 流式调用（GET，便于手动测试）
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query, HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");
        return dashScopeChatClient.prompt(query).stream().content();
    }

    /**
     * ChatClient 使用自定义的 Advisor 实现功能增强（带会话记忆）.
     */
    @GetMapping("/advisor/chat/{conversationId}")
    public Flux<String> advisorChat(
            HttpServletResponse response,
            @PathVariable String conversationId,
            @RequestParam String query
    ) {

        response.setCharacterEncoding("UTF-8");

        return this.dashScopeChatClient.prompt(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)
                ).stream().content();
    }

    /**
     * ChatClient 新的聊天接口，支持流式输出和自定义 ChatOptions 配置
     */
    @GetMapping("/advisor/newChat")
    public Flux<String> newChat(
            HttpServletResponse response,
            @RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query,
            @RequestParam(value = "topP", required = false) Double topP,
            @RequestParam(value = "temperature", required = false) Double temperature,
            @RequestParam(value = "maxTokens", required = false) Integer maxToken) {

        response.setCharacterEncoding("UTF-8");

        // 构建 ChatOptions
        DashScopeChatOptions.DashScopeChatOptionsBuilder optionsBuilder = DashScopeChatOptions.builder();

        if (topP != null) {
            optionsBuilder.topP(topP);
        }
        if (temperature != null) {
            optionsBuilder.temperature(temperature);
        }
        if (maxToken != null) {
            optionsBuilder.maxToken(maxToken);
        }

        return this.dashScopeChatClient.prompt(query)
                .options(optionsBuilder.build())
                .stream()
                .content();
    }
}
