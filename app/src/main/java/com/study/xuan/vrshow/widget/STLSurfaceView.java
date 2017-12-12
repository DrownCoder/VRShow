package com.study.xuan.vrshow.widget;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.study.xuan.vrshow.callback.onCreateCallBack;
import com.study.xuan.vrshow.callback.onReadListener;
import com.study.xuan.vrshow.model.Model;
import com.study.xuan.vrshow.model.RotateModel;
import com.study.xuan.vrshow.operate.ISTLReader;
import com.study.xuan.vrshow.operate.ReaderHandler;
import com.study.xuan.vrshow.operate.STLReader;
import com.study.xuan.vrshow.util.IOUtils;
import com.study.xuan.vrshow.util.ScreenUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Author : xuan.
 * Date : 2017/12/12.
 * Description :input the description of this file.
 */

public class STLSurfaceView extends GLSurfaceView {
    private Context mContext;
    private STLRenderer stlRenderer;
    private Model model;
    private File file;
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

    public STLSurfaceView(Context context) {
        super(context);
    }

    public STLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
        //stlRenderer.requestRedraw();
    }

    private void init() {
        wHeight = ScreenUtil.getWindowDisplay(mContext).getHeight();
        wWidth = ScreenUtil.getWindowDisplay(mContext).getWidth();
        angle = new RotateModel();
        lastAngle = new RotateModel();
        //lastScale = stlRenderer.getStandardScale();//初始大小
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        double curScale = lastScale;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //sensorManager.unregisterListener(sensorEventListener);
                mode = 1;
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                /*sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
                        .SENSOR_DELAY_FASTEST);*/
                mode = 0;
                lastAngle.xAngle = angle.xAngle;
                lastAngle.yAngle = angle.yAngle;
                lastAngle.zAngle = angle.zAngle;
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //sensorManager.unregisterListener(sensorEventListener);
                mode += 1;
                downSpacing = spacing(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /*sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
                        .SENSOR_DELAY_FASTEST);*/
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
                    scale(curScale * stlRenderer.getStandardScale());
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

    /**
     * 计算两点之间的距离
     */
    private double spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }

    public void setRenderer(STLRenderer renderer) {
        stlRenderer = renderer;
        super.setRenderer(renderer);
    }

    private void initSensor() {
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
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

    private void scale(double scale) {
        stlRenderer.setScale((float) scale);
        invalidate();
    }

    public void rotate(RotateModel angle) {
        rotate(angle);
        invalidate();
    }

    /**
     * 更新object 刷新界面
     */
    public void setNewModel(Model model) {
        stlRenderer.requestRedraw(model);
    }

    /**
     * 刷新界面
     */
    public void requestRedraw() {
        stlRenderer.requestRedraw();
    }

    public static class ReaderBuilder {
        private static final int TYPE_FILE = 0;
        private static final int TYPE_BYTE = 1;
        private ReaderHandler handler;
        private onReadListener listener;
        private File file;
        private byte[] bytes;
        private ISTLReader reader;
        private boolean hasSource;
        private int type;

        public ReaderBuilder Reader(ISTLReader reader) {
            this.reader = reader;
            return this;
        }

        public ReaderBuilder CallBack(onReadListener listener) {
            this.listener = listener;
            if (reader != null) {
                reader.setCallBack(listener);
            }
            return this;
        }

        public ReaderBuilder Byte(byte[] bytes) {
            hasSource = true;
            type = TYPE_BYTE;
            this.bytes = bytes;
            return this;
        }

        public ReaderBuilder File(File file) {
            type = TYPE_FILE;
            hasSource = true;
            this.file = file;
            return this;
        }

        public ReaderBuilder build() {
            if (!hasSource) {
                Log.e("VRShow", "has not set the source file!");
                return this;
            }
            handler = new ReaderHandler(reader, listener);
            try {
                switch (type) {
                    case TYPE_BYTE:
                        handler.read(bytes);
                        break;
                    case TYPE_FILE:
                        handler.read(IOUtils.toByteArray(new FileInputStream(file)));
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }
    }
}
