package com.vibemusic.result;


import com.vibemusic.constant.MessageConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后端统一返回结果
 *
 * @param <T>
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;   // 业务状态码  0-成功  1-失败
    private String message; // 提示信息
    private T data;         // 响应数据

    // 快速返回操作成功响应结果(默认提示信息)
    public static <T> Result<T> success(T data) {
        return new Result<>(0, MessageConstant.OPERATION + MessageConstant.SUCCESS, data);
    }

    // 快速返回操作成功响应结果(默认提示信息)
    public static Result success() {
        return new Result(0, MessageConstant.OPERATION + MessageConstant.SUCCESS, null);
    }

    // 快速返回操作失败响应结果(默认提示信息)
    public static Result error() {
        return new Result(1, MessageConstant.OPERATION + MessageConstant.FAILED, null);
    }

    // 快速返回操作成功响应结果(带响应数据和自定义提示信息)
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    // 快速返回操作成功响应结果(带自定义提示信息)
    public static Result success(String message) {
        return new Result(0, message, null);
    }

    // 快速返回操作失败响应结果(带自定义提示信息)
    public static Result error(String message) {
        return new Result(1, message, null);
    }

}
