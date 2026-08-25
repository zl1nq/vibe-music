package com.vibemusic.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlaylistAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单标题
     */
    private String title;

    /**
     * 歌单简介
     */
    private String introduction;

    /**
     * 歌单风格
     */
    private String style;

}
