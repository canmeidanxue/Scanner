package com.bulesky.zxinglibrary.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bulesky.zxinglibrary.R;
import com.bulesky.zxinglibrary.common.Tool;
import com.bulesky.zxinglibrary.decode.QRDecode;
import com.bulesky.zxinglibrary.picture.PickPictureTotalActivity;
import com.bulesky.zxinglibrary.view.OnScannerCompletionListener;
import com.bulesky.zxinglibrary.view.ScannerView;
import com.google.zxing.Result;
import com.google.zxing.result.ParsedResult;


public class ScannerActivity extends Activity implements OnScannerCompletionListener {
    public static final String EXTRA_LASER_LINE_MODE = "extra_laser_line_mode";
    public static final String EXTRA_SCAN_MODE = "extra_scan_mode";
    public static final String EXTRA_SHOW_THUMBNAIL = "EXTRA_SHOW_THUMBNAIL";
    public static final String EXTRA_SCAN_FULL_SCREEN = "EXTRA_SCAN_FULL_SCREEN";
    public static final String EXTRA_HIDE_LASER_FRAME = "EXTRA_HIDE_LASER_FRAME";

    public static final int EXTRA_LASER_LINE_MODE_0 = 0;
    public static final int EXTRA_LASER_LINE_MODE_1 = 1;
    public static final int EXTRA_LASER_LINE_MODE_2 = 2;

    public static final int EXTRA_SCAN_MODE_0 = 0;
    public static final int EXTRA_SCAN_MODE_1 = 1;
    public static final int EXTRA_SCAN_MODE_2 = 2;
    private String TAG = ScannerActivity.class.getSimpleName();

    boolean showThumbnail = false;
    private ScannerView mScannerView;
    private Result mLastResult;
    private TextView tv_aulbum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        mScannerView =  findViewById(R.id.scanner_view);
        mScannerView.setOnScannerCompletionListener(this);
        tv_aulbum = findViewById(R.id.tv_aulbum);
        Bundle extras = getIntent().getExtras();
        int laserMode = extras.getInt(EXTRA_LASER_LINE_MODE);
        int scanMode = extras.getInt(EXTRA_SCAN_MODE);
        showThumbnail = extras.getBoolean(EXTRA_SHOW_THUMBNAIL);
        mScannerView.setMediaResId(R.raw.weixin_beep);//设置扫描成功的声音
        mScannerView.setDrawText("将二维码放入框内，即可自动扫描", true);
        mScannerView.setDrawTextColor(Color.WHITE);

        if (scanMode == 1) {
            //二维码
            mScannerView.setScanMode(Tool.ScanMode.QR_CODE_MODE);
        } else if (scanMode == 2) {
            //一维码
            mScannerView.setScanMode(Tool.ScanMode.PRODUCT_MODE);
        }

        //显示扫描成功后的缩略图
        mScannerView.isShowResThumbnail(showThumbnail);
        //全屏识别
        mScannerView.isScanFullScreen(extras.getBoolean(EXTRA_SCAN_FULL_SCREEN));
        //隐藏扫描框
        mScannerView.isHideLaserFrame(extras.getBoolean(EXTRA_HIDE_LASER_FRAME));
//        mScannerView.isScanInvert(true);//扫描反色二维码
//        mScannerView.setCameraFacing(CameraFacing.FRONT);
//        mScannerView.setLaserMoveSpeed(1);//速度

//        mScannerView.setLaserFrameTopMargin(100);//扫描框与屏幕上方距离
//        mScannerView.setLaserFrameSize(400, 400);//扫描框大小
//        mScannerView.setLaserFrameCornerLength(25);//设置4角长度
//        mScannerView.setLaserLineHeight(5);//设置扫描线高度

        switch (laserMode) {
            case EXTRA_LASER_LINE_MODE_0:
                mScannerView.setLaserLineResId(R.mipmap.wx_scan_line);//线图
                break;
            case EXTRA_LASER_LINE_MODE_1:
                mScannerView.setLaserGridLineResId(R.mipmap.zfb_grid_scan_line);//网格图
                mScannerView.setLaserFrameBoundColor(0xFF26CEFF);//支付宝颜色
                break;
            case EXTRA_LASER_LINE_MODE_2:
                mScannerView.setLaserColor(Color.RED);
                break;
        }
        tv_aulbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickPictureTotalActivity.gotoActivity(ScannerActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        mScannerView.onResume();
        resetStatusView();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mScannerView.onPause();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mLastResult != null) {
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void restartPreviewAfterDelay(long delayMS) {
        mScannerView.restartPreviewAfterDelay(delayMS);
        resetStatusView();
    }

    private void resetStatusView() {
        mLastResult = null;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED && resultCode == Activity.RESULT_OK) {
            if (requestCode == PickPictureTotalActivity.REQUEST_CODE_SELECT_PICTURE) {
                String picturePath = data.getStringExtra(PickPictureTotalActivity
                        .EXTRA_PICTURE_PATH);
                QRDecode.decodeQR(picturePath, this);
            }
        }
    }
    @Override
    public void OnScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {
        Log.d(TAG, "OnScannerCompletion: "+parsedResult);
        Toast.makeText(ScannerActivity.this,parsedResult.toString(),Toast.LENGTH_SHORT).show();
        Toast.makeText(ScannerActivity.this,rawResult.toString(),Toast.LENGTH_SHORT).show();
        finish();
    }
}
