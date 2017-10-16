package ocrtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.scannerdemo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class TestOrcActivity extends AppCompatActivity {
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATA_PATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String tessdata = DATA_PATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     * 'chi_sim'================================'eng'
     */
    private static final String DEFAULT_LANGUAGE = "eng";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;
    CameraView mCameraView;
    ImageView mImageView;
    TextView tv_show_text;
    public static String RESULT_KEY = "result";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_test_main);
        mCameraView = (CameraView) findViewById(R.id.main_camera);
        mImageView = (ImageView) findViewById(R.id.main_image);
        tv_show_text = (TextView) findViewById(R.id.tv_show_text);
        mCameraView.setTag(R.id.tag_img, mImageView);
        mCameraView.setTag(R.id.tag_text, tv_show_text);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
            }
        }
        copyToSD(LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
        mCameraView.setOcrResult(new OcrResult() {
            @Override
            public void onResult(String result) {
                if (!TextUtils.isEmpty(result)) {
                    //匹配的数据
                    String reg = "[^0-9a-zA-Z.，()“”、：；\\u4e00-\\u9fa5]";
                    int totalLen = result.length();
                    result = result.replaceAll(reg, "");
                    result = result.trim();
                    int validLen = result.length();
                    double ratio = (double) validLen / totalLen;
                    if (ratio > 0.6) {
                        showResult(result);
                    }
                }
            }
        });

    }

    /**
     * 主线程更新UI
     *
     * @param mContext
     */
    private void showResult(final String mContext) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_show_text.setText(mContext);
            }
        });
    }

    /**
     * 请求到权限后在这里复制识别库
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    copyToSD(LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 将assets中的识别库复制到SD卡中
     *
     * @param path 要存放在SD卡中的 完整的文件名。这里是"/storage/emulated/0//tessdata/chi_sim.traineddata"
     * @param name assets中的文件名 这里是 "chi_sim.traineddata"
     */
    public void copyToSD(String path, String name) {

        //如果存在就删掉
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        if (!f.exists()) {
            File p = new File(f.getParent());
            if (!p.exists()) {
                p.mkdirs();
            }
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = this.getAssets().open(name);
            File file = new File(path);
            os = new FileOutputStream(file);
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
