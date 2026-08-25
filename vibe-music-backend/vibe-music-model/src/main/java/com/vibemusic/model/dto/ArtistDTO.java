package com.vibemusic.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ArtistDTO implements Serializable {

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
     * 歌手姓名
     */
    private String artistName;

    /**
     * 歌手性别：0-男，1-女
     */
    private Integer gender;

    /**
     * 歌手所处地区
     */
    private String area;

}
