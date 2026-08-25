package com.vibemusic.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum FavoriteTypeEnum {

    SONG(0, "歌曲收藏"),
    PLAYLIST(1, "歌单收藏");

    @EnumValue
    private final Integer id;
    private final String favoriteType;

    FavoriteTypeEnum(Integer id, String favoriteType) {
        this.id = id;
        this.favoriteType = favoriteType;
    }

}
