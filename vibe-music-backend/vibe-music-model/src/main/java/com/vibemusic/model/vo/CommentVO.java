package com.vibemusic.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class CommentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论 id
     */
    private Long commentId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;

    /**
     * 点赞数量
     */
    private Long likeCount;

}
