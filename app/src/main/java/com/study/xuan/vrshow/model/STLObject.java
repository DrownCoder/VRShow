package com.study.xuan.vrshow.model;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;


/**
 * 解析STL文件 (优化后的)
 * @author zhaowencong
 *
 */
public class STLObject {
	private byte[] stlBytes = null;
	List<Float> normalList;
	FloatBuffer triangleBuffer;
	FloatBuffer normalBuffer;
	
	private int normalsize;
	
	IFinishCallBack finishcallback;
	
	public float maxX;
	public float maxY;
	public float maxZ;
	public float minX;
	public float minY;
	public float minZ;
	
	//优化使用的数组
	private  float[] normal_array=null;
	private  float[] vertex_array=null;
	private  int vertext_size=0;
	
	

	private ProgressDialog prepareProgressDialog(Context context) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setTitle("加载中...");
		progressDialog.setMax(0);
		progressDialog.setMessage("加载中");
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(false);
		
		progressDialog.show();
		
		return progressDialog;
	}
	
	public STLObject(byte[] stlBytes, Context context , IFinishCallBack finishcallback) {
		this.stlBytes = stlBytes;
		this.finishcallback=finishcallback;
		processSTL(stlBytes, context);
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

	private int getIntWithLittleEndian(byte[] bytes, int offset) {
		return (0xff & stlBytes[offset]) | ((0xff & stlBytes[offset + 1]) << 8) | ((0xff & stlBytes[offset + 2]) << 16) | ((0xff & stlBytes[offset + 3]) << 24);
	}
	
	/**
	 * checks 'text' in ASCII code
	 * 
	 * @param bytes
	 * @return
	 */
	boolean isText(byte[] bytes) {
		for (byte b : bytes) {
			if (b == 0x0a || b == 0x0d || b == 0x09) {
				// white spaces
				continue;
			}
			if (b < 0x20 || (0xff & b) >= 0x80) {
				// control codes
				return false;
			}
		}
		return true;
	}
	
	/**
	 * FIXME 'STL format error detection' depends exceptions.
	 * 
	 * @param stlBytes
	 * @param context
	 * @param
	 * @return
	 */
	private boolean processSTL(byte[] stlBytes, final Context context) {
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		normalList = new ArrayList<Float>();
		
		final ProgressDialog progressDialog = prepareProgressDialog(context);

		final AsyncTask<byte[], Integer, float[]> task = new AsyncTask<byte[], Integer, float[]>() {

			float[] processText(String stlText) throws Exception {
				List<Float> vertexList = new ArrayList<Float>();
				normalList.clear();

				String[] stlLines = stlText.split("\n");
				vertext_size=(stlLines.length-2)/7;
				vertex_array=new float[vertext_size*9];
				normal_array=new float[vertext_size*9];
				progressDialog.setMax(stlLines.length);
				
				int normal_num=0;
				int vertex_num=0;
				for (int i = 0; i < stlLines.length; i++) {
					String string = stlLines[i].trim();
					if (string.startsWith("facet normal ")) {
						string = string.replaceFirst("facet normal ", "");
						String[] normalValue = string.split(" ");
						for(int n=0;n<3;n++){
							normal_array[normal_num++]=Float.parseFloat(normalValue[0]);
							normal_array[normal_num++]=Float.parseFloat(normalValue[1]);
							normal_array[normal_num++]=Float.parseFloat(normalValue[2]);
						}
					}
					if (string.startsWith("vertex ")) {
						string = string.replaceFirst("vertex ", "");
						String[] vertexValue = string.split(" ");
						float x = Float.parseFloat(vertexValue[0]);
						float y = Float.parseFloat(vertexValue[1]);
						float z = Float.parseFloat(vertexValue[2]);
						adjustMaxMin(x, y, z);
						vertex_array[vertex_num++]=x;
						vertex_array[vertex_num++]=y;
						vertex_array[vertex_num++]=z;
					}
					
					if (i % (stlLines.length / 50) == 0) {
						publishProgress(i);
					}
				}
				//vertext_size=vertex_array.length;
				return vertex_array;
			}
			
			float[] processBinary(byte[] stlBytes) throws Exception {
				
				vertext_size=getIntWithLittleEndian(stlBytes, 80);;
				vertex_array=new float[vertext_size*9];
				normal_array=new float[vertext_size*9];
				
				progressDialog.setMax(vertext_size);
				for (int i = 0; i < vertext_size; i++) {
					for(int n=0;n<3;n++){
						normal_array[i*9+n*3]=Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50));
						normal_array[i*9+n*3+1]=Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 4));
						normal_array[i*9+n*3+2]=Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 8));
					}
					float x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 12));
					float y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 16));
					float z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 20));
					adjustMaxMin(x, y, z);
					vertex_array[i*9]=x;
					vertex_array[i*9+1]=y;
					vertex_array[i*9+2]=z;
					
					x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 24));
					y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 28));
					z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 32));
					adjustMaxMin(x, y, z);
					vertex_array[i*9+3]=x;
					vertex_array[i*9+4]=y;
					vertex_array[i*9+5]=z;
					
					x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 36));
					y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 40));
					z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 44));
					adjustMaxMin(x, y, z);
					vertex_array[i*9+6]=x;
					vertex_array[i*9+7]=y;
					vertex_array[i*9+8]=z;
					
					if (i % (vertext_size / 50) == 0) {
						publishProgress(i);
					}
				}
				
				return vertex_array;
			}
			
			@Override
			protected float[] doInBackground(byte[]... stlBytes) {
				float[]processResult = null;
				try {
					if (isText(stlBytes[0])) {
						//Log.i("trying text...");
						processResult = processText(new String(stlBytes[0]));
					} else {
						//Log.i("trying binary...");
						processResult = processBinary(stlBytes[0]);
					}
				} catch (Exception e) {
				}
				if (processResult != null && processResult.length > 0 && normal_array != null && normal_array.length > 0) {
					return processResult;
				}
				
				return processResult;
			}
			
			@Override
			public void onProgressUpdate(Integer... values) {
				progressDialog.setProgress(values[0]);
			}
			
			@Override
			protected void onPostExecute(float[] vertexList) {
				
				
				
				if (normal_array.length < 1 || vertex_array.length < 1) {
					Toast.makeText(context, "错误", Toast.LENGTH_LONG).show();
					
					progressDialog.dismiss();
					return;
				}
				
				System.out.println("zwcnormalsize"+vertext_size);
				
				ByteBuffer normal = ByteBuffer.allocateDirect(normal_array.length * 4);
				normal.order(ByteOrder.nativeOrder());
				normalBuffer = normal.asFloatBuffer();
				normalBuffer.put(normal_array);
				normalBuffer.position(0);
				
				//=================矫正中心店坐标========================
				float center_x=(maxX+minX)/2;
				float center_y=(maxY+minY)/2;
				float center_z=(maxZ+minZ)/2;
				
				for(int i=0;i<vertext_size*3;i++){
					adjust_coordinate(vertex_array,i*3,center_x);
					adjust_coordinate(vertex_array,i*3+1,center_y);
					adjust_coordinate(vertex_array,i*3+2,center_z);
				}
				
				
				ByteBuffer vbb = ByteBuffer.allocateDirect(vertex_array.length * 4);
				vbb.order(ByteOrder.nativeOrder());
				triangleBuffer = vbb.asFloatBuffer();
				triangleBuffer.put(vertex_array);
				triangleBuffer.position(0);
				
				finishcallback.readstlfinish();

				progressDialog.dismiss();
			}
		};

		try {
			task.execute(stlBytes);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	/**
	 * 矫正坐标  坐标圆心移动
	 * @param
	 * @param postion
	 */
	private void adjust_coordinate(float[] vertex_array , int postion,float adjust){
		vertex_array[postion]-=adjust;
	}
	
	/**
	 * 记录矫正法线的数据   (有时间再完善这个矫正)
	 * @param vertexList
	 * @param vertex_point
	 */
	private void remenber_repeat(List<Float> vertexList,float vertex_point){
		Map<String,List<Integer>> remenbermap=new HashMap<String,List<Integer>>();
		if(remenbermap!=null){
			
		}
	}
	
	private float[] listToFloatArray(List<Float> list) {
		float[] result = new float[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return result;
	}
	
	public void draw(GL10 gl) {
		if (normalList == null || triangleBuffer == null) {
			return;
		}
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		//gl.glFrontFace(GL10.GL_CCW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triangleBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT,0, normalBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertext_size*3);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		

	}
	
	public interface IFinishCallBack{
		public  void readstlfinish();
	}
	
	public void delete (){
		stlBytes=null;
	}
}
