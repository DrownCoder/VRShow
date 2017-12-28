# VRShow
VR全景图+Opengl3D模型展示  
**使用方式：**  
1.Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        compile 'com.github.sdfdzx:VRShow:v1.0.3'
	}
### 1.全景360°GIF图
![全景360°GIF图](https://github.com/sdfdzx/VRShow/blob/master/gif/gifdemo.gif)  

**功能：**  
>1.支持单指拖拽  
>2.支持双指缩放  
>3.支持触摸响应速度模式：LOW,NORMAL,FAST    

**使用方式：**
Step 1. XML and Java
```
<com.study.xuan.gifshow.widget.VrGifView
        android:id="@+id/gif"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/demo"
        />

public class GifActivity extends AppCompatActivity {
    private VrGifView mGif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);
        mGif = (VrGifView) findViewById(R.id.gif);
        mGif.setTouch(true);//是否 可触摸
        mGif.setDrag(true);//是否可拖拽
        mGif.setScale(false);//是否可伸缩
        mGif.setMoveMode(VrGifView.MODE_FAST);//触摸响应速度
    }
}
```
### 2.3D模型展示  
![3D模型展示](https://github.com/sdfdzx/VRShow/blob/master/gif/book.gif)  

**功能：**  
>1.异步读取STL格式的3D文件  
>2.支持进度回调  
>3.支持单指拖动  
>4.支持双指缩放  
>5.支持陀螺仪传感器  

**使用方式：**
Step 1. XML and Java
```
<com.study.xuan.stlshow.widget.STLView
        android:id="@+id/stl"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>


        STLViewBuilder.init(mStl).Assets(this, "bai.stl").build();
        mStl.setTouch(true);
        mStl.setScale(true);
        mStl.setRotate(true);
        mStl.setSensor(true);
mStl.setOnReadCallBack(new OnReadCallBack() {
            @Override
            public void onStart() {}
            @Override
            public void onReading(int cur, int total) {}
            @Override
            public void onFinish() {}
        });
```
