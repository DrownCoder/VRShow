package com.study.xuan.vrshow.operate;

import android.content.Context;
import android.util.Log;

import com.study.xuan.vrshow.callback.onReadListener;
import com.study.xuan.vrshow.model.Model;
import com.study.xuan.vrshow.util.STLUtils;
import com.study.xuan.vrshow.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Package com.hc.opengl
 * Created by HuaChao on 2016/7/28.
 */
public class STLReader implements ISTLReader{
    private onReadListener listener;
    List<Float> normalList;
    public float maxX;
    public float maxY;
    public float maxZ;
    public float minX;
    public float minY;
    public float minZ;
    //优化使用的数组
    private float[] normal_array = null;
    private float[] vertex_array = null;
    private int vertext_size = 0;

    public Model parserBinStlInSDCard(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        return parserBinStl(fis);
    }

    public Model parserBinStlInAssets(Context context, String fileName)
                            throws IOException {

        InputStream is = context.getAssets().open(fileName);
        if (STLUtils.isAscii(Util.toByteArray(is))) {
            return parserAsciiStl(is);
        }
        return parserBinStl(is);
    }

    public Model parserAsciiStlAssets(Context context, String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        return parserAsciiStl(is);
    }

    /**
     * 解析模型数据，包括顶点数据、法向量数据、所占空间范围等
     */
    private void parseModel(Model model, byte[] facetBytes) {
        int facetCount = model.getFacetCount();
        /**
         *  每个三角面片占用固定的50个字节,50字节当中：
         *  三角片的法向量：（1个向量相当于一个点）*（3维/点）*（4字节浮点数/维）=12字节
         *  三角片的三个点坐标：（3个点）*（3维/点）*（4字节浮点数/维）=36字节
         *  最后2个字节用来描述三角面片的属性信息
         * **/
        // 保存所有顶点坐标信息,一个三角形3个顶点，一个顶点3个坐标轴
        float[] verts = new float[facetCount * 3 * 3];
        // 保存所有三角面对应的法向量位置，
        // 一个三角面对应一个法向量，一个法向量有3个点
        // 而绘制模型时，是针对需要每个顶点对应的法向量，因此存储长度需要*3
        // 又同一个三角面的三个顶点的法向量是相同的，
        // 因此后面写入法向量数据的时候，只需连续写入3个相同的法向量即可
        float[] vnorms = new float[facetCount * 3 * 3];
        //保存所有三角面的属性信息
        short[] remarks = new short[facetCount];

        int stlOffset = 0;
        try {
            for (int i = 0; i < facetCount; i++) {
                if (listener != null) {
                    listener.onLoading(i, facetCount);
                }
                for (int j = 0; j < 4; j++) {
                    float x = Util.byte4ToFloat(facetBytes, stlOffset);
                    float y = Util.byte4ToFloat(facetBytes, stlOffset + 4);
                    float z = Util.byte4ToFloat(facetBytes, stlOffset + 8);
                    stlOffset += 12;

                    if (j == 0) {//法向量 
                        vnorms[i * 9] = x;
                        vnorms[i * 9 + 1] = y;
                        vnorms[i * 9 + 2] = z;
                        vnorms[i * 9 + 3] = x;
                        vnorms[i * 9 + 4] = y;
                        vnorms[i * 9 + 5] = z;
                        vnorms[i * 9 + 6] = x;
                        vnorms[i * 9 + 7] = y;
                        vnorms[i * 9 + 8] = z;
                    } else {//三个顶点
                        verts[i * 9 + (j - 1) * 3] = x;
                        verts[i * 9 + (j - 1) * 3 + 1] = y;
                        verts[i * 9 + (j - 1) * 3 + 2] = z;

                        //记录模型中三个坐标轴方向的最大最小值
                        if (i == 0 && j == 1) {
                            model.minX = model.maxX = x;
                            model.minY = model.maxY = y;
                            model.minZ = model.maxZ = z;
                        } else {
                            model.minX = Math.min(model.minX, x);
                            model.minY = Math.min(model.minY, y);
                            model.minZ = Math.min(model.minZ, z);
                            model.maxX = Math.max(model.maxX, x);
                            model.maxY = Math.max(model.maxY, y);
                            model.maxZ = Math.max(model.maxZ, z);
                        }
                    }
                }
                short r = Util.byte2ToShort(facetBytes, stlOffset);
                stlOffset = stlOffset + 2;
                remarks[i] = r;
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e);
            } else {
                e.printStackTrace();
            }
        }
        //将读取的数据设置到Model对象中
        model.setVerts(verts);
        model.setVnorms(vnorms);
        model.setRemarks(remarks);

    }

    private void adjustMaxMin(float x, float y, float z) {
        if (x > maxX) {
            maxX = x;
        }
        if (y > maxY) {
            maxY = y;
        }
        if (z > maxZ) {
            maxZ = z;
        }
        if (x < minX) {
            minX = x;
        }
        if (y < minY) {
            minY = y;
        }
        if (z < minZ) {
            minZ = z;
        }
    }

    /**
     * 解析二进制格式的STL文件
     */
    @Override
    public Model parserBinStl(byte[] source) {
        InputStream in = new ByteArrayInputStream(source);
        if (listener != null)
            listener.onstart();
        Model model = new Model();
        try {
            //前面80字节是文件头，用于存贮文件名；
            in.skip(80);

            //紧接着用 4 个字节的整数来描述模型的三角面片个数
            byte[] bytes = new byte[4];
            in.read(bytes);// 读取三角面片个数
            int facetCount = Util.byte4ToInt(bytes, 0);
            model.setFacetCount(facetCount);
            if (facetCount == 0) {
                in.close();
                return model;
            }

            // 每个三角面片占用固定的50个字节
            byte[] facetBytes = new byte[50 * facetCount];
            // 将所有的三角面片读取到字节数组
            in.read(facetBytes);
            //数据读取完毕后，可以把输入流关闭
            in.close();


            parseModel(model, facetBytes);


        } catch (IOException e) {
        }
        return model;
    }

    @Override
    public Model parserBinStl(InputStream in) {
        Model model = new Model();
        try {
            //前面80字节是文件头，用于存贮文件名；
            in.skip(80);

            //紧接着用 4 个字节的整数来描述模型的三角面片个数
            byte[] bytes = new byte[4];
            in.read(bytes);// 读取三角面片个数
            int facetCount = Util.byte4ToInt(bytes, 0);
            model.setFacetCount(facetCount);
            if (facetCount == 0) {
                in.close();
                return model;
            }

            // 每个三角面片占用固定的50个字节
            byte[] facetBytes = new byte[50 * facetCount];
            // 将所有的三角面片读取到字节数组
            in.read(facetBytes);
            //数据读取完毕后，可以把输入流关闭
            in.close();


            parseModel(model, facetBytes);


        } catch (IOException e) {
        }
        return model;
    }

    /**
     * 解析ASCII格式的STL文件
     */
    @Override
    public Model parserAsciiStl(byte[] bytes) {
        int max = 0;
        Model model = new Model();
        String stlText = new String(bytes);
        List<Float> vertexList = new ArrayList<Float>();
        //normalList.clear();

        String[] stlLines = stlText.split("\n");
        vertext_size = (stlLines.length - 2) / 7;
        vertex_array = new float[vertext_size * 9];
        normal_array = new float[vertext_size * 9];
        //progressDialog.setMax(stlLines.length);
        max = stlLines.length;

        int normal_num = 0;
        int vertex_num = 0;
        for (int i = 0; i < stlLines.length; i++) {
            String string = stlLines[i].trim();
            if (string.startsWith("facet normal ")) {
                string = string.replaceFirst("facet normal ", "");
                String[] normalValue = string.split(" ");
                for (int n = 0; n < 3; n++) {
                    normal_array[normal_num++] = Float.parseFloat(normalValue[0]);
                    normal_array[normal_num++] = Float.parseFloat(normalValue[1]);
                    normal_array[normal_num++] = Float.parseFloat(normalValue[2]);
                }
            }
            if (string.startsWith("vertex ")) {
                string = string.replaceFirst("vertex ", "");
                String[] vertexValue = string.split(" ");
                float x = Float.parseFloat(vertexValue[0]);
                float y = Float.parseFloat(vertexValue[1]);
                float z = Float.parseFloat(vertexValue[2]);
                adjustMaxMin(x, y, z);
                vertex_array[vertex_num++] = x;
                vertex_array[vertex_num++] = y;
                vertex_array[vertex_num++] = z;
            }

            if (i % (stlLines.length / 50) == 0) {
                listener.onLoading(i, max);
            }
        }
        //将读取的数据设置到Model对象中
        //=================矫正中心店坐标========================
        float center_x=(maxX+minX)/2;
        float center_y=(maxY+minY)/2;
        float center_z=(maxZ+minZ)/2;

        for(int i=0;i<vertext_size*3;i++){
            adjust_coordinate(vertex_array,i*3,center_x);
            adjust_coordinate(vertex_array,i*3+1,center_y);
            adjust_coordinate(vertex_array,i*3+2,center_z);
        }
        model.setMax(maxX, maxY, maxZ);
        model.setMin(minX, minY, minZ);
        model.setFacetCount(vertext_size);
        model.setVerts(vertex_array);
        model.setVnorms(normal_array);
        return model;
    }
    /**
     * 矫正坐标  坐标圆心移动
     * @param
     * @param postion
     */
    private void adjust_coordinate(float[] vertex_array , int postion,float adjust){
        vertex_array[postion]-=adjust;
    }

    @Override
    public Model parserAsciiStl(InputStream in) {
        return null;
    }

    @Override
    public void setCallBack(onReadListener listener) {
        this.listener = listener;
    }

}
