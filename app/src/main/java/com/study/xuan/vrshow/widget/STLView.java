package com.study.xuan.vrshow.widget;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.study.xuan.vrshow.model.STLModel;
import com.study.xuan.vrshow.util.IOUtils;


public class STLView extends GLSurfaceView {

	private STLRenderer stlRenderer;
	private Uri uri;
	//控制缩放速度的
	static int CONTROL=10;

	public STLView(Context context, STLModel stlObject) {
		super(context);


		SharedPreferences colorConfig = context.getSharedPreferences("colors", Activity.MODE_PRIVATE);
		STLRenderer.red = colorConfig.getFloat("red", 0.75f);
		STLRenderer.green = colorConfig.getFloat("green", 0.75f);
		STLRenderer.blue = colorConfig.getFloat("blue", 0.75f);
		STLRenderer.alpha = colorConfig.getFloat("alpha", 0.5f);

		// render: stlObject as null
		stlRenderer = new STLRenderer(stlObject);
		setRenderer(stlRenderer);
		stlRenderer.requestRedraw();
	}

	/**
	 * @param context
	 * @return
	 */
	private byte[] getSTLBytes(Context context, Uri uri) {
		byte[] stlBytes = null;
		InputStream inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
			stlBytes = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return stlBytes;
	}

	//这里将偏移数值降低
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320/2;
	private float previousX;
	private float previousY;

	private void changeDistance(float scale) {
		stlRenderer.scale = scale;
	}

	private boolean isRotate = true;

	public boolean isRotate() {
		return isRotate;
	}

	public void setRotate(boolean isRotate) {
		this.isRotate = isRotate;
	}

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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// starts pinch
			case MotionEvent.ACTION_POINTER_DOWN:
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
					changeDistance( pinchScale );
					stlRenderer.requestRedraw();
					invalidate();
				}
				break;

			// end pinch
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				pinchScale=0;
				pinchStartZ=0;
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

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// start drag
			case MotionEvent.ACTION_DOWN:
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
					previousX = x;
					previousY = y;

					if (isRotate) {
						stlRenderer.angleX += dx * TOUCH_SCALE_FACTOR;
						stlRenderer.angleY += dy * TOUCH_SCALE_FACTOR;
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
				if (touchMode == TOUCH_DRAG) {
					touchMode = TOUCH_NONE;
					break;
				}
				stlRenderer.setsclae();
		}

		return true;
	}

	/**
	 *
	 * @param event
	 * @return pinched distance
	 */
	private float getPinchDistance(MotionEvent event) {
		float x=0;
		float y=0;
		try {
			x = event.getX(0) - event.getX(1);
			y = event.getY(0) - event.getY(1);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 *
	 * @param event
	 * @param pt pinched point
	 */
	private void getPinchCenterPoint(MotionEvent event, PointF pt) {
		pt.x = (event.getX(0) + event.getX(1)) * 0.5f;
		pt.y = (event.getY(0) + event.getY(1)) * 0.5f;
	}

	public Uri getUri() {
		return uri;
	}
	/**
	 * 更新object 刷新界面
	 * @param stlObject
	 */
	public void setNewSTLObject(STLModel stlObject){
		stlRenderer.requestRedraw(stlObject);
	}
	/**
	 * 刷新界面
	 */
	public void requestRedraw(){
		stlRenderer.requestRedraw();
	}

	public void delete (){
		stlRenderer.delete();
	}
}
