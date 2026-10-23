package com.vibemusic.agent;

import com.vibemusic.model.vo.SongVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录每轮对话中工具真实查到的歌曲，供流结束时补写 <vibe-songs> 标签。
 * <p>
 * 模型并不总能按提示词要求输出标签，前端歌曲卡片依赖这个标签渲染，
 * 因此由后端在流末尾兜底注入，保证标签格式和数据一定正确。
 */
@Component
public class SongResultStore {

    /** toolContext 中传递会话 id 的 key */
    public static final String CONVERSATION_ID_KEY = "conversationId";

    /** 标签内歌曲数上限，与提示词约定一致 */
    private static final int MAX_SONGS = 5;

    private final Map<String, List<SongVO>> store = new ConcurrentHashMap<>();

    /** 新一轮对话开始前清掉上一轮的记录 */
    public void reset(String conversationId) {
        store.remove(conversationId);
    }

    public void add(String conversationId, List<SongVO> songs) {
        if (conversationId == null || songs == null || songs.isEmpty()) {
            return;
        }
        store.compute(conversationId, (id, existing) -> {
            List<SongVO> merged = existing == null ? new ArrayList<>(songs) : existing;
            if (existing != null) {
                merged.addAll(songs);
            }
            return merged;
        });
    }

    /** 取出并清空该会话本轮记录的歌曲，最多 {@value MAX_SONGS} 首 */
    public List<SongVO> take(String conversationId) {
        List<SongVO> songs = store.remove(conversationId);
        if (songs == null) {
            return List.of();
        }
        return songs.size() <= MAX_SONGS ? songs : new ArrayList<>(songs.subList(0, MAX_SONGS));
    }
}
