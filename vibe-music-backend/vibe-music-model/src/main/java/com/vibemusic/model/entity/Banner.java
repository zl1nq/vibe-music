package com.vibemusic.model.entity;

import com.vibemusic.enumeration.BannerStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_banner")
public class Banner implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 轮播图 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long bannerId;

    /**
     * 轮播图 url
     */
    @TableField("banner_url")
    private String bannerUrl;

    /**
     * 轮播图状态：0-启用，1-禁用
     */
    @TableField("status")
    private BannerStatusEnum bannerStatus;
}
