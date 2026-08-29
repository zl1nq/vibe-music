package com.vibemusic.controller;


import com.vibemusic.result.Result;
import com.vibemusic.service.IPlaylistBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/playlist-binding")
public class PlaylistBindingController {

    @Autowired
    private IPlaylistBindingService playlistBindingService;

    /**
     * 把歌曲加入歌单
     *
     * @param playlistId 歌单 id
     * @param songId     歌曲 id
     * @return 添加结果
     */
    @PostMapping("/addSong")
    public Result addSong(@RequestParam Long playlistId, @RequestParam Long songId) {
        return playlistBindingService.addSongToPlaylist(playlistId, songId);
    }

    /**
     * 把歌曲移出歌单
     *
     * @param playlistId 歌单 id
     * @param songId     歌曲 id
     * @return 移除结果
     */
    @DeleteMapping("/removeSong")
    public Result removeSong(@RequestParam Long playlistId, @RequestParam Long songId) {
        return playlistBindingService.removeSongFromPlaylist(playlistId, songId);
    }
}
