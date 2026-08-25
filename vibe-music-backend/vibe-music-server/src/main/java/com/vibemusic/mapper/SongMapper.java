package com.vibemusic.mapper;

import com.vibemusic.model.entity.Song;
import com.vibemusic.model.vo.SongAdminVO;
import com.vibemusic.model.vo.SongDetailVO;
import com.vibemusic.model.vo.SongVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Mapper
public interface SongMapper extends BaseMapper<Song> {

    // 获取歌曲列表
    @Select("""
                SELECT 
                    s.id AS songId, 
                    s.name AS songName, 
                    s.album, 
                    s.duration, 
                    s.cover_url AS coverUrl, 
                    s.audio_url AS audioUrl, 
                    s.release_time AS releaseTime, 
                    a.name AS artistName
                FROM tb_song s
                LEFT JOIN tb_artist a ON s.artist_id = a.id
                WHERE 
                    (#{songName} IS NULL OR s.name LIKE CONCAT('%', #{songName}, '%'))
                    AND (#{artistName} IS NULL OR a.name LIKE CONCAT('%', #{artistName}, '%'))
                    AND (#{album} IS NULL OR s.album LIKE CONCAT('%', #{album}, '%'))
            """)
    IPage<SongVO> getSongsWithArtist(Page<SongVO> page,
                                     @Param("songName") String songName,
                                     @Param("artistName") String artistName,
                                     @Param("album") String album);

    // 获取歌曲列表
    @Select("""
                SELECT 
                    s.id AS songId, 
                    s.name AS songName, 
                    s.artist_id AS artistId, 
                    s.album, 
                    s.lyric, 
                    s.duration, 
                    s.style, 
                    s.cover_url AS coverUrl, 
                    s.audio_url AS audioUrl, 
                    s.release_time AS releaseTime, 
                    a.name AS artistName
                FROM tb_song s
                LEFT JOIN tb_artist a ON s.artist_id = a.id
                WHERE 
                    (#{artistId} IS NULL OR s.artist_id = #{artistId})
                    AND(#{songName} IS NULL OR s.name LIKE CONCAT('%', #{songName}, '%'))
                    AND (#{album} IS NULL OR s.album LIKE CONCAT('%', #{album}, '%'))
                ORDER BY s.release_time DESC
            """)
    IPage<SongAdminVO> getSongsWithArtistName(Page<SongAdminVO> page,
                                              @Param("artistId") Long artistId,
                                              @Param("songName") String songName,
                                              @Param("album") String album);

    // 获取随机歌曲列表
    @Select("""
                SELECT 
                    s.id AS songId, 
                    s.name AS songName, 
                    s.album, 
                    s.duration, 
                    s.cover_url AS coverUrl, 
                    s.audio_url AS audioUrl, 
                    s.release_time AS releaseTime, 
                    a.name AS artistName
                FROM tb_song s
                LEFT JOIN tb_artist a ON s.artist_id = a.id
                ORDER BY RAND() LIMIT 20
            """)
    List<SongVO> getRandomSongsWithArtist();

    // 按关键词搜索歌曲（关键词命中歌名/歌手/专辑任一即可，OR 逻辑）
    @Select("""
                SELECT 
                    s.id AS songId, 
                    s.name AS songName, 
                    s.album, 
                    s.duration, 
                    s.cover_url AS coverUrl, 
                    s.audio_url AS audioUrl, 
                    s.release_time AS releaseTime, 
                    a.name AS artistName
                FROM tb_song s
                LEFT JOIN tb_artist a ON s.artist_id = a.id
                WHERE #{keyword} IS NOT NULL AND #{keyword} <> ''
                  AND (s.name LIKE CONCAT('%', #{keyword}, '%')
                       OR a.name LIKE CONCAT('%', #{keyword}, '%')
                       OR s.album LIKE CONCAT('%', #{keyword}, '%'))
                ORDER BY s.release_time DESC
                LIMIT #{limit}
            """)
    List<SongVO> searchSongsByKeyword(@Param("keyword") String keyword,
                                      @Param("limit") int limit);

    // 根据id获取歌曲详情
    SongDetailVO getSongDetailById(Long songId);

    // 根据用户收藏的歌曲id列表获取歌曲列表
    IPage<SongVO> getSongsByIds(Page<SongVO> page,
                                @Param("songIds") List<Long> songIds,
                                @Param("songName") String songName,
                                @Param("artistName") String artistName,
                                @Param("album") String album);

    // 根据用户收藏的歌曲id列表获取歌曲列表
    List<Long> getFavoriteSongStyles(@Param("favoriteSongIds") List<Long> favoriteSongIds);

    // 根据用户收藏的歌曲id列表获取歌曲列表
    List<SongVO> getRecommendedSongsByStyles(@Param("sortedStyleIds") List<Long> sortedStyleIds,
                                             @Param("favoriteSongIds") List<Long> favoriteSongIds,
                                             @Param("limit") int limit);

}
