package com.study.xuan.gifshow.gif;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.study.xuan.gifshow.util.ScreenUtil;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Author : xuan.
 * Date : 2017/12/16.
 * Description :利用3d展示的gif控制播放进度
 */

public class VrGifView extends GifImageView {
    //触摸响应速度
    public static final int MODE_FAST = 1;
    public static final int MODE_NORMAL = 2;
    public static final int MODE_LOW = 3;
    private int SPEED_FAST = 100;
    private int SPEED_NORMAL = 250;
    private int SPEED_LOW = 500;

    private Context mContext;
    private GifDrawable gifDrawable;
    private int gifLength;
    //单指旋转
    private float lastX;
    private float downTime;
    private int curPos;
    private float moveX;
    private float moveDis;
    //手指拖动距离和播放位置的映射
    private float PX_TO_POS;
    private int moveMode;
    private int moveSpeed;
    //双指缩放
    private float pinchScale = 1.0f;
    private float pinchStartDistance = 0.0f;
    private ObjectAnimator scaleAnimator;
    private boolean isUp;
    private Animator.AnimatorListener listener;
    //当前缩放量
    private float scale_now = 1.0f;
    //触摸模式
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_DRAG = 1;
    private static final int TOUCH_ZOOM = 2;
    private int touchMode = TOUCH_NONE;
    //感应开关
    private boolean isTouch;
    private boolean isScale;
    private boolean isDrag;
    private boolean canAnim;

    public VrGifView(Context context) {
        this(context, null);
    }

    public VrGifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VrGifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        init();
    }

    private void init() {
        if (gifDrawable == null) {
            gifDrawable = (GifDrawable) getDrawable();
        }
        if (gifDrawable == null) {
            return;
        }
        gifLength = gifDrawable.getDuration();
        moveMode = MODE_FAST;
        moveSpeed = SPEED_FAST;
        canAnim = true;
        PX_TO_POS = gifLength / ScreenUtil.getWindowDisplay(mContext).getWidth();
        setImageDrawable(gifDrawable);
    }

    public void setTouch(boolean touch) {
        isTouch = touch;
    }

    public void setScale(boolean scale) {
        isScale = scale;
        if (isScale) {
            listener = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    canAnim = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    canAnim = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            };
        }
    }

    public void setDrag(boolean drag) {
        isDrag = drag;
    }

    /**
     * 设置触摸触发响应速度
     */
    public void setMoveMode(int mode) {
        switch (mode) {
            case MODE_FAST:
                moveMode = MODE_FAST;
                moveSpeed = SPEED_FAST;
                break;
            case MODE_NORMAL:
                moveMode = MODE_NORMAL;
                moveSpeed = SPEED_NORMAL;
                break;
            case MODE_LOW:
                moveMode = MODE_LOW;
                moveSpeed = SPEED_LOW;
                break;
            default:
                moveMode = MODE_NORMAL;
                moveSpeed = SPEED_NORMAL;
                break;
        }
    }

    /**
     * 设置gif图
     */
    public void setGifDrawable(GifDrawable gifDrawable) {
        this.gifDrawable = gifDrawable;
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
        if (isDrag) {
            rotateModel(event);
        }
        return true;
    }

    private void rotateModel(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (touchMode == TOUCH_NONE && event.getPointerCount() == 1) {
                    touchMode = TOUCH_DRAG;
                    gifDrawable.stop();
                    lastX = event.getX();
                    downTime = event.getDownTime();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_DRAG) {
                    if ((event.getEventTime() - downTime) > moveSpeed) {
                        moveX = event.getX();
                        moveDis = moveX - lastX;
                        lastX = moveX;
                        curPos = gifDrawable.getCurrentPosition();
                        if ((curPos + moveDis * PX_TO_POS) < 0) {
                            curPos += moveDis * PX_TO_POS + gifLength;
                        } else {
                            curPos += moveDis * PX_TO_POS;
                        }
                        if (curPos < 0) {
                            curPos = 0;
                        }
                        gifDrawable.seekTo(curPos);
                        downTime = event.getEventTime();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (touchMode == TOUCH_DRAG) {
                    touchMode = TOUCH_NONE;
                }
                gifDrawable.start();
                break;
        }
    }

    private void zoomScale(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // starts pinch
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2) {
                    pinchStartDistance = getPinchDistance(event);
                    downTime = event.getDownTime();
                    if (pinchStartDistance > 50f) {
                        touchMode = TOUCH_ZOOM;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_ZOOM && pinchStartDistance > 0) {
                    // on pinch
                    if ((event.getEventTime() - downTime) > moveSpeed) {
                        if (getPinchDistance(event) > pinchStartDistance) {
                            //递增
                            isUp = true;
                        } else {
                            isUp = false;
                        }
                        pinchScale = getPinchDistance(event) / pinchStartDistance;
                        if (checkScale(pinchScale)) {
                            changeScale(pinchScale);
                        }
                    }
                }
                break;

            // end pinch
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                pinchScale = 0;
                if (touchMode == TOUCH_ZOOM) {
                    touchMode = TOUCH_NONE;
                }
                break;
        }
    }

    private boolean checkScale(float pinchScale) {
        if (canAnim) {
            if (isUp) {
                if (pinchScale > 1) {
                    return true;
                }
            } else {
                if (pinchScale < 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private void changeScale(float pinchScale) {
        scaleAnimator = ObjectAnimator.ofFloat(this, "scale", scale_now, scale_now * pinchScale);
        scale_now = scale_now * pinchScale;
        scaleAnimator.setDuration(50);
        if (listener != null) {
            scaleAnimator.addListener(listener);
        }
        scaleAnimator.start();
    }

    private float getPinchDistance(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 动画反射需要
     */
    public void setScale(float value) {
        setScaleX(value);
        setScaleY(value);
    }
}
