package com.study.xuan.vrshow.operate;

import com.study.xuan.vrshow.callback.onReadListener;
import com.study.xuan.vrshow.model.STLModel;

import java.io.InputStream;

/**
 * Author : xuan.
 * Date : 2017/12/10.
 * Description : interface of stlreader
 */

public interface ISTLReader {
    public STLModel parserBinStl(byte[] bytes);

    public STLModel parserBinStl(InputStream in);

    public STLModel parserAsciiStl(byte[] bytes);

    public STLModel parserAsciiStl(InputStream in);

    public void setCallBack(onReadListener listener);
}
