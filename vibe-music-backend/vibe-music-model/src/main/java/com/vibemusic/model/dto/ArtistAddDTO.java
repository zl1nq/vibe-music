package com.vibemusic.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ArtistAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手姓名
     */
    private String artistName;

    /**
     * 歌手性别：0-男，1-女
     */
    private Integer gender;


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

}
