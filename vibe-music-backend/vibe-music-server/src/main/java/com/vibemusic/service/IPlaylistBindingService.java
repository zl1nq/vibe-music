package com.vibemusic.service;

import com.vibemusic.model.entity.PlaylistBinding;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vibemusic.result.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
public interface IPlaylistBindingService extends IService<PlaylistBinding> {

    /**
     * 把歌曲加入歌单
     *
     * @param playlistId 歌单 id
     * @param songId     歌曲 id
     * @return 添加结果
     */
    Result addSongToPlaylist(Long playlistId, Long songId);

    /**
     * 把歌曲移出歌单
     *
     * @param playlistId 歌单 id
     * @param songId     歌曲 id
     * @return 移除结果
     */
    Result removeSongFromPlaylist(Long playlistId, Long songId);

}
