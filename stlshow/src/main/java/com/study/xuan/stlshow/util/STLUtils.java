package com.study.xuan.stlshow.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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

    public static FloatBuffer floatToBuffer(float[] a) {
        //先初始化buffer，数组的长度*4，因为一个float占4个字节
        ByteBuffer bb = ByteBuffer.allocateDirect(a.length * 4);
        //数组排序用nativeOrder
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(a);
        buffer.position(0);
        return buffer;
    }

    public static int byte4ToInt(byte[] bytes, int offset) {
        int b3 = bytes[offset + 3] & 0xFF;
        int b2 = bytes[offset + 2] & 0xFF;
        int b1 = bytes[offset + 1] & 0xFF;
        int b0 = bytes[offset + 0] & 0xFF;
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

    public static short byte2ToShort(byte[] bytes, int offset) {
        int b1 = bytes[offset + 1] & 0xFF;
        int b0 = bytes[offset + 0] & 0xFF;
        return (short) ((b1 << 8) | b0);
    }

    public static float byte4ToFloat(byte[] bytes, int offset) {

        return Float.intBitsToFloat(byte4ToInt(bytes, offset));
    }

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Convert <code>input</code> stream into byte[].
     *
     * @param input
     * @return Array of Byte
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * Copy <code>length</code> size of <code>input</code> stream to <code>output</code> stream.
     * This method will NOT close input and output stream.
     *
     * @param input
     * @param output
     * @return long copied length
     * @throws IOException
     */
    private static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Copy <code>length</code> size of <code>input</code> stream to <code>output</code> stream.
     *
     * @param input
     * @param output
     * @return long copied length
     * @throws IOException
     */
    public static long copy(InputStream input, OutputStream output, int length) throws IOException {
        byte[] buffer = new byte[length];
        int count = 0;
        int n = 0;
        int max = length;
        while ((n = input.read(buffer, 0, max)) != -1) {
            output.write(buffer, 0, n);
            count += n;
            if (count > length) {
                break;
            }

            max -= n;
            if (max <= 0) {
                break;
            }
        }
        return count;
    }

    /**
     * Close <code>closeable</code> quietly.
     *
     * @param closeable
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Throwable e) {
            System.out.println("文件关闭失败");
        }
    }
}
