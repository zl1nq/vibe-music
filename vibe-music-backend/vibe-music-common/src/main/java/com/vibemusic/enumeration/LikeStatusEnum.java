package com.vibemusic.enumeration;

import lombok.Getter;

@Getter
public enum LikeStatusEnum {

    DEFAULT(0, "默认"),
    LIKE(1, "喜欢");

    private final Integer id;
    private final String likeStatus;

    LikeStatusEnum(Integer id, String likeStatus) {
        this.id = id;
        this.likeStatus = likeStatus;
    }

}
