package com.study.xuan.vrshow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.study.xuan.gifshow.widget.VrGifView;

public class GifActivity extends AppCompatActivity {
    private VrGifView mGif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);
        mGif = (VrGifView) findViewById(R.id.gif);
        mGif.setTouch(true);
        mGif.setDrag(true);
        mGif.setMoveMode(VrGifView.MODE_FAST);
        mGif.setScale(false);
    }
}
