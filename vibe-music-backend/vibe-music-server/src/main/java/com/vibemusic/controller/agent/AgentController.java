package com.vibemusic.controller.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemusic.agent.MusicAgentTools;
import com.vibemusic.agent.SongResultStore;
import com.vibemusic.model.dto.AgentChatDTO;
import com.vibemusic.model.vo.SongVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.vibemusic.agent.AgentPrompt.DEFAULT_PROMPT;

@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {


    private final ChatClient chatClient;
    private final SongResultStore songResultStore;
    private final ObjectMapper objectMapper;

    public AgentController(ChatClient.Builder chatClientBuilder,
                           MusicAgentTools musicAgentTools,
                           SongResultStore songResultStore,
                           ObjectMapper objectMapper) {
        log.info("AgentController constructor");
        this.songResultStore = songResultStore;
        this.objectMapper = objectMapper;
        this.chatClient = chatClientBuilder
                .defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build(),
                        new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(
                        ZhiPuAiChatOptions.builder()
                                .topP(0.7)
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
        // 本轮对话工具查到的歌曲重新开始记录
        songResultStore.reset(dto.getConversationId());
        Flux<String> content = this.chatClient.prompt(dto.getQuery())
                .toolContext(Map.of(SongResultStore.CONVERSATION_ID_KEY, dto.getConversationId()))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, dto.getConversationId())
                ).stream().content();
        streamToEmitter(content, emitter, fullText -> sendFallbackSongsTag(dto.getConversationId(), fullText, emitter));
        return emitter;
    }

    /**
     * 把流式内容逐段推送到 SseEmitter（每次 send 都会立即 flush 到客户端）。
     * 流结束时把完整回复交给 onComplete，用于补写兜底标签。
     */
    private void streamToEmitter(Flux<String> content, SseEmitter emitter, Consumer<String> onComplete) {
        StringBuilder fullText = new StringBuilder();
        content.subscribe(
                delta -> {
                    fullText.append(delta);
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
                () -> {
                    if (onComplete != null) {
                        onComplete.accept(fullText.toString());
                    }
                    emitter.complete();
                }
        );
    }

    /**
     * 模型没有按约定输出 <vibe-songs> 标签时，用本轮工具记录的真实歌曲补写一个，
     * 保证前端歌曲卡片一定能渲染（glm-4-flash 对标签格式的遵从并不稳定）。
     */
    private void sendFallbackSongsTag(String conversationId, String fullText, SseEmitter emitter) {
        try {
            boolean modelEmittedTag = fullText.contains("<vibe-songs") || fullText.contains("[vibe-songs");
            List<SongVO> songs = songResultStore.take(conversationId);
            if (modelEmittedTag || songs.isEmpty()) {
                return;
            }
            String json = objectMapper.writeValueAsString(songs);
            emitter.send(SseEmitter.event().name("text").data("\n<vibe-songs>" + json + "</vibe-songs>"));
        } catch (Exception e) {
            log.warn("补写 vibe-songs 标签失败: {}", e.getMessage());
        }
    }

    /**
     * ChatClient 简单调用（GET，便于手动测试）
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {

        return chatClient.prompt(query)
                .toolContext(Map.of(SongResultStore.CONVERSATION_ID_KEY, "test-simple"))
                .call().content();
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
        streamToEmitter(chatClient.prompt(query)
                .toolContext(Map.of(SongResultStore.CONVERSATION_ID_KEY, "test-stream"))
                .stream().content(), emitter, null);
        return emitter;
    }
}
