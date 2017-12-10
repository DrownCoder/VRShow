package com.study.xuan.vrshow.operate;

import android.os.AsyncTask;

import com.study.xuan.vrshow.model.Model;
import com.study.xuan.vrshow.util.STLUtils;

/**
 * Author : xuan.
 * Date : 2017/12/10.
 * Description : 异步加载Stl文件
 */
public class ReaderHandler {
    private ISTLReader reader;
    private AsyncTask backWorker;

    public ReaderHandler(ISTLReader reader) {
        this.reader = reader;
    }

    private class ReaderTask extends AsyncTask<byte[], Integer,Model>{

        @Override
        protected Model doInBackground(byte[]... bytes) {
            Model model = null;
            if (STLUtils.isAscii(bytes[0])) {
                //parser ascii code
                model = reader.parserAsciiStl(bytes[0]);
            }else{
                // parser bin code
                model = reader.parserBinStl(bytes[0]);
            }
            return model;
        }
    }
}
