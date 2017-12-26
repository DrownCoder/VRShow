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

    private class ReaderTask extends AsyncTask<Object, Integer, STLModel> {
        @Override
        protected void onPreExecute() {
            listener.onstart();
        }

        @Override
        protected STLModel doInBackground(Object... source) {
            STLModel model = null;
            model = parserByte((byte[]) source[0]);
            return model;
        }

        private STLModel parserByte(byte[] bytes) {
            if (STLUtils.isAscii(bytes)) {
                //parser ascii code
                return reader.parserAsciiStl(bytes);
            } else {
                // parser bin code
                return reader.parserBinStl(bytes);
            }
        }

        @Override
        protected void onPostExecute(STLModel model) {
            listener.onFinished(model);
        }
    }


}
