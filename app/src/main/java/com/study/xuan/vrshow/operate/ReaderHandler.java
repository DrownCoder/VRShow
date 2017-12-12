package com.study.xuan.vrshow.operate;

import android.os.AsyncTask;
import android.util.Log;

import com.study.xuan.vrshow.callback.onReadListener;
import com.study.xuan.vrshow.model.Model;
import com.study.xuan.vrshow.util.IOUtils;
import com.study.xuan.vrshow.util.STLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Author : xuan.
 * Date : 2017/12/10.
 * Description : 异步加载Stl文件
 */
public class ReaderHandler {
    private ISTLReader reader;
    private ReaderTask backWorker;
    private onReadListener listener;

    public ReaderHandler(ISTLReader reader, onReadListener listener) {
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

    private class ReaderTask extends AsyncTask<byte[], Integer,Model>{
        @Override
        protected void onPreExecute() {
            listener.onstart();
        }

        @Override
        protected Model doInBackground(byte[]... bytes) {
            Model model;
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
        protected void onPostExecute(Model model) {
            listener.onFinished(model);
        }
    }


}
