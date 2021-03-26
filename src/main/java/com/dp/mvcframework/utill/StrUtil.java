package com.dp.mvcframework.utill;

/**
 * @auther: liudaping
 * @description: 字符串工具类
 * @date: 2021-03-26
 * @since 1.0.0
 */
public class StrUtil {

    /**
     * 首字符小写
     *
     * @param simpleName
     * @return
     */
    public static String toLowFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
