package com.example.administrator.scannerdemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.scannerdemo.R;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * update by hsl on 2017-9-28 18:24:24
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static final String DEFAULT_LANGUAGE = "chi_sim";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean isPreviewOn;
    //默认预览尺寸
    private int screenWidth = 1920;
    private int screenHeight = 1080;
    //帧率
    private int frameRate = 30;

    private TextView textView;
    private ImageView imageView;

    public CameraView(Context context) {
        super(context);
        init(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.heightPixels;
        screenHeight = dm.widthPixels;

        mHolder = getHolder();
        //设置SurfaceView 的SurfaceHolder的回调函数
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Surface创建时开启Camera
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //设置Camera基本参数
        if (mCamera != null)
            initCameraParams();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            release();
        } catch (Exception e) {
        }
    }

    private boolean isScanning = false;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                textView.setText((String) msg.obj);
            }
            if (msg.what==1){
                imageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    /**
     * Camera帧数据回调用
     */
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        camera.addCallbackBuffer(data);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //识别中不处理其他帧数据
                if (!isScanning) {
                    isScanning = true;
                    try {
                        //获取Camera预览尺寸
                        Camera.Size size = camera.getParameters().getPreviewSize();
                        //将帧数据转为bitmap
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if (image != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            //这里返回的照片默认横向的，先将图片旋转90度
                            bmp = rotateToDegrees(bmp, 90);
                            //然后裁切出需要的区域，具体区域要和UI布局中配合，这里取图片正中间，宽度取图片的一半，高度这里用的适配数据，可以自定义
                            bmp = bitmapCrop(bmp, bmp.getWidth() / 2 - (int) getResources().getDimension(R.dimen.x160) / 2, bmp.getHeight() / 2 - (int) getResources().getDimension(R.dimen.x50) / 2, (int) getResources().getDimension(R.dimen.x160), (int) getResources().getDimension(R.dimen.x50));
                            if (bmp == null)
                                return;

                            //将裁切的图片显示出来（测试用，需要为CameraView  setTag（ImageView））
                            imageView = (ImageView) getTag(R.id.tag_img);
                            Message message = handler.obtainMessage();
                            message.what=1;
                            message.obj=bmp;
                            handler.sendMessage(message);

                            textView = (TextView) getTag(R.id.tag_text);
                            stream.close();
                            //灰化处理
                            bmp = ImageFilter.grayScale(bmp);
                            //二值化处理
                            bmp = ImageFilter.binaryzation(bmp);
                            //开始识别
                            TessBaseAPI baseApi = new TessBaseAPI();
                            //初始化OCR的字体数据，DATA_PATH为路径，DEFAULT_LANGUAGE指明要用的字体库（不用加后缀）
                            if (baseApi.init(DATA_PATH, DEFAULT_LANGUAGE)) {
                                //设置识别模式
                                baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
                                //设置要识别的图片
                                baseApi.setImage(bmp);
                                //开始识别
                                String result = baseApi.getUTF8Text();
                                baseApi.clear();
                                baseApi.end();
                                isQualified(result);
                            }
                        }
                    } catch (Exception ex) {
                        isScanning = false;
                    }
                }
            }
        }
        ).start();

    }


    /**
     * 获取字符串中的有效字符
     */
    public void isQualified(String str) {
        if (str == null || str.length() == 0) {
            isScanning = false;
            return;
        }
        String reg = "[^0-9a-zA-Z.，()“”、：；\\u4e00-\\u9fa5]";
        int totalLen = str.length();
        str = str.replaceAll(reg, "");
        str = str.trim();
        int validLen = str.length();
        double ratio = (double) validLen / totalLen;
        if (ratio > 0.6) {
            isScanning = true;
            Message message = handler.obtainMessage();
            message.what = 0;
            message.obj = str.toString();
            handler.sendMessage(message);
        } else {
            isScanning = false;
        }
    }

    /**
     * 获取字符串中的手机号
     */
    public String getTelnum(String sParam) {

        if (sParam.length() <= 0)
            return "";
        Pattern pattern = Pattern.compile("(1|861)(3|5|8)\\d{9}$*");
        Matcher matcher = pattern.matcher(sParam);
        StringBuffer bf = new StringBuffer();
        while (matcher.find()) {
            bf.append(matcher.group()).append(",");
        }
        int len = bf.length();
        if (len > 0) {
            bf.deleteCharAt(len - 1);
        }
        return bf.toString();
    }

    /**
     * Bitmap裁剪
     *
     * @param bitmap 原图
     * @param width  宽
     * @param height 高
     */
    public static Bitmap bitmapCrop(Bitmap bitmap, int left, int top, int width, int height) {
        if (null == bitmap || width <= 0 || height < 0) {
            return null;
        }
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();
        if (widthOrg >= width && heightOrg >= height) {
            try {
                bitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
            } catch (Exception e) {
                return null;
            }
        }
        return bitmap;
    }

    /**
     * 图片旋转
     *
     * @param tmpBitmap
     * @param degrees
     * @return
     */
    public static Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degrees);
        return Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix,
                true);
    }


    /**
     * 摄像头配置
     */
    public void initCameraParams() {
        stopPreview();
        //获取camera参数
        Camera.Parameters camParams = mCamera.getParameters();
        List<Camera.Size> sizes = camParams.getSupportedPreviewSizes();
        //确定前面定义的预览宽高是camera支持的，不支持取就更大的
        for (int i = 0; i < sizes.size(); i++) {
            if ((sizes.get(i).width >= screenWidth && sizes.get(i).height >= screenHeight) || i == sizes.size() - 1) {
                screenWidth = sizes.get(i).width;
                screenHeight = sizes.get(i).height;
                break;
            }
        }
        //设置最终确定的预览大小
        camParams.setPreviewSize(screenWidth, screenHeight);
        //设置帧率
        camParams.setPreviewFrameRate(frameRate);
        //启用参数
        mCamera.setParameters(camParams);
        mCamera.setDisplayOrientation(90);
        //开始预览
        startPreview();
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        try {
            mCamera.addCallbackBuffer(new byte[((screenWidth * screenHeight) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.setPreviewDisplay(mHolder);//set the surface to be used for live preview
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCB);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }

    /**
     * 打开指定摄像头
     */
    public void openCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    mCamera = Camera.open(cameraId);
                } catch (Exception e) {
                    if (mCamera != null) {
                        mCamera.release();
                        mCamera = null;
                    }
                }
                break;
            }
        }
    }

    /**
     * 摄像头自动聚焦
     */
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            postDelayed(doAutoFocus, 500);
        }
    };
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null) {
                try {
                    mCamera.autoFocus(autoFocusCB);
                } catch (Exception e) {
                }
            }
        }
    };

    /**
     * 释放
     */
    public void release() {
        if (isPreviewOn && mCamera != null) {
            isPreviewOn = false;
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

}
