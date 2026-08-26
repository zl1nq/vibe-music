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
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import static com.vibemusic.agent.AgentPrompt.DEFAULT_PROMPT;

@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {


    private final ChatClient chatClient;

    public AgentController(ChatClient.Builder chatClientBuilder,
                           MusicAgentTools musicAgentTools) {
        log.info("AgentController constructor");
        this.chatClient = chatClientBuilder
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

        Flux<String> content = this.chatClient.prompt(dto.getQuery())
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

        return chatClient.prompt(query).call().content();
    }

    /**
     * ChatClient 流式调用（GET，便于手动测试）
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query, HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt(query).stream().content();
    }
}
