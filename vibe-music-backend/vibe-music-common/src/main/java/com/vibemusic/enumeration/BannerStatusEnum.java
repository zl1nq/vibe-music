package com.vibemusic.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum BannerStatusEnum {

    ENABLE(0, "启用"),
    DISABLE(1, "禁用");

    @EnumValue
    private final Integer id;
    private final String bannerStatus;

    BannerStatusEnum(Integer id, String bannerStatus) {
        this.id = id;
        this.bannerStatus = bannerStatus;
    }

}
