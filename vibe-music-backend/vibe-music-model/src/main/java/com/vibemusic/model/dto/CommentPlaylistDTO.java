package com.vibemusic.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CommentPlaylistDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单id
     */
    private Long playlistId;

    /**
     * 评论内容
     */
    private String content;

}
