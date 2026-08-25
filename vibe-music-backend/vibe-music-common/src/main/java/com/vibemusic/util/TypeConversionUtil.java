package com.vibemusic.util;

public class TypeConversionUtil {

    /**
     * 将 Object 转换为 Long，支持 Long 和 Integer 类型的转换。
     *
     * @param obj 要转换的对象
     * @return 转换后的 Long 值
     * @throws IllegalArgumentException 如果对象类型不支持转换
     */
    public static Long toLong(Object obj) {
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else {
            throw new IllegalArgumentException("转换失败，不支持类型：" + obj.getClass());
        }
    }

}
