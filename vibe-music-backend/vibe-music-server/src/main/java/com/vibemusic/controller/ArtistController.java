package com.vibemusic.controller;


import com.vibemusic.model.dto.ArtistDTO;
import com.vibemusic.model.vo.ArtistDetailVO;
import com.vibemusic.model.vo.ArtistVO;
import com.vibemusic.result.PageResult;
import com.vibemusic.result.Result;
import com.vibemusic.service.IArtistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/artist")
public class ArtistController {

    @Autowired
    private IArtistService artistService;

    /**
     * 获取所有歌手列表
     *
     * @param artistDTO artistDTO
     * @return 歌手列表
     */
    @PostMapping("/getAllArtists")
    public Result<PageResult<ArtistVO>> getAllArtists(@RequestBody @Valid ArtistDTO artistDTO) {
        return artistService.getAllArtists(artistDTO);
    }

    /**
     * 获取随机歌手
     * 随机歌手的数量为 10
     *
     * @return 随机歌手列表
     */
    @GetMapping("/getRandomArtists")
    public Result<List<ArtistVO>> getRandomArtists() {
        return artistService.getRandomArtists();
    }

    /**
     * 获取歌手详情
     *
     * @param artistId 歌手id
     * @return 歌手详情
     */
    @GetMapping("/getArtistDetail/{id}")
    public Result<ArtistDetailVO> getArtistDetail(@PathVariable("id") Long artistId, HttpServletRequest request) {
        return artistService.getArtistDetail(artistId, request);
    }

}
