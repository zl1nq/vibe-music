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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

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
                                // 必须开启增量输出，否则 DashScope 会把整段回复一次性返回，前端无法流式展示
                                .incrementalOutput(true)
                                .build()
                )
                // 注册 VibeAgent 可调用的音乐数据工具
                .defaultTools(musicAgentTools)
                .build();
    }

    /**
     * VibeAgent 聊天接口，供前端聊天窗使用（SSE 流式输出）
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AgentChatDTO dto, HttpServletResponse response) {
        log.info("chatController chat");
        response.setCharacterEncoding("UTF-8");
        // 禁止代理/容器缓冲，保证流式逐段到达
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(0L);
        Flux<String> content = this.chatClient.prompt(dto.getQuery())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, dto.getConversationId())
                ).stream().content();
        streamToEmitter(content, emitter);
        return emitter;
    }

    /**
     * 把流式内容逐段推送到 SseEmitter（每次 send 都会立即 flush 到客户端）
     */
    private void streamToEmitter(Flux<String> content, SseEmitter emitter) {
        content.subscribe(
                delta -> {
                    try {
                        emitter.send(SseEmitter.event().name("text").data(delta));
                    } catch (IOException e) {
                        log.warn("SSE 客户端已断开: {}", e.getMessage());
                        emitter.complete();
                    }
                },
                error -> {
                    log.error("agent 对话流式输出失败", error);
                    emitter.completeWithError(error);
                },
                emitter::complete
        );
    }

    /**
     * ChatClient 简单调用（GET，便于手动测试）
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {

        return chatClient.prompt(query).call().content();
    }

    /**
     * ChatClient 流式调用（GET，便于手动测试，curl -N 观察逐段输出）
     */
    @GetMapping("/stream/chat")
    public SseEmitter streamChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query,
                                 HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(0L);
        streamToEmitter(chatClient.prompt(query).stream().content(), emitter);
        return emitter;
    }
}
