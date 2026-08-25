package com.vibemusic.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ArtistVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 id
     */
    private Long artistId;

    /**
     * 歌手姓名
     */
    private String artistName;

    /**
     * 歌手头像
     */
    private String avatar;

}
