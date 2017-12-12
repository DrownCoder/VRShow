package com.study.xuan.vrshow.operate;

import com.study.xuan.vrshow.callback.onReadListener;
import com.study.xuan.vrshow.model.Model;

import java.io.InputStream;

/**
 * Author : xuan.
 * Date : 2017/12/10.
 * Description : interface of stlreader
 */

public interface ISTLReader {
    public Model parserBinStl(byte[] bytes);

    public Model parserBinStl(InputStream in);

    public Model parserAsciiStl(byte[] bytes);

    public Model parserAsciiStl(InputStream in);

    public void setCallBack(onReadListener listener);
}
