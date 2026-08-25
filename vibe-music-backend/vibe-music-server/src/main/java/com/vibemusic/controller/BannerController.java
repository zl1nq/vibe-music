package com.vibemusic.controller;


import com.vibemusic.model.dto.BannerDTO;
import com.vibemusic.model.entity.Banner;
import com.vibemusic.model.vo.BannerVO;
import com.vibemusic.result.PageResult;
import com.vibemusic.result.Result;
import com.vibemusic.service.IBannerService;
import com.vibemusic.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
public class BannerController {

    @Autowired
    private IBannerService bannerService;
    @Autowired
    private MinioService minioService;

    /**
     * 获取轮播图列表
     *
     * @return 轮播图列表
     */
    @PostMapping("/admin/getAllBanners")
    public Result<PageResult<Banner>> getAllBanners(@RequestBody BannerDTO bannerDTO) {
        return bannerService.getAllBanners(bannerDTO);
    }

    /**
     * 添加轮播图
     *
     * @param banner 轮播图
     * @return 结果
     */
    @PostMapping("/admin/addBanner")
    public Result addBanner(@RequestParam("banner") MultipartFile banner) {
        String bannerUrl = minioService.uploadFile(banner, "banners");
        return bannerService.addBanner(bannerUrl);
    }

    /**
     * 更新轮播图
     *
     * @param banner 轮播图
     * @return 结果
     */
    @PatchMapping("/admin/updateBanner/{id}")
    public Result updateBanner(@PathVariable("id") Long bannerId, @RequestParam("banner") MultipartFile banner) {
        String bannerUrl = minioService.uploadFile(banner, "banners");
        return bannerService.updateBanner(bannerId, bannerUrl);
    }

    /**
     * 更新轮播图状态
     *
     * @param bannerStatus 轮播图状态
     * @return 结果
     */
    @PatchMapping("/admin/updateBannerStatus/{id}")
    public Result updateBannerStatus(@PathVariable("id") Long bannerId, @RequestParam("status") Integer bannerStatus) {
        return bannerService.updateBannerStatus(bannerId, bannerStatus);
    }

    /**
     * 删除轮播图
     *
     * @param bannerId 轮播图id
     * @return 结果
     */
    @DeleteMapping("/admin/deleteBanner/{id}")
    public Result deleteBanner(@PathVariable("id") Long bannerId) {
        return bannerService.deleteBanner(bannerId);
    }

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 轮播图id列表
     * @return 结果
     */
    @DeleteMapping("/admin/deleteBanners")
    public Result deleteBanners(@RequestBody List<Long> bannerIds) {
        return bannerService.deleteBanners(bannerIds);
    }

    /**
     * 获取轮播图列表（用户端）
     *
     * @return 轮播图列表
     */
    @GetMapping("/banner/getBannerList")
    public Result<List<BannerVO>> getBannerList() {
        return bannerService.getBannerList();
    }
}
