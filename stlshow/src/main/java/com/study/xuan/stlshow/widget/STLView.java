package com.study.xuan.stlshow.widget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.study.xuan.stlshow.callback.OnReadCallBack;
import com.study.xuan.stlshow.callback.OnReadListener;
import com.study.xuan.stlshow.model.STLModel;

/**
 * Author : xuan.
 * Date : 2017/12/10.
 * Description : 自定义展示器
 */

public class STLView extends GLSurfaceView {
    private STLRenderer stlRenderer;
    private Context mContext;
    private OnReadCallBack onReadCallBack;
    //控制缩放速度的
    static int CONTROL = 10;
    //双指缩放
    //这里将偏移数值降低
    private final float TOUCH_SCALE_FACTOR = 180.0f / 1080 / 2;
    private float previousX;
    private float previousY;
    // zoom rate (larger > 1.0f > smaller)
    private float pinchScale = 1.0f;
    private PointF pinchStartPoint = new PointF();
    private float pinchStartZ = 0.0f;
    private float pinchStartDistance = 0.0f;
    private float pinchMoveX = 0.0f;
    private float pinchMoveY = 0.0f;

    // for touch event handling
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_DRAG = 1;
    private static final int TOUCH_ZOOM = 2;
    private int touchMode = TOUCH_NONE;
    //传感器
    private float timestamp;
    // 创建常量，把纳秒转换为秒。
    private static final float NS2S = 1.0f / 1000000000.0f;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private SensorEventListener sensorEventListener;
    //感应开关
    private boolean isSensor;
    private boolean isTouch;
    private boolean isRotate;
    private boolean isScale;

    private STLModel modelData;


    public STLView(Context context) {
        this(context, null);
    }

    public STLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init() {
        SharedPreferences colorConfig = mContext.getSharedPreferences("colors", Activity.MODE_PRIVATE);
        STLRenderer.red = colorConfig.getFloat("red", 0.75f);
        STLRenderer.green = colorConfig.getFloat("green", 0.75f);
        STLRenderer.blue = colorConfig.getFloat("blue", 0.75f);
        STLRenderer.alpha = colorConfig.getFloat("alpha", 0.5f);
        stlRenderer = new STLRenderer(new STLModel());
        setRenderer(stlRenderer);
    }

    public void setOnReadCallBack(OnReadCallBack onReadCallBack) {
        this.onReadCallBack = onReadCallBack;
    }

    public OnReadListener getReadListener() {
        return readListener;
    }

    private final OnReadListener readListener = new OnReadListener() {
        @Override
        public void onstart() {
            if (onReadCallBack != null) {
                onReadCallBack.onStart();
            }
        }

        @Override
        public void onLoading(int cur, int total) {
            if (onReadCallBack != null) {
                onReadCallBack.onReading(cur, total);
            }
        }

        @Override
        public void onFinished(STLModel model) {
            modelData = model;
            if (isSensor) {
                initSensor();
            }
            if (onReadCallBack != null) {
                onReadCallBack.onFinish();
            }
            stlRenderer.requestRedraw(model);
        }

        @Override
        public void onFailure(Exception e) {

        }
    };

    private void changeDistance(float scale) {
        stlRenderer.scale = scale;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTouch) {
            return true;
        }
        //双指缩放
        if (isScale) {
            zoomScale(event);
        }
        //单指旋转
        if (isRotate) {
            rotateModel(event);
        }
        return true;
    }

    /**
     * 单指旋转model
     */
    private void rotateModel(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // start drag
            case MotionEvent.ACTION_DOWN:
                registerSensor(false);
                if (touchMode == TOUCH_NONE && event.getPointerCount() == 1) {
                    touchMode = TOUCH_DRAG;
                    previousX = event.getX();
                    previousY = event.getY();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_DRAG) {
                    float x = event.getX();
                    float y = event.getY();

                    float dx = x - previousX;
                    float dy = y - previousY;
                    //一次只移动一个方向
                    previousX = x;
                    previousY = y;

                    if (isRotate) {
                        if (Math.abs(dx) > Math.abs(dy)) {
                            stlRenderer.angleX = (stlRenderer.angleX + dx * TOUCH_SCALE_FACTOR) %
                                    360.0f;
                        } else {
                            stlRenderer.angleY = (stlRenderer.angleY + dy * TOUCH_SCALE_FACTOR) %
                                    360.0f;
                        }
                    } else {
                        // change view point
                        stlRenderer.positionX += dx * TOUCH_SCALE_FACTOR / 5;
                        stlRenderer.positionY += dy * TOUCH_SCALE_FACTOR / 5;
                    }
                    stlRenderer.requestRedraw();
                    requestRender();
                }
                break;

            // end drag
            case MotionEvent.ACTION_UP:
                registerSensor(true);
                if (touchMode == TOUCH_DRAG) {
                    touchMode = TOUCH_NONE;
                    break;
                }
                stlRenderer.setsclae();
        }
    }

    /**
     * 双指缩放大小
     */
    private void zoomScale(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // starts pinch
            case MotionEvent.ACTION_POINTER_DOWN:
                registerSensor(false);
                if (event.getPointerCount() >= 2) {
                    pinchStartDistance = getPinchDistance(event);
                    //pinchStartZ = pinchStartDistance;
                    if (pinchStartDistance > 50f) {
                        getPinchCenterPoint(event, pinchStartPoint);
                        previousX = pinchStartPoint.x;
                        previousY = pinchStartPoint.y;
                        touchMode = TOUCH_ZOOM;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_ZOOM && pinchStartDistance > 0) {
                    // on pinch
                    PointF pt = new PointF();

                    getPinchCenterPoint(event, pt);
                    pinchMoveX = pt.x - previousX;
                    pinchMoveY = pt.y - previousY;
                    float dx = pinchMoveX;
                    float dy = pinchMoveY;
                    previousX = pt.x;
                    previousY = pt.y;

                    if (isRotate) {
                        stlRenderer.angleX += dx * TOUCH_SCALE_FACTOR;
                        stlRenderer.angleY += dy * TOUCH_SCALE_FACTOR;
                    } else {
                        // change view point
                        stlRenderer.positionX += dx * TOUCH_SCALE_FACTOR / 5;
                        stlRenderer.positionY += dy * TOUCH_SCALE_FACTOR / 5;
                    }

                    pinchScale = getPinchDistance(event) / pinchStartDistance;
                    changeDistance(pinchScale);
                    stlRenderer.requestRedraw();
                    invalidate();
                }
                break;

            // end pinch
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                registerSensor(true);
                pinchScale = 0;
                pinchStartZ = 0;
                if (touchMode == TOUCH_ZOOM) {
                    touchMode = TOUCH_NONE;

                    pinchMoveX = 0.0f;
                    pinchMoveY = 0.0f;
                    pinchScale = 1.0f;
                    pinchStartPoint.x = 0.0f;
                    pinchStartPoint.y = 0.0f;
                    invalidate();
                }
                break;
        }
    }

    /**
     * 传感器注册事件
     */
    private void registerSensor(boolean register) {
        if (sensorManager != null) {
            if (register) {
                sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
                        .SENSOR_DELAY_GAME);
            } else {
                sensorManager.unregisterListener(sensorEventListener);
            }
        }
    }

    /**
     * @param event
     * @return pinched distance
     */
    private float getPinchDistance(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
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
                        stlRenderer.angleX += sensorEvent.values[0] * dT * 180.0f % 360.0f;
                        stlRenderer.angleY += sensorEvent.values[1] * dT * 180.0f % 360.0f;
                        stlRenderer.requestRedraw();
                        requestRender();
                    }
                    timestamp = sensorEvent.timestamp;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
                .SENSOR_DELAY_GAME);
    }

    public void setSensor(boolean sensor) {
        isSensor = sensor;
    }

    public void setTouch(boolean touch) {
        isTouch = touch;
    }

    public void setRotate(boolean rotate) {
        isTouch = true;
        isRotate = rotate;
    }

    public void setScale(boolean scale) {
        isTouch = true;
        isScale = scale;
    }

    /**
     * @param event
     * @param pt    pinched point
     */
    private void getPinchCenterPoint(MotionEvent event, PointF pt) {
        pt.x = (event.getX(0) + event.getX(1)) * 0.5f;
        pt.y = (event.getY(0) + event.getY(1)) * 0.5f;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("super_state", super.onSaveInstanceState());
        bundle.putParcelable("model", modelData);
        bundle.putBoolean("isRotate", isRotate);
        bundle.putBoolean("isScale", isScale);
        bundle.putBoolean("isSensor", isSensor);
        bundle.putBoolean("isTouch", isTouch);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            setTouch(bundle.getBoolean("isTouch"));
            setRotate(bundle.getBoolean("isRotate"));
            setScale(bundle.getBoolean("isScale"));
            setSensor(bundle.getBoolean("isSensor"));
            setNewSTLObject((STLModel) bundle.getParcelable("model"));
            super.onRestoreInstanceState(bundle.getParcelable("super_state"));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 更新object 刷新界面
     *
     * @param stlObject
     */
    public void setNewSTLObject(STLModel stlObject) {
        this.modelData = stlObject;
        stlRenderer.requestRedraw(stlObject);
    }

    /**
     * 刷新界面
     */
    public void requestRedraw() {
        stlRenderer.requestRedraw();
    }

    public void delete() {
        stlRenderer.delete();
    }
}
