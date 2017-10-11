package activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.scanlibrary.R;
import com.google.zxing.Result;

import java.io.IOException;

import interfaces.OnDecodeListener;
import interfaces.OnScanerListener;
import scanner.CameraManager;
import scanner.CaptureActivityHandler;
import tools.Tool;

import static com.example.scanlibrary.R.id.qr_code_header_black_pic;

/**
 * 扫码界面
 */
public class ScanerCodeActivity extends Activity implements SurfaceHolder.Callback {

    private static final float BEEP_VOLUME = 0.50f;
    private static final long VIBRATE_DURATION = 200L;
    private static final String TAG = ScanerCodeActivity.class.getSimpleName();
    private static OnScanerListener mScanerListener;
    private final int CHOOSE_PICTURE = 1003;
    boolean flag = true;
    private CaptureActivityHandler handler;
    private boolean hasSurface;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;
    private int x = 0;
    private int y = 0;
    private int mCropWidth = 0;
    private int mCropHeight = 0;
    private RelativeLayout mContainer = null;
    private RelativeLayout mCropLayout = null;
    private String mResult;
    private String TYPE_SCAN = "扫描";
    private String TYPE_ALBUM = "相册中获取";


    /**
     * 识别结果
     *
     * @param scanerListener
     */
    public static void setScanerListener(OnScanerListener scanerListener) {
        mScanerListener = scanerListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_test);
        //界面控件初始化
        initView();
        //扫描动画初始化
        initScanerAnimation();
        //初始化 CameraManager
        CameraManager.init(ScanerCodeActivity.this);
        hasSurface = false;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        //初始化surfaceView
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            //初始化Camera
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        mScanerListener = null;
        super.onDestroy();
    }

    private void initView() {
//        ImageView mIvLight = (ImageView) findViewById(R.id.top_mask);
        mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
        mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);
        //请求Camera权限 与 文件读写 权限
        if (ContextCompat.checkSelfPermission(ScanerCodeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(ScanerCodeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ScanerCodeActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


    }

    private void initScanerAnimation() {
//        ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
//        MyAnimationTool.ScaleUpDowm(mQrLineView);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCropWidth() {
        return mCropWidth;
    }

    public int getCropHeight() {
        return mCropHeight;
    }

    public void setCropWidth(int cropWidth) {
        this.mCropWidth = cropWidth;
        CameraManager.FRAME_WIDTH = mCropWidth;

    }

    public void setCropHeight(int cropHeight) {
        this.mCropHeight = cropHeight;
        CameraManager.FRAME_HEIGHT = mCropHeight;
    }

    private void light() {
        if (flag) {
            flag = false;
            // 开闪光灯
            CameraManager.get().openLight();
        } else {
            flag = true;
            // 关闪光灯
            CameraManager.get().offLight();
        }

    }


    public void btn(View view) {
        int i = view.getId();
//        if (i == R.id.top_mask) {
//            light();
//        } else if (i == R.id.top_back) {
//            finish();
//        } else if (i == R.id.top_openpicture) {
//            getPicture();
//        }
        if (i == qr_code_header_black_pic) {
            getPicture();
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);

            Point point = CameraManager.get().getCameraResolution();
            int width = point.y;
            int height = point.x;
            int x = mCropLayout.getLeft() * width / mContainer.getWidth();
            int y = mCropLayout.getTop() * height / mContainer.getHeight();
            int cropWidth = mCropLayout.getWidth() * width
                    / mContainer.getWidth();
            int cropHeight = mCropLayout.getHeight() * height
                    / mContainer.getHeight();
            setX(x);
            setY(y);
            setCropWidth(cropWidth);
            setCropHeight(cropHeight);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(ScanerCodeActivity.this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public Handler getHandler() {
        return handler;
    }

    /***
     * 调用系统相册
     */
    private void getPicture() {
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openAlbumIntent.setType("image/*");
        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ContentResolver resolver = getContentResolver();
//            // 照片的原始资源地址
            Uri originalUri = data.getData();
            String path = Tool.getPath(ScanerCodeActivity.this, originalUri);
            Tool.parsePhoto(path, new OnDecodeListener() {
                @Override
                public void onResult(String result) {
                    if (TextUtils.isEmpty(result)) {
                        mScanerListener.onFail(TYPE_ALBUM, "获取失败");
                    } else {
                        // 识别出图片二维码/条码
                        mScanerListener.onFail(TYPE_ALBUM, result);
                    }
                    finish();
                }
            });
        }
    }

    public void handleDecode(Result result) {
        playBeepSoundAndVibrate();
        this.mResult = result.getText();
        Log.v("二维码/条形码 扫描结果", mResult);
        if (mScanerListener == null) {
            Toast.makeText(ScanerCodeActivity.this, mResult, Toast.LENGTH_SHORT).show();
            String type = result.getBarcodeFormat().name();
            String realContent = result.getText();
            if ("QR_CODE".equals(type)) {
                Toast.makeText(ScanerCodeActivity.this, realContent, Toast.LENGTH_SHORT).show();
            } else if ("EAN_13".equals(type)) {
                Toast.makeText(ScanerCodeActivity.this, realContent, Toast.LENGTH_SHORT).show();
            }
            handler.sendEmptyMessage(R.id.restart_preview);
        } else {
            mScanerListener.onSuccess(TYPE_SCAN, result);
            finish();
        }
    }

    //扫描声音定制
    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.seekTo(0);
                }
            });

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }
}