package com.study.xuan.vrshow;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.study.xuan.vrshow.model.RotateModel;
import com.study.xuan.vrshow.util.ScreenUtil;

public class MainActivity extends AppCompatActivity {
    private boolean supportsEs2;
    private GLSurfaceView glView;
    private float rotateDegreen = 0;
    private GLRenderer glRenderer;
    private EditText editText;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private SensorEventListener sensorEventListener;
    //单指旋转
    private float lastX = 0.0f;
    private float lastY = 0.0f;
    private float wHeight;
    private float wWidth;
    private RotateModel angle;
    private RotateModel lastAngle;
    //双指缩放
    private int mode;
    private double lastScale;
    private double downSpacing;
    private double upSpacing;
    //传感器
    private float timestamp;
    private final float[] tempAngle = new float[3];
    // 创建常量，把纳秒转换为秒。
    private static final float NS2S = 1.0f / 1000000000.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkSupported();
        if (supportsEs2) {
            setContentView(R.layout.activity_main);
            glView = (GLSurfaceView) findViewById(R.id.glview);
            glRenderer = new GLRenderer(this);
            glView.setRenderer(glRenderer);
            editText = (EditText) findViewById(R.id.et);
        } else {
            setContentView(R.layout.activity_main);
            Toast.makeText(this, "当前设备不支持OpenGL ES 2.0!", Toast.LENGTH_SHORT).show();
        }
        init();
        initEvent();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    //rotate(Float.valueOf(s.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void init() {
        wHeight = ScreenUtil.getWindowDisplay(this).getHeight();
        wWidth = ScreenUtil.getWindowDisplay(this).getWidth();
        Log.i("差值", "宽："+wWidth+"高："+wHeight);
        angle = new RotateModel();
        lastAngle = new RotateModel();
        lastScale = glRenderer.getStandScale();//初始大小
    }

    private void initEvent() {
        glView.setOnTouchListener(onTouchListener);
        initSensor();
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            double curScale = lastScale;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    sensorManager.unregisterListener(sensorEventListener);
                    mode = 1;
                    lastX = x;
                    lastY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
                            .SENSOR_DELAY_FASTEST);
                    mode = 0;
                    lastAngle.xAngle = angle.xAngle;
                    lastAngle.yAngle = angle.yAngle;
                    lastAngle.zAngle = angle.zAngle;
                    lastX = x;
                    lastY = y;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    sensorManager.unregisterListener(sensorEventListener);
                    mode += 1;
                    downSpacing = spacing(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
                            .SENSOR_DELAY_FASTEST);
                    mode = -1;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mode == -1) return true;
                    if (mode >= 2) {//双指
                        upSpacing = spacing(event);
                        Log.i("差值", Math.abs(upSpacing - downSpacing) + "");
                        if ((upSpacing - downSpacing) > 0) {//放大
                            curScale += Math.abs(upSpacing - downSpacing) / wHeight;
                        } else {//缩小
                            curScale -= Math.abs(upSpacing - downSpacing) / wHeight;
                        }
                        if (curScale < 1) {
                            curScale = 1;
                        }
                        if (curScale > 3) {
                            curScale = 3;
                        }
                        Log.i("缩放值", "curScale:" + curScale);
                        scale(curScale * glRenderer.getStandScale());
                        lastScale = curScale;
                        downSpacing = upSpacing;
                    } else {//单指
                        //绕x轴移动
                        angle.xAngle = (x - lastX) / wWidth * 180.0f;
                        //绕y轴移动
                        angle.yAngle = (y - lastY) / wHeight * 180.0f;
                        angle.xAngle = (angle.xAngle + lastAngle.xAngle) % 360.0f;
                        angle.yAngle = (angle.yAngle + lastAngle.yAngle) % 360.0f;
                        rotate(angle);
                    }
                    break;
            }
            return true;
        }
    };

    private void scale(double v) {
        glRenderer.scale((float) v);
        glView.invalidate();
    }

    /**
     * 计算两点之间的距离
     */
    private double spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }


    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (timestamp != 0) {
                        final float dT = (sensorEvent.timestamp - timestamp) * NS2S;
                        angle.yAngle += sensorEvent.values[0] * dT * 180.0f % 360.0f;
                        angle.xAngle += sensorEvent.values[1] * dT * 180.0f % 360.0f;
                        angle.zAngle += sensorEvent.values[2] * dT * 180.0f % 360.0f;
                        rotate(angle);
                    }
                    timestamp = sensorEvent.timestamp;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
        .SENSOR_DELAY_FASTEST);
    }

    public void rotate(RotateModel angle) {
        glRenderer.rotate(angle);
        glView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

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
        if (glView != null) {
            glView.onPause();
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

}
