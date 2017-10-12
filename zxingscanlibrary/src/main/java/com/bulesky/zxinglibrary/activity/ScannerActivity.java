package com.bulesky.zxinglibrary.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bulesky.zxinglibrary.R;
import com.bulesky.zxinglibrary.album.PictureTotalActivity;
import com.bulesky.zxinglibrary.common.Tool;
import com.bulesky.zxinglibrary.decode.QRDecode;
import com.bulesky.zxinglibrary.view.OnScannerCompletionListener;
import com.bulesky.zxinglibrary.view.ScannerView;
import com.google.zxing.Result;


public class ScannerActivity extends Activity implements OnScannerCompletionListener, SensorEventListener {
    public static final String EXTRA_LASER_LINE_MODE = "extra_laser_line_mode";
    public static final String EXTRA_SCAN_MODE = "extra_scan_mode";
    public static final String EXTRA_SHOW_THUMBNAIL = "EXTRA_SHOW_THUMBNAIL";
    public static final String EXTRA_SCAN_FULL_SCREEN = "EXTRA_SCAN_FULL_SCREEN";

    public static final int EXTRA_LASER_LINE_MODE_0 = 0;
    public static final int EXTRA_LASER_LINE_MODE_1 = 1;
    public static final int EXTRA_LASER_LINE_MODE_2 = 2;
    //扫描全部（条形码和二维码）
    public static final int EXTRA_SCAN_MODE_0 = 0;
    //扫描条形码
    public static final int EXTRA_SCAN_MODE_1 = 1;
    //扫描二维码
    public static final int EXTRA_SCAN_MODE_2 = 2;
    private String TAG = ScannerActivity.class.getSimpleName();

    boolean showThumbnail = false;
    private ScannerView mScannerView;
    private Result mLastResult;
    private TextView tv_aulbum;
    private SensorManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        mScannerView = findViewById(R.id.scanner_view);
        mScannerView.setOnScannerCompletionListener(this);
        tv_aulbum = findViewById(R.id.tv_aulbum);
        Bundle extras = getIntent().getExtras();
        int laserMode = extras.getInt(EXTRA_LASER_LINE_MODE);
        int scanMode = extras.getInt(EXTRA_SCAN_MODE);
        showThumbnail = extras.getBoolean(EXTRA_SHOW_THUMBNAIL);
        mScannerView.setMediaResId(R.raw.weixin_beep);//设置扫描成功的声音
        mScannerView.setDrawText("将二维码放入框内，即可自动扫描", true);
        mScannerView.setDrawTextSize(12);
        mScannerView.setDrawTextColor(Color.WHITE);

        if (scanMode == EXTRA_SCAN_MODE_1) {
            //二维码
            mScannerView.setScanMode(Tool.ScanMode.QR_CODE_MODE);
        } else if (scanMode == EXTRA_SCAN_MODE_2) {
            //一维码
            mScannerView.setScanMode(Tool.ScanMode.PRODUCT_MODE);
        }

        //全屏识别
        mScannerView.isScanFullScreen(extras.getBoolean(EXTRA_SCAN_FULL_SCREEN));
        //隐藏扫描框
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
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        //光线传感器 参数指定获取什么类型的传感器，也可以指定获取所有传感器
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        //注册一个监听器
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);//第三参数代表采样频率，频率越高，精度越
        tv_aulbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureTotalActivity.gotoActivity(ScannerActivity.this);
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
    protected void onDestroy() {
        sm.unregisterListener(this);
        super.onDestroy();
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
            if (requestCode == PictureTotalActivity.REQUEST_CODE_SELECT_PICTURE) {
                String picturePath = data.getStringExtra(PictureTotalActivity
                        .EXTRA_PICTURE_PATH);
                QRDecode.decodeQR(picturePath, this);
            }
        }
    }

    @Override
    public void OnScannerCompletion(Result rawResult) {
        if (null != rawResult) {
            Toast.makeText(ScannerActivity.this, rawResult.toString(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ScannerActivity.this, "未识别到二维码信息", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //不同类型的传感器，values的意义是不一样的 这里指的是光线的强弱
        float light = sensorEvent.values[0];
        if (light < 50) {
            mScannerView.setFlashBitmap(true);
        } else {
            mScannerView.setFlashBitmap(false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
