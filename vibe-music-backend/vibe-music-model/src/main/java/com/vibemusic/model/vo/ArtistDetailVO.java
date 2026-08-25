package com.vibemusic.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class ArtistDetailVO implements Serializable {

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
     * 歌手性别：0-男，1-女
     */
    private Integer gender;

    /**
     * 歌手头像
     */
    private String avatar;

    /**
     * 歌手出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    /**
     * 歌手所处地区
     */
    private String area;

    /**
     * 歌手简介
     */
    private String introduction;

    /**
     * 歌曲列表
     */
    private List<SongVO> songs;

}
