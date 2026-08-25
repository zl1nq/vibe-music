package com.vibemusic.model.dto;

import com.vibemusic.enumeration.BannerStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BannerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @NotNull
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull
    private Integer pageSize;

    /**
     * 轮播图状态：0-启用，1-禁用
     */
    private BannerStatusEnum bannerStatus;

}
