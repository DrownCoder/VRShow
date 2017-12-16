package com.study.xuan.vrshow;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity{
    private boolean supportsEs2;
    private TextView mTvGif;
    private TextView mTvStl;
    private TextView mTvGoogle;
    private TextView mTvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        /*checkSupported();
        if (supportsEs2) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main);
            Toast.makeText(this, "当前设备不支持OpenGL ES 2.0!", Toast.LENGTH_SHORT).show();
        }*/

    }

    private void initView() {
        mTvGif = (TextView) findViewById(R.id.gif);
        mTvStl = (TextView) findViewById(R.id.stl);
        mTvGoogle = (TextView) findViewById(R.id.google);
    }

    private void initEvent() {
        mTvGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GifActivity.class);
                startActivity(intent);
            }
        });

        mTvStl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, STLActivity.class);
                startActivity(intent);
            }
        });

        mTvGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GoogleActivity.class);
                startActivity(intent);
            }
        });
        /*stlView.setRotate(true);
        stlView.setScale(true);
        stlView.setSensor(true);
        stlView.setOnReadCallBack(new OnReadCallBack() {
            @Override
            public void onStart() {
                mTvProgress.setText("开始解析！");
            }

            @Override
            public void onReading(int cur, int total) {
                bundle.putInt("cur", cur);
                bundle.putInt("total", total);
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onFinish() {
                mTvProgress.setText("解析完成！");
            }
        });*/
    }


    /*private onReadListener readListener = new onReadListener() {
        @Override
        public void onstart() {
            mTvProgress.setText("开始解析！");
        }

        @Override
        public void onLoading(int cur, int total) {
            bundle.putInt("cur", cur);
            bundle.putInt("total", total);
            Message msg = new Message();
            msg.setData(bundle);
            handler.sendMessage(msg);
        }

        @Override
        public void onFinished(STLModel model) {
            mTvProgress.setText("解析完成！");
            if (model != null) {
                if (stlView == null) {
                    stlView = new STLView(MainActivity.this,model);
                    container.addView(stlView);
                } else {
                    stlView.setNewSTLObject(model);
                }
            }
        }

        @Override
        public void onFailure(Exception e) {

        }
    };*/


    private void checkSupported() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        supportsEs2 = configurationInfo.reqGlEsVersion >= 0x2000;

        boolean isEmulator = Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"));

        supportsEs2 = supportsEs2 || isEmulator;
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (stlView != null) {
            stlView.onPause();
        }*/
    }
}
