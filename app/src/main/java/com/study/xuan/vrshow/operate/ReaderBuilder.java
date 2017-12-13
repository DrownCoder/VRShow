package com.study.xuan.vrshow.operate;

import android.util.Log;

import com.study.xuan.vrshow.callback.onReadListener;
import com.study.xuan.vrshow.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ReaderBuilder {
        private static final int TYPE_FILE = 0;
        private static final int TYPE_BYTE = 1;
        private ReaderHandler handler;
        private onReadListener listener;
        private File file;
        private byte[] bytes;
        private ISTLReader reader;
        private boolean hasSource;
        private int type;

        public ReaderBuilder Reader(ISTLReader reader) {
            this.reader = reader;
            return this;
        }

        public ReaderBuilder CallBack(onReadListener listener) {
            this.listener = listener;
            if (reader != null) {
                reader.setCallBack(listener);
            }
            return this;
        }

        public ReaderBuilder Byte(byte[] bytes) {
            hasSource = true;
            type = TYPE_BYTE;
            this.bytes = bytes;
            return this;
        }

        public ReaderBuilder File(File file) {
            type = TYPE_FILE;
            hasSource = true;
            this.file = file;
            return this;
        }

        public ReaderBuilder build() {
            if (!hasSource) {
                Log.e("VRShow", "has not set the source file!");
                return this;
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
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }
    }