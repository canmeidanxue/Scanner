/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bulesky.zxinglibrary.decode;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.bulesky.zxinglibrary.camera.CameraManager;
import com.bulesky.zxinglibrary.common.Tool;
import com.duoyi.qrdecode.BarcodeFormat;
import com.duoyi.qrdecode.DecodeEntry;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;

import java.util.Map;

final class DecodeHandler extends Handler {

    private final CameraManager cameraManager;
    private final Handler scannerViewHandler;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    DecodeHandler(CameraManager cameraManager, Handler scannerViewHandler,
                  Map<DecodeHintType, Object> hints) {
        this.cameraManager = cameraManager;
        this.scannerViewHandler = scannerViewHandler;
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
    }

    @Override
    public void handleMessage(Message message) {
        if (message == null || !running) {
            return;
        }
        switch (message.what) {
            case Tool.DECODE:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case Tool.QUIT:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * 捕捉画面并解码<br/>
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        //此处为java解析
        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
//        if (cameraManager.getContext().getResources().getConfiguration().orientation ==
//                Configuration.ORIENTATION_PORTRAIT) {
//            byte[] rotatedData = new byte[data.length];
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++)
//                    rotatedData[x * height + height - y - 1] = data[x + y * width];
//            }
//            // 宽高也要调整
//            int tmp = width;
//            width = height;
//            height = tmp;
//            data = rotatedData;
//        }

//        Result rawResult = null;
//        PlanarYUVLuminanceSource source = cameraManager.buildLuminanceSource(data, width, height);
//        if (source != null) {
//            //觉得HybridBinarizer速度慢,改成GlobalHistogramBinarizer
//            BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
//            try {
//                rawResult = multiFormatReader.decodeWithState(bitmap);
//            } catch (ReaderException re) {
//                // continue
//            } finally {
//                multiFormatReader.reset();
//            }
//        }

//        Handler handler = scannerViewHandler;
//        if (rawResult != null) {
//            if (handler != null) {
//                //会向 ScannerViewHandler 发消息
//                Message message = Message.obtain(handler, Tool.DECODE_SUCCEEDED, rawResult);
//                Bundle bundle = new Bundle();
//                message.setData(bundle);
//                message.sendToTarget();
//            }
//        } else {
//            if (handler != null) {
//                Message message = Message.obtain(handler, Tool.DECODE_FAILED);
//                message.sendToTarget();
//            }
//        }

        //测试代码 此处为C代码解析
        Handler handler = scannerViewHandler;
        Rect rect = cameraManager.getRealFramingRect();
        BarcodeFormat barcodeFormat = new BarcodeFormat();
        barcodeFormat.add(BarcodeFormat.QRCODE);
        barcodeFormat.add(BarcodeFormat.BARCODE);
       String result = DecodeEntry.getDecodeResult(barcodeFormat, data, width, height, rect.left, rect.top,
                rect.width(), rect.height());
        if (!TextUtils.isEmpty(result)) {
            if (handler != null) {
                //会向 ScannerViewHandler 发消息
                Message message = Message.obtain(handler, Tool.DECODE_SUCCEEDED, result);
                Bundle bundle = new Bundle();
                message.setData(bundle);
                message.sendToTarget();
            }
        }else {
            if (handler != null) {
                Message message = Message.obtain(handler, Tool.DECODE_FAILED);
                message.sendToTarget();
            }
        }
    }


}
