package com.study.xuan.vrshow.callback;

import com.study.xuan.vrshow.model.STLModel;

public interface onReadListener {
    void onstart();

    void onLoading(int cur, int total);

    void onFinished(STLModel model);

    void onFailure(Exception e);
}