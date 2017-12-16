package com.study.xuan.stlshow.operate;

import android.os.AsyncTask;

import com.study.xuan.stlshow.callback.OnReadListener;
import com.study.xuan.stlshow.model.STLModel;
import com.study.xuan.stlshow.util.STLUtils;

/**
 * Author : xuan.
 * Date : 2017/12/10.
 * Description : 异步加载Stl文件
 */
public class ReaderHandler {
    private ISTLReader reader;
    private ReaderTask backWorker;
    private OnReadListener listener;

    public ReaderHandler(ISTLReader reader, OnReadListener listener) {
        this.reader = reader;
        this.listener = listener;
        backWorker = new ReaderTask();
    }

    public void read(byte[] stlBytes) {
        try {
            backWorker.execute(stlBytes);
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }

    private class ReaderTask extends AsyncTask<byte[], Integer,STLModel>{
        @Override
        protected void onPreExecute() {
            listener.onstart();
        }

        @Override
        protected STLModel doInBackground(byte[]... bytes) {
            STLModel model;
            if (STLUtils.isAscii(bytes[0])) {
                //parser ascii code
                model = reader.parserAsciiStl(bytes[0]);
            }else{
                // parser bin code
                model = reader.parserBinStl(bytes[0]);
            }
            return model;
        }

        @Override
        protected void onPostExecute(STLModel model) {
            listener.onFinished(model);
        }
    }


}
