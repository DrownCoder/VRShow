package com.study.xuan.vrshow.callback;

import com.study.xuan.vrshow.model.Model;

public interface onReadListener {
    void onstart();

    void onLoading(int cur, int total);

    void onFinished(Model model);

    void onFailure(Exception e);
}