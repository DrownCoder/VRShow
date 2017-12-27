package com.study.xuan.gifshow.widget.stlview.callback;

import com.study.xuan.gifshow.widget.stlview.model.STLModel;

public interface OnReadListener {
    void onstart();

    void onLoading(int cur, int total);

    void onFinished(STLModel model);

    void onFailure(Exception e);
}