package com.vibemusic.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CommentTypeEnum {

    SONG(0, "歌曲评论"),
    PLAYLIST(1, "歌单评论");

    @EnumValue
    private final Integer id;
    private final String commentType;

    CommentTypeEnum(Integer id, String commentType) {
        this.id = id;
        this.commentType = commentType;
    }

}
