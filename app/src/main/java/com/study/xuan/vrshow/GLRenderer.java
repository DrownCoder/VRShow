package com.study.xuan.vrshow;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import com.study.xuan.vrshow.model.Model;
import com.study.xuan.vrshow.model.Point;
import com.study.xuan.vrshow.model.RotateModel;
import com.study.xuan.vrshow.model.STLReader;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Package com.hc.opengl
 * Created by HuaChao on 2016/7/28.
 */
public class GLRenderer implements GLSurfaceView.Renderer {

    private Model model;
    private Point mCenterPoint;
    private Point eye = new Point(0, 0, -3);
    private Point up = new Point(0, 1, 0);
    private Point center = new Point(0, 0, 0);
    private float mScalef = 1;
    private float standScale;
    private RotateModel mDegree;

    public GLRenderer(Context context) {
        try {
            model = new STLReader().parserBinStlInAssets(context, "BelleBook_Big.stl");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDegree = new RotateModel();
    }

    public void rotate(RotateModel angle) {
        mDegree = angle;
    }

    public void scale(float scale) {
        this.mScalef = scale;
    }

    public float getStandScale() {
        return standScale;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除屏幕和深度缓存
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);


        gl.glLoadIdentity();// 重置当前的模型观察矩阵


        //眼睛对着原点看
        GLU.gluLookAt(gl, eye.x, eye.y, eye.z, center.x,
                center.y, center.z, up.x, up.y, up.z);
        //旋转
        gl.glRotatef(mDegree.xAngle, 0, mDegree.xAngle, 0);
        gl.glRotatef(mDegree.yAngle, mDegree.yAngle, 0, 0);
        gl.glRotatef(mDegree.zAngle, 0, 0, mDegree.zAngle);

        //将模型放缩到View刚好装下
        gl.glScalef(mScalef, mScalef, mScalef);
        Log.i("scale", mScalef + "");
        //把模型移动到原点
        gl.glTranslatef(-mCenterPoint.x, -mCenterPoint.y,
                -mCenterPoint.z);


        //===================begin==============================//

        //允许给每个顶点设置法向量
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        // 允许设置顶点
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // 允许设置颜色

        //设置法向量数据源
        gl.glNormalPointer(GL10.GL_FLOAT, 0, model.getVnormBuffer());
        // 设置三角形顶点数据源
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, model.getVertBuffer());

        // 绘制三角形
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, model.getFacetCount() * 3);

        // 取消顶点设置
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        //取消法向量设置
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);

        //=====================end============================//

    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // 设置OpenGL场景的大小,(0,0)表示窗口内部视口的左下角，(width, height)指定了视口的大小
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION); // 设置投影矩阵
        gl.glLoadIdentity(); // 设置矩阵为单位矩阵，相当于重置矩阵
        GLU.gluPerspective(gl, 45.0f, ((float) width) / height, 1f, 100f);// 设置透视范围

        //以下两句声明，以后所有的变换都是针对模型(即我们绘制的图形)
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();


    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glEnable(GL10.GL_DEPTH_TEST); // 启用深度缓存
        gl.glClearDepthf(1.0f); // 设置深度缓存值
        gl.glDepthFunc(GL10.GL_LEQUAL); // 设置深度缓存比较函数
        gl.glShadeModel(GL10.GL_SMOOTH);// 设置阴影模式GL_SMOOTH
        float r = model.getR();
        //r是半径，不是直径，因此用0.5/r可以算出放缩比例
        if (mScalef == 1) {
            mScalef = 0.5f / r;
            standScale = mScalef;
        }
        mCenterPoint = model.getCentrePoint();
    }
}
