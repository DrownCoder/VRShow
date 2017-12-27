package com.study.xuan.gifshow.widget.stlview.operate;


import com.study.xuan.gifshow.widget.stlview.callback.OnReadListener;
import com.study.xuan.gifshow.widget.stlview.model.STLModel;

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
