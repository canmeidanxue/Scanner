package com.bulesky.zxinglibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.bulesky.zxinglibrary.camera.CameraManager;
import com.bulesky.zxinglibrary.camera.open.CameraFacing;
import com.bulesky.zxinglibrary.common.Tool;
import com.bulesky.zxinglibrary.decode.DecodeFormatManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import java.io.IOException;
import java.util.Collection;

/**
 * 自定义扫描界面
 * update by hsl on 2017-10-5
 */
public class ScannerView extends FrameLayout implements SurfaceHolder.Callback {

    private static final String TAG = ScannerView.class.getSimpleName();

    private SurfaceView mSurfaceView;
    private ViewfinderView mViewfinderView;

    private boolean hasSurface;
    private CameraManager mCameraManager;
    private ScannerViewHandler mScannerViewHandler;
    private BeepManager mBeepManager;
    private int mMediaResId;
    private OnScannerCompletionListener mScannerCompletionListener;

    private boolean lightMode = false;//闪光灯，默认关闭
    private int laserFrameWidth, laserFrameHeight;//扫描框大小
    private int laserFrameTopMargin;//扫描框离屏幕上方距离
    private Collection<BarcodeFormat> decodeFormats;//解码类型
    //    private boolean mShowResThumbnail = false;//扫描成功是否显示缩略图
    private CameraFacing mCameraFacing = CameraFacing.BACK;//默认后置摄像头
    private boolean mScanFullScreen;//全屏扫描
    private boolean invertScan;//扫描反色二维码（黑底白色码）

    public ScannerView(Context context) {
        this(context, null);
    }

    public ScannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        hasSurface = false;

        mSurfaceView = new SurfaceView(context, attrs, defStyle);
        addView(mSurfaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mViewfinderView = new ViewfinderView(context, attrs);
        addView(mViewfinderView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void onResume() {
        mCameraManager = new CameraManager(getContext(), mCameraFacing);
        mCameraManager.setLaserFrameTopMargin(laserFrameTopMargin);//扫描框与屏幕距离
        mCameraManager.setScanFullScreen(mScanFullScreen);//是否全屏扫描
        mCameraManager.setInvertScan(invertScan);
        mViewfinderView.setCameraManager(mCameraManager);
        if (mBeepManager != null) mBeepManager.updatePrefs();

        mScannerViewHandler = null;

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            surfaceHolder.addCallback(this);
        }
    }

    public void onPause() {
        if (mScannerViewHandler != null) {
            mScannerViewHandler.quitSynchronously();
            mScannerViewHandler = null;
        }
        if (mBeepManager != null) mBeepManager.close();
        mCameraManager.closeDriver();
        mViewfinderView.laserLineBitmapRecycle();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            mCameraManager.setTorch(lightMode);
            // Creating the mScannerViewHandler starts the preview, which can also throw a
            // RuntimeException.
            if (mScannerViewHandler == null) {
                mScannerViewHandler = new ScannerViewHandler(this, decodeFormats, mCameraManager);
            }
            //设置扫描框大小
            if (laserFrameWidth > 0 && laserFrameHeight > 0)
                mCameraManager.setManualFramingRect(laserFrameWidth, laserFrameHeight);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     */
    void handleDecode(Result rawResult) {
        //扫描成功
        if (mScannerCompletionListener != null) {
            //转换结果
            mScannerCompletionListener.OnScannerCompletion(rawResult);
        }
        if (mMediaResId != 0) {
            if (mBeepManager == null) {
                mBeepManager = new BeepManager(getContext());
                mBeepManager.setMediaResId(mMediaResId);
            }
            mBeepManager.playBeepSoundAndVibrate();
        }
    }
    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult
     */
    void handleDecodeC(String rawResult) {
        //扫描成功
        if (mScannerCompletionListener != null) {
            //转换结果
            mScannerCompletionListener.OnScannerCompletion(rawResult);
        }
        if (mMediaResId != 0) {
            if (mBeepManager == null) {
                mBeepManager = new BeepManager(getContext());
                mBeepManager.setMediaResId(mMediaResId);
            }
            mBeepManager.playBeepSoundAndVibrate();
        }
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of
     * the barcode.
     *
     * @param barcode     A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult   The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(Tool.color.RESULT_POINTS);
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
            } else if (points.length == 4
                    && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and
                // metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
                drawLine(canvas, paint, points[2], points[3], scaleFactor);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                    }
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(), scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        hasSurface = false;
        if (!hasSurface && surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
        }
    }

    /**
     * 设置扫描成功监听器
     *
     * @param listener
     * @return
     */
    public ScannerView setOnScannerCompletionListener(OnScannerCompletionListener listener) {
        this.mScannerCompletionListener = listener;
        return this;
    }

    /**
     * 设置扫描线颜色
     *
     * @param laserColor
     */
    public ScannerView setLaserColor(int laserColor) {
        mViewfinderView.setLaserColor(laserColor);
        return this;
    }

    /**
     * 设置线形扫描线资源
     *
     * @param laserLineResId resId
     */
    public ScannerView setLaserLineResId(int laserLineResId) {
        mViewfinderView.setLaserLineResId(laserLineResId);
        return this;
    }

    /**
     * 设置网格扫描线资源
     *
     * @param laserLineResId resId
     */
    public ScannerView setLaserGridLineResId(int laserLineResId) {
        mViewfinderView.setLaserGridLineResId(laserLineResId);
        return this;
    }

    /**
     * 设置扫描线高度
     *
     * @param laserLineHeight dp
     */
    public ScannerView setLaserLineHeight(int laserLineHeight) {
        mViewfinderView.setLaserLineHeight(laserLineHeight);
        return this;
    }

    /**
     * 设置扫描框4角颜色
     *
     * @param laserFrameBoundColor
     */
    public ScannerView setLaserFrameBoundColor(int laserFrameBoundColor) {
        mViewfinderView.setLaserFrameBoundColor(laserFrameBoundColor);
        return this;
    }

    /**
     * 设置扫描框4角长度
     *
     * @param laserFrameCornerLength dp
     */
    public ScannerView setLaserFrameCornerLength(int laserFrameCornerLength) {
        mViewfinderView.setLaserFrameCornerLength(laserFrameCornerLength);
        return this;
    }

    /**
     * 设置扫描框4角宽度
     *
     * @param laserFrameCornerWidth dp
     */
    public ScannerView setLaserFrameCornerWidth(int laserFrameCornerWidth) {
        mViewfinderView.setLaserFrameCornerWidth(laserFrameCornerWidth);
        return this;
    }

    /**
     * 设置文字颜色
     *
     * @param textColor 文字颜色
     */
    public ScannerView setDrawTextColor(int textColor) {
        mViewfinderView.setDrawTextColor(textColor);
        return this;
    }

    /**
     * 设置文字大小
     *
     * @param textSize 文字大小 sp
     */
    public ScannerView setDrawTextSize(int textSize) {
        mViewfinderView.setDrawTextSize(textSize);
        return this;
    }

    /**
     * 设置文字
     *
     * @param text
     * @param isBottom 是否在扫描框下方
     */
    public ScannerView setDrawText(String text, boolean isBottom) {
        return setDrawText(text, isBottom, 0);
    }

    /**
     * 设置文字
     *
     * @param text
     * @param isBottom   是否在扫描框下方
     * @param textMargin 离扫描框间距 dp
     */
    public ScannerView setDrawText(String text, boolean isBottom, int textMargin) {
        return setDrawText(text, 0, 0, isBottom, textMargin);
    }

    /**
     * 设置文字
     *
     * @param text
     * @param textSize   文字大小 sp
     * @param textColor  文字颜色
     * @param isBottom   是否在扫描框下方
     * @param textMargin 离扫描框间距 dp
     */
    public ScannerView setDrawText(String text, int textSize, int textColor, boolean isBottom, int textMargin) {
        mViewfinderView.setDrawText(text, textSize, textColor, isBottom, textMargin);
        return this;
    }

    /**
     * 设置扫描完成播放声音
     *
     * @param mediaResId
     */
    public ScannerView setMediaResId(int mediaResId) {
        this.mMediaResId = mediaResId;
        return this;
    }

    /**
     * 切换闪光灯
     *
     * @param mode true开；false关
     */
    public ScannerView toggleLight(boolean mode) {
        this.lightMode = mode;
        if (mCameraManager != null) mCameraManager.setTorch(lightMode);
        return this;
    }

    /**
     * 设置扫描框大小
     *
     * @param width  dp
     * @param height dp
     */
    public ScannerView setLaserFrameSize(int width, int height) {
        this.laserFrameWidth = Tool.dp2px(getContext(), width);
        this.laserFrameHeight = Tool.dp2px(getContext(), height);
        return this;
    }

    /**
     * 设置扫描框与屏幕距离
     *
     * @param laserFrameTopMargin
     */
    public ScannerView setLaserFrameTopMargin(int laserFrameTopMargin) {
        this.laserFrameTopMargin = Tool.dp2px(getContext(), laserFrameTopMargin);
        return this;
    }

    /**
     * 设置扫描解码类型（二维码、一维码、商品条码）
     *
     * @param scanMode {@linkplain Tool.ScanMode mode}
     * @return
     */
    public ScannerView setScanMode(String scanMode) {
        this.decodeFormats = DecodeFormatManager.parseDecodeFormats(scanMode);
        return this;
    }

    /**
     * 设置扫描解码类型
     *
     * @param barcodeFormat
     * @return
     */
    public ScannerView setScanMode(BarcodeFormat... barcodeFormat) {
        this.decodeFormats = DecodeFormatManager.parseDecodeFormats(barcodeFormat);
        return this;
    }

    /**
     * 设置扫描框线移动间距，每毫秒移动 moveSpeed 像素
     *
     * @param moveSpeed px
     * @return
     */
    public ScannerView setLaserMoveSpeed(int moveSpeed) {
        this.mViewfinderView.setLaserMoveSpeed(moveSpeed);
        return this;
    }

    /**
     * 设置扫描摄像头，默认后置
     *
     * @param cameraFacing
     * @return
     */
    public ScannerView setCameraFacing(CameraFacing cameraFacing) {
        this.mCameraFacing = cameraFacing;
        return this;
    }

    /**
     * 是否全屏扫描
     *
     * @param scanFullScreen
     * @return
     */
    public ScannerView isScanFullScreen(boolean scanFullScreen) {
        this.mScanFullScreen = scanFullScreen;
        return this;
    }

    /**
     * 设置是否显示灯
     *
     * @param isShow
     * @return
     */
    public ScannerView setFlashBitmap(boolean isShow) {
        this.mViewfinderView.setFlashBitmap(isShow);
        return this;
    }

    /**
     * 在经过一段延迟后重置相机以进行下一次扫描。 成功扫描过后可调用此方法立刻准备进行下次扫描
     *
     * @param delayMS 毫秒
     */
    public void restartPreviewAfterDelay(long delayMS) {
        if (mScannerViewHandler != null)
            mScannerViewHandler.sendEmptyMessageDelayed(Tool.RESTART_PREVIEW, delayMS);
    }


    void drawViewfinder() {
        mViewfinderView.drawViewfinder();
    }

}
