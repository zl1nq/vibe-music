package com.vibemusic.service.impl;

import com.vibemusic.constant.MessageConstant;
import com.vibemusic.enumeration.BannerStatusEnum;
import com.vibemusic.mapper.BannerMapper;
import com.vibemusic.model.dto.BannerDTO;
import com.vibemusic.model.entity.Banner;
import com.vibemusic.model.vo.BannerVO;
import com.vibemusic.result.PageResult;
import com.vibemusic.result.Result;
import com.vibemusic.service.IBannerService;
import com.vibemusic.service.MinioService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Service
@CacheConfig(cacheNames = "bannerCache")
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService {

    @Autowired
    private BannerMapper bannerMapper;
    @Autowired
    private MinioService minioService;

    /**
     * 获取轮播图列表
     *
     * @return 轮播图列表
     */
    @Override
    @Cacheable(key = "'bannerList' + #bannerDTO.pageNum + #bannerDTO.pageSize + #bannerDTO.bannerStatus")
    public Result<PageResult<Banner>> getAllBanners(BannerDTO bannerDTO) {
        // 分页查询
        Page<Banner> page = new Page<>(bannerDTO.getPageNum(), bannerDTO.getPageSize());
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        if (bannerDTO.getBannerStatus() != null) {
            queryWrapper.eq("status", bannerDTO.getBannerStatus().getId());
        }
        // 倒序排序
        queryWrapper.orderByDesc("id");

        IPage<Banner> bannerPage = bannerMapper.selectPage(page, queryWrapper);
        if (bannerPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(bannerPage.getTotal(), bannerPage.getRecords()));
    }

    /**
     * 添加轮播图
     *
     * @param bannerUrl 轮播图URL
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public Result addBanner(String bannerUrl) {
        Banner banner = new Banner();
        banner.setBannerUrl(bannerUrl).setBannerStatus(BannerStatusEnum.ENABLE);

        if (bannerMapper.insert(banner) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 更新轮播图
     *
     * @param bannerId  轮播图ID
     * @param bannerUrl 轮播图URL
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public Result updateBanner(Long bannerId, String bannerUrl) {
        if (bannerId == null || bannerUrl == null) {
            return Result.error(MessageConstant.ERROR);
        }
        Banner banner = bannerMapper.selectById(bannerId);
        String oldBannerUrl = banner.getBannerUrl();
        if (oldBannerUrl != null && !oldBannerUrl.isEmpty()) {
            minioService.deleteFile(oldBannerUrl);
        }

        banner.setBannerUrl(bannerUrl);
        if (bannerMapper.updateById(banner) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新轮播图状态
     *
     * @param bannerId     轮播图ID
     * @param bannerStatus 轮播图状态
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public Result updateBannerStatus(Long bannerId, Integer bannerStatus) {
        // 确保轮播图状态有效
        BannerStatusEnum statusEnum;
        if (bannerStatus == 0) {
            statusEnum = BannerStatusEnum.ENABLE;
        } else if (bannerStatus == 1) {
            statusEnum = BannerStatusEnum.DISABLE;
        } else {
            return Result.error(MessageConstant.BANNER_STATUS_INVALID);
        }

        // 更新轮播图状态
        Banner banner = new Banner();
        banner.setBannerId(bannerId).setBannerStatus(statusEnum);

        if (bannerMapper.updateById(banner) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);

    }

    /**
     * 删除轮播图
     *
     * @param bannerId 轮播图ID
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public Result deleteBanner(Long bannerId) {
        Banner banner = bannerMapper.selectById(bannerId);
        if (banner == null) {
            return Result.error(MessageConstant.DATA_NOT_FOUND);
        }
        String bannerUrl = banner.getBannerUrl();
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            minioService.deleteFile(bannerUrl);
        }

        if (bannerMapper.deleteById(bannerId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 轮播图ID列表
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public Result deleteBanners(List<Long> bannerIds) {
        List<Banner> banners = bannerMapper.selectByIds(bannerIds);
        List<String> bannerUrlList = banners.stream()
                .map(Banner::getBannerUrl)
                .filter(url -> url != null && !url.isEmpty())
                .toList();
        bannerUrlList.forEach(url -> minioService.deleteFile(url));

        if (bannerMapper.deleteByIds(bannerIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 获取轮播图列表（用户端）
     *
     * @return 轮播图列表
     */
    @Override
    @Cacheable(key = "'bannerList'")
    public Result<List<BannerVO>> getBannerList() {
        // 获取最后九个有效的轮播图
        List<Banner> banners = bannerMapper.selectList(new QueryWrapper<Banner>()
                .eq("status", BannerStatusEnum.ENABLE.getId())
                .orderByDesc("id")
                .last("limit 9"));

        // 转换为VO
        List<BannerVO> bannerVOList = banners.stream()
                .map(banner -> {
                    BannerVO bannerVO = new BannerVO();
                    BeanUtils.copyProperties(banner, bannerVO);
                    return bannerVO;
                }).toList();

        return Result.success(bannerVOList);
    }

}
