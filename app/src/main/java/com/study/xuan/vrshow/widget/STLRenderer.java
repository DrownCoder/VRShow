package com.study.xuan.vrshow.widget;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

import com.study.xuan.vrshow.model.STLModel;

/**
 * Author : xuan.
 * Date : 2017/12/14.
 * Description : 渲染器
 */


public class STLRenderer implements Renderer {
    public static final int FRAME_BUFFER_COUNT = 5;
    public float angleX;
    public float angleY;
    public float positionX = 0f;
    public float positionY = 0f;
    //scale
    //外部控制
    public float scale = 1.0f;
    //当前展示
    private float scale_rember = 1.0f;
    //当前固定
    private float scale_now = 1.0f;
    private boolean scaleRange;
    private float SCALE_MAX;
    private float SCALE_MIN;
    public float translation_z;

    public static float red;
    public static float green;
    public static float blue;
    public static float alpha;
    public static boolean displayAxes = false;
    public static boolean displayGrids = false;
    private static int bufferCounter = FRAME_BUFFER_COUNT;

    private STLModel stlObject;

    public STLRenderer(STLModel stlObject) {
        this.stlObject = stlObject;
        setTransLation_Z();
    }

    /**
     * 简单重绘（适用于旋转等）
     */
    public void requestRedraw() {
        bufferCounter = FRAME_BUFFER_COUNT;
    }

    /**
     * 复杂重绘 （适用于更换文件）
     *
     * @param stlObject
     */
    public void requestRedraw(STLModel stlObject) {

        this.stlObject = stlObject;
        setTransLation_Z();
        bufferCounter = FRAME_BUFFER_COUNT;
    }

    public void checkScaleRange(boolean scaleRange) {
        this.scaleRange = scaleRange;
    }

    public void setScaleRange(float max, float min) {
        this.SCALE_MAX = max;
        this.SCALE_MIN = min;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (stlObject == null) {
            return;
        }
        if (bufferCounter < 1) {
            return;
        }
        bufferCounter--;
        gl.glLoadIdentity();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glTranslatef(positionX, -positionY, 0);
        // rotation and apply Z-axis
        gl.glTranslatef(0, 0, translation_z);
        // TODO: 2017/12/14 根据angleX的范围变化，angelY的旋转中心轴需要相应的变化
        gl.glRotatef(angleX, 0, 1, 0);
        gl.glRotatef(angleY, 1, 0, 0);
        Log.i("angle", "angleX" + angleX + "angleY" + angleY);
        scale_rember = scale_now * scale;
        if (scaleRange) {
            if (scale_rember > SCALE_MAX) {
                scale_rember = SCALE_MAX;
            }
            if (scale_rember < SCALE_MIN) {
                scale_rember = SCALE_MIN;
            }
        }
        gl.glScalef(scale_rember, scale_rember, scale_rember);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        // draw X-Y field
        if (!displayGrids) {
            drawGrids(gl);
            drawLines(gl);
        }

        // draw axis
        if (displayAxes) {
            gl.glLineWidth(3f);
            float[] vertexArray = {-100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100};
            FloatBuffer lineBuffer = getFloatBufferFromArray(vertexArray);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);

            // X : red
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{1.0f, 0f, 0f, 0.75f}, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{1.0f, 0f, 0f, 0.5f}, 0);
            gl.glDrawArrays(GL10.GL_LINES, 0, 2);

            // Y : blue
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{0f, 0f, 1.0f, 0.75f}, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{0f, 0f, 1.0f, 0.5f}, 0);
            gl.glDrawArrays(GL10.GL_LINES, 2, 2);

            // Z : green
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{0f, 1.0f, 0f, 0.75f}, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{0f, 1.0f, 0f, 0.5f}, 0);
            gl.glDrawArrays(GL10.GL_LINES, 4, 2);
        }

        // draw object
        if (stlObject != null) {
            // FIXME transparency applying does not correctly
            gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_AMBIENT, new float[]{0.75f, 0.75f, 0.75f, 1.0f}, 0);

            gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_DIFFUSE, new float[]{0.75f, 0.75f, 0.75f, 1.0f}, 0);

            gl.glEnable(GL10.GL_COLOR_MATERIAL);
            gl.glPushMatrix();
            gl.glColor4f(red, green, blue, 1.0f);
            stlObject.draw(gl);
            gl.glPopMatrix();
            gl.glDisable(GL10.GL_COLOR_MATERIAL);
        }
    }

    private FloatBuffer getFloatBufferFromArray(float[] vertexArray) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer triangleBuffer = vbb.asFloatBuffer();
        triangleBuffer.put(vertexArray);
        triangleBuffer.position(0);
        return triangleBuffer;
    }

    private FloatBuffer getFloatBufferFromList(List<Float> vertexList) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexList.size() * 4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer triangleBuffer = vbb.asFloatBuffer();
        float[] array = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            array[i] = vertexList.get(i);
        }
        triangleBuffer.put(array);
        triangleBuffer.position(0);
        return triangleBuffer;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (stlObject == null) {
            return;
        }
        float aspectRatio = (float) width / height;

        gl.glViewport(0, 0, width, height);

        gl.glLoadIdentity();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        GLU.gluPerspective(gl, 45f, aspectRatio, 1f, 5000f);// (stlObject.maxZ - stlObject.minZ) * 10f + 100f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        GLU.gluLookAt(gl, 0, 0, 100f, 0, 0, 0, 0, 1f, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (stlObject == null) {
            return;
        }
//		gl.glClearColor(0f, 0f, 0f, 0.5f);


        //gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL10.GL_BLEND);
//		 gl.glEnable(GL10.GL_TEXTURE_2D);
//		 gl.glBlendFunc(GL10.GL_ONE, GL10.GL_SRC_COLOR);
        // FIXME This line seems not to be needed?
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(3152, 4354);
        gl.glEnable(GL10.GL_NORMALIZE);
        gl.glShadeModel(GL10.GL_SMOOTH);

        gl.glMatrixMode(GL10.GL_PROJECTION);

        // Lighting
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, getFloatBufferFromArray(new float[]{0.5f, 0.5f, 0.5f, 1.0f}));// 全局环境光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT_AND_DIFFUSE, new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[]{0f, 0f, 1000f, 1.0f}, 0);
        gl.glEnable(GL10.GL_LIGHT0);

    }

    /**
     * 画网格
     *
     * @param gl
     */
    private void drawGrids(GL10 gl) {
        List<Float> lineList = new ArrayList<Float>();

        for (int x = -100; x <= 100; x += 5) {
            lineList.add((float) x);
            lineList.add(-100f);
            lineList.add(0f);
            lineList.add((float) x);
            lineList.add(100f);
            lineList.add(0f);
        }
        for (int y = -100; y <= 100; y += 5) {
            lineList.add(-100f);
            lineList.add((float) y);
            lineList.add(0f);
            lineList.add(100f);
            lineList.add((float) y);
            lineList.add(0f);
        }

        FloatBuffer lineBuffer = getFloatBufferFromList(lineList);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);

        gl.glLineWidth(1f);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
        gl.glDrawArrays(GL10.GL_LINES, 0, lineList.size() / 3);
    }

    /**
     * 画坐标
     *
     * @param gl
     */
    private void drawLines(GL10 gl) {
        gl.glLineWidth(3f);
        float[] vertexArray = {-100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100};
        FloatBuffer lineBuffer = getFloatBufferFromArray(vertexArray);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);

        // X : red
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{1.0f, 0f, 0f, 0.75f}, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{1.0f, 0f, 0f, 0.5f}, 0);
        gl.glDrawArrays(GL10.GL_LINES, 0, 2);

        // Y : blue
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{0f, 0f, 1.0f, 0.75f}, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{0f, 0f, 1.0f, 0.5f}, 0);
        gl.glDrawArrays(GL10.GL_LINES, 2, 2);

        // Z : green
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{0f, 1.0f, 0f, 0.75f}, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{0f, 1.0f, 0f, 0.5f}, 0);
        gl.glDrawArrays(GL10.GL_LINES, 4, 2);
    }

    /**
     * 调整Z轴平移位置    （目的式为了模型展示大小适中）
     */
    private void setTransLation_Z() {
        //算x、y轴差值
        float distance_x = stlObject.maxX - stlObject.minX;
        float distance_y = stlObject.maxY - stlObject.minY;
        float distance_z = stlObject.maxZ - stlObject.minZ;
        translation_z = distance_x;
        if (translation_z < distance_y) {
            translation_z = distance_y;
        }
        if (translation_z < distance_z) {
            translation_z = distance_z;
        }
        translation_z *= -2;
    }

    public void delete() {
        stlObject.delete();
        stlObject = null;
    }

    /**
     * 固定缩放比例
     */
    public void setsclae() {
        scale_now = scale_rember;
        scale_rember = 1.0f;
        scale = 1.0f;
    }
}
