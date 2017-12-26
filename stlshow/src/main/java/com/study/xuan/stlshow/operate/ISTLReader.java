package com.study.xuan.stlshow.operate;


import com.study.xuan.stlshow.callback.OnReadListener;
import com.study.xuan.stlshow.model.STLModel;

import java.io.InputStream;

/**
 * Author : xuan.
 * Date : 2017/12/10.
 * Description : interface of stlreader
 */

public interface ISTLReader {
    public STLModel parserBinStl(byte[] bytes);

    public STLModel parserAsciiStl(byte[] bytes);

    public void setCallBack(OnReadListener listener);
}
