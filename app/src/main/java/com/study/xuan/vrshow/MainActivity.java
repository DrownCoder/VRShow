package com.study.xuan.vrshow;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.study.xuan.vrshow.callback.OnReadCallBack;
import com.study.xuan.vrshow.widget.STLView;

public class MainActivity extends AppCompatActivity{
    private boolean supportsEs2;
    private STLView stlView;
    private FrameLayout container;
    private TextView mTvProgress;
    private Bundle bundle = new Bundle();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int cur = bundle.getInt("cur");
            int total = bundle.getInt("total");
            mTvProgress.setText(cur + "/" + total);
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkSupported();
        if (supportsEs2) {
            setContentView(R.layout.activity_main);
            container = (FrameLayout) findViewById(R.id.container);
            stlView = (STLView) findViewById(R.id.stlview);
            mTvProgress = (TextView) findViewById(R.id.progress);
            initEvent();
        } else {
            setContentView(R.layout.activity_main);
            Toast.makeText(this, "当前设备不支持OpenGL ES 2.0!", Toast.LENGTH_SHORT).show();
        }

    }

    private void initEvent() {
        stlView.setTouch(true);
        stlView.setRotate(true);
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
        });
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
        if (stlView != null) {
            stlView.onPause();
        }
    }
}
