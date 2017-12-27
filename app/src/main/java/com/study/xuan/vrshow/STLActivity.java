package com.study.xuan.vrshow;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.study.xuan.gifshow.widget.stlview.callback.OnReadCallBack;
import com.study.xuan.gifshow.widget.stlview.widget.STLView;
import com.study.xuan.gifshow.widget.stlview.widget.STLViewBuilder;

public class STLActivity extends AppCompatActivity {
    private STLView mStl;
    private Context mContext;
    private ProgressDialog mBar;
    private Bundle bundle = new Bundle();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            float cur = bundle.getFloat("cur");
            float total = bundle.getFloat("total");
            float progress = cur / total;
            Log.i("Progress", progress + "");
            mBar.setProgress((int) (progress * 100.0f));
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stl);
        mContext = this;
        mStl = (STLView) findViewById(R.id.stl);
        mBar = prepareProgressDialog(mContext);
        mStl.setOnReadCallBack(new OnReadCallBack() {
            @Override
            public void onStart() {
                Toast.makeText(mContext, "开始解析!", Toast.LENGTH_LONG).show();
                mBar.show();
            }

            @Override
            public void onReading(int cur, int total) {
                bundle.putFloat("cur", cur);
                bundle.putFloat("total", total);
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onFinish() {
                mBar.dismiss();
            }
        });
        STLViewBuilder.init(mStl).Assets(this, "bai.stl").build();
        mStl.setTouch(true);
        mStl.setScale(true);
        mStl.setRotate(true);
        mStl.setSensor(true);
    }

    private ProgressDialog prepareProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.stl_load_progress_title);
        progressDialog.setMax(100);
        progressDialog.setMessage(context.getString(R.string.stl_load_progress_message));
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        return progressDialog;
    }
}
