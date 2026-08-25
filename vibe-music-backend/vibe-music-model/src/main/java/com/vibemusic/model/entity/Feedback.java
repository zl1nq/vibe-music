package com.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

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
@TableName("tb_feedback")
public class Feedback implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 反馈 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long feedbackId;

    /**
     * 用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 反馈内容
     */
    @TableField("feedback")
    private String feedback;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

}
