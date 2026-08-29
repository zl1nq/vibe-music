package com.vibemusic.service.impl;

import com.vibemusic.constant.MessageConstant;
import com.vibemusic.mapper.PlaylistBindingMapper;
import com.vibemusic.mapper.PlaylistMapper;
import com.vibemusic.mapper.SongMapper;
import com.vibemusic.model.entity.PlaylistBinding;
import com.vibemusic.result.Result;
import com.vibemusic.service.IPlaylistBindingService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Service
public class PlaylistBindingServiceImpl extends ServiceImpl<PlaylistBindingMapper, PlaylistBinding> implements IPlaylistBindingService {

    @Resource
    private PlaylistBindingMapper playlistBindingMapper;
    @Resource
    private PlaylistMapper playlistMapper;
    @Resource
    private SongMapper songMapper;

    /**
     * 把歌曲加入歌单
     *
     * @param playlistId 歌单 id
     * @param songId     歌曲 id
     * @return 添加结果
     */
    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result addSongToPlaylist(Long playlistId, Long songId) {
        if (playlistId == null || songId == null) {
            return Result.error(MessageConstant.ERROR);
        }

        if (playlistMapper.selectById(playlistId) == null) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.NOT_EXIST);
        }
        if (songMapper.selectById(songId) == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_EXIST);
        }

        QueryWrapper<PlaylistBinding> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("playlist_id", playlistId).eq("song_id", songId);
        if (playlistBindingMapper.selectCount(queryWrapper) > 0) {
            return Result.error(MessageConstant.SONG_ALREADY_IN_PLAYLIST);
        }

        PlaylistBinding playlistBinding = new PlaylistBinding().setPlaylistId(playlistId).setSongId(songId);
        if (playlistBindingMapper.insert(playlistBinding) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 把歌曲移出歌单
     *
     * @param playlistId 歌单 id
     * @param songId     歌曲 id
     * @return 移除结果
     */
    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result removeSongFromPlaylist(Long playlistId, Long songId) {
        if (playlistId == null || songId == null) {
            return Result.error(MessageConstant.ERROR);
        }

        if (playlistMapper.selectById(playlistId) == null) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.NOT_EXIST);
        }
        if (songMapper.selectById(songId) == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_EXIST);
        }

        QueryWrapper<PlaylistBinding> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("playlist_id", playlistId).eq("song_id", songId);
        if (playlistBindingMapper.delete(queryWrapper) == 0) {
            return Result.error(MessageConstant.SONG_NOT_IN_PLAYLIST);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }
}
