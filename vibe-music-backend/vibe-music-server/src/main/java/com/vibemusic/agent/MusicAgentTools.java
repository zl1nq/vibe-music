package com.vibemusic.agent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vibemusic.mapper.ArtistMapper;
import com.vibemusic.mapper.SongMapper;
import com.vibemusic.mapper.StyleMapper;
import com.vibemusic.model.entity.Artist;
import com.vibemusic.model.entity.Song;
import com.vibemusic.model.entity.Style;
import com.vibemusic.model.vo.SongVO;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * VibeAgent 可以调用的音乐数据工具，让模型能够查询真实的曲库。
 */
@Component
public class MusicAgentTools {

    @Autowired
    private SongMapper songMapper;
    @Autowired
    private StyleMapper styleMapper;
    @Autowired
    private ArtistMapper artistMapper;
    @Autowired
    private SongResultStore songResultStore;

    /** 从 toolContext 里取会话 id，把工具查到的真实歌曲记录下来，供流末尾兜底补标签 */
    private void record(String conversationId, List<SongVO> songs) {
        songResultStore.add(conversationId, songs);
    }

    private static String conversationId(ToolContext context) {
        Object id = context == null ? null : context.getContext().get(SongResultStore.CONVERSATION_ID_KEY);
        return id == null ? null : id.toString();
    }

    /**
     * 按关键词搜索歌曲，关键词可以是歌名、歌手名或专辑名
     */
    @Tool(description = "按关键词搜索歌曲，关键词可以是歌名、歌手名或专辑名，返回歌曲列表。例如：'海阔天空'、'周杰伦'、'七里香'")
    public List<SongVO> searchSongs(@ToolParam(description = "搜索关键词") String keyword, ToolContext context) {
        List<SongVO> songs = songMapper.searchSongsByKeyword(keyword, 10);
        record(conversationId(context), songs);
        return songs;
    }

    /**
     * 根据音乐风格推荐歌曲
     */
    @Tool(description = "根据音乐风格推荐歌曲，风格如：流行、摇滚、民谣、电子、爵士、嘻哈、古典、R&B 等，返回该风格下的歌曲列表")
    public List<SongVO> recommendSongsByStyle(@ToolParam(description = "音乐风格名称") String style, ToolContext context) {
        QueryWrapper<Song> queryWrapper = new QueryWrapper<Song>()
                .like("style", style)
                .last("LIMIT 10");
        List<Song> songs = songMapper.selectList(queryWrapper);
        List<SongVO> result = fillArtistNames(songs);
        record(conversationId(context), result);
        return result;
    }

    /**
     * 获取平台的推荐歌曲（随机热门歌曲，20 首）
     */
    @Tool(description = "获取平台的推荐歌曲，返回 20 首随机热门歌曲")
    public List<SongVO> getHotSongs(ToolContext context) {
        List<SongVO> songs = songMapper.getRandomSongsWithArtist();
        record(conversationId(context), songs);
        return songs;
    }

    /**
     * 获取平台支持的音乐风格列表
     */
    @Tool(description = "获取平台支持的音乐风格名称列表")
    public List<String> listStyles() {
        return styleMapper.selectList(null).stream()
                .map(Style::getName)
                .toList();
    }

    /**
     * 为按风格查询出的歌曲补充歌手名
     */
    private List<SongVO> fillArtistNames(List<Song> songs) {
        if (songs.isEmpty()) {
            return List.of();
        }
        Set<Long> artistIds = songs.stream()
                .map(Song::getArtistId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> artistNameMap = artistIds.isEmpty() ? Map.of()
                : artistMapper.selectBatchIds(artistIds).stream()
                        .collect(Collectors.toMap(Artist::getArtistId, Artist::getArtistName));

        return songs.stream().map(song -> {
            SongVO vo = new SongVO();
            BeanUtils.copyProperties(song, vo);
            vo.setArtistName(artistNameMap.get(song.getArtistId()));
            return vo;
        }).toList();
    }

}
