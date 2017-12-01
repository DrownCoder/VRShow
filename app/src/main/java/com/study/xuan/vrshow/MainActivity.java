package com.study.xuan.vrshow;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
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
    private double lastSpacing;
    private double downSpacing;
    private double upSpacing;

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
        angle = new RotateModel();
        lastAngle = new RotateModel();
        lastSpacing = 1.0;//屏幕宽度对应一倍大小
    }

    private void initEvent() {
        glView.setOnTouchListener(onTouchListener);
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            double curSapcing = lastSpacing;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //绕x轴移动
                    angle.xAngle = (x - lastX) / wWidth * 180.0f;
                    //绕y轴移动
                    angle.yAngle = (y - lastY) / wHeight * 180.0f;
                    angle.xAngle = (angle.xAngle + lastAngle.xAngle) % 360.0f;
                    angle.yAngle = (angle.yAngle + lastAngle.yAngle) % 360.0f;
                    rotate(angle);
                    break;
                case MotionEvent.ACTION_UP:
                    lastAngle.xAngle = angle.xAngle;
                    lastAngle.yAngle = angle.yAngle;
                    lastAngle.zAngle = angle.zAngle;
                    lastX = x;
                    lastY = y;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    downSpacing = spacing(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    upSpacing = spacing(event);
                    Log.i("差值", (upSpacing - downSpacing) + "");
                    if ((upSpacing - downSpacing) > 0) {//放大
                        curSapcing += (upSpacing - downSpacing) / (wWidth / 2);
                    } else {//缩小
                        curSapcing -= (upSpacing - downSpacing) / (wWidth / 2);
                    }
                    Log.i("差值", "curSpacing:" + curSapcing);

                    scale(curSapcing * glRenderer.getStandScale());
                    lastSpacing = curSapcing;
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


    /*private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (timestamp != 0) {
                        final float dT = (event.timestamp - timestamp) * NS2S;
                        angle[0] += event.values[0] * dT;
                        angle[1] += event.values[1] * dT;
                        angle[2] += event.values[2] * dT;
                        float anglex = (float) Math.toDegrees(angle[0]);
                        float angley = (float) Math.toDegrees(angle[1]);
                        float anglez = (float) Math.toDegrees(angle[2]);
                        Sensordt info = new Sensordt();
                        info.setSensorX(angley);
                        info.setSensorY(anglex);
                        info.setSensorZ(anglez);
                        Message msg = new Message();
                        msg.what = 101;
                        msg.obj = info;
                        mHandler.sendMessage(msg);
                    }
                    timestamp = event.timestamp;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
        .SENSOR_DELAY_FASTEST);
    }*/

    public void rotate(RotateModel angle) {
        glRenderer.rotate(angle);
        glView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (glView != null) {
            glView.onResume();

            //不断改变rotateDegreen值，实现旋转
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            sleep(100);

                            rotateDegreen += 5;
                            handler.sendEmptyMessage(0x001);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.start();
        }*/


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
        }
    }

}
