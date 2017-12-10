package com.study.xuan.vrshow.util;

/**
 * Author : xuan.
 * Date : 2017/12/10.
 * Description : STL文件相关工具类
 */
public class STLUtils {
    /**
     * 判断STL文件格式是否是Ascii格式
     */
    public static boolean isAscii(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0x0a || b == 0x0d || b == 0x09) {
                continue;
            }
            if (b < 0x20 || (0xff & b) >= 0x80) {
                return false;
            }
        }
        return true;
    }
}
