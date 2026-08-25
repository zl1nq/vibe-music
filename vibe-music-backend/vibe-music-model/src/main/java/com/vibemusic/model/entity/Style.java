package com.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.io.Serializable;
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
@TableName("tb_style")
public class Style implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 风格 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long styleId;

    /**
     * 风格名称
     */
    @TableField("name")
    private String name;

}
