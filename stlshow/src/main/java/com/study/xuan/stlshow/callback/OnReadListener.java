package com.study.xuan.stlshow.callback;

import com.study.xuan.stlshow.model.STLModel;

public interface OnReadListener {
    void onstart();

    void onLoading(int cur, int total);

    void onFinished(STLModel model);

    void onFailure(Exception e);
}