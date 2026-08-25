package com.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_comment")
public class Comment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 歌曲 id
     */
    @TableField("song_id")
    private Long songId;

    /**
     * 歌单 id
     */
    @TableField("playlist_id")
    private Long playlistId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 评论类型：0-歌曲评论，1-歌单评论
     */
    @TableField("type")
    private Integer type;

    /**
     * 点赞数量
     */
    @TableField("like_count")
    private Long likeCount;

}
