package com.study.xuan.gifshow.widget.stlview.widget;

import android.content.Context;
import android.util.Log;

import com.study.xuan.gifshow.widget.stlview.callback.OnReadListener;
import com.study.xuan.gifshow.widget.stlview.operate.ISTLReader;
import com.study.xuan.gifshow.widget.stlview.operate.ReaderHandler;
import com.study.xuan.gifshow.widget.stlview.operate.STLReader;
import com.study.xuan.gifshow.widget.stlview.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author : xuan.
 * Date : 2017/12/14.
 * Description :3dView 构造器
 */

public class STLViewBuilder {
    private STLView stlView;
    private static final int TYPE_FILE = 0;
    private static final int TYPE_BYTE = 1;
    private static final int TYPE_STREAM = 2;
    private ReaderHandler handler;
    private OnReadListener listener;
    private File file;
    private byte[] bytes;
    private InputStream is;
    private ISTLReader reader;
    private boolean hasSource;
    private int type;
    private Object obj;

    public STLViewBuilder(STLView stlView) {
        this.stlView = stlView;
        this.listener = stlView.getReadListener();
    }

    public static STLViewBuilder init(STLView stlView) {
        return new STLViewBuilder(stlView);
    }

    public STLViewBuilder Reader(ISTLReader reader) {
        this.reader = reader;
        this.reader.setCallBack(this.listener);
        return this;
    }

    public STLViewBuilder Byte(byte[] bytes) {
        hasSource = true;
        type = TYPE_BYTE;
        this.bytes = bytes;
        return this;
    }

    public STLViewBuilder File(File file) {
        type = TYPE_FILE;
        hasSource = true;
        this.file = file;
        return this;
    }

    public STLViewBuilder Assets(Context context, String fileName) {
        try {
            return InputStream(context.getAssets().open(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public STLViewBuilder InputStream(InputStream inputStream) {
        type = TYPE_STREAM;
        hasSource = true;
        this.is = inputStream;
        return this;
    }

    public STLViewBuilder build() {
        if (!hasSource) {
            Log.e("VRShow", "has not set the source file!");
            return this;
        }
        if (reader == null) {
            reader = new STLReader();
            reader.setCallBack(this.listener);
        }
        handler = new ReaderHandler(reader, listener);
        try {
            switch (type) {
                case TYPE_BYTE:
                    handler.read(bytes);
                    break;
                case TYPE_FILE:
                    handler.read(IOUtils.toByteArray(new FileInputStream(file)));
                    break;
                case TYPE_STREAM:
                    handler.read(IOUtils.toByteArray(is));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }
}
