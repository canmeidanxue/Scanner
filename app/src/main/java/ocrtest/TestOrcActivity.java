package ocrtest;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.scannerdemo.R;

import java.io.File;

import static com.example.administrator.scannerdemo.R.id.tv_show_text;


public class TestOrcActivity extends AppCompatActivity {
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATA_PATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String TESSDATA = DATA_PATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     * 'chi_sim'================================'eng'
     */
    private static String DEFAULT_LANGUAGE = "eng";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String LANGUAGE_PATH = TESSDATA + File.separator + DEFAULT_LANGUAGE_NAME;
    CameraView mCameraView;
    ImageView mImageView;
    TextView tvShowText;
    public static String RESULT_KEY = "result";
    private double mRatio = 0.6;
    public static String languageKey = "language";
    private static String chineseLanguage = "chi_sim";
    private boolean recogniseLanguage;
    private String TAG = TestOrcActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_test_main);
        mCameraView = (CameraView) findViewById(R.id.main_camera);
        mImageView = (ImageView) findViewById(R.id.main_image);
        tvShowText = (TextView) findViewById(tv_show_text);
        mCameraView.setTag(R.id.tag_img, mImageView);
        mCameraView.setTag(R.id.tag_text, tv_show_text);
        recogniseLanguage = getIntent().getBooleanExtra(languageKey, false);
        if (!recogniseLanguage) {
            DEFAULT_LANGUAGE = chineseLanguage;
            ocrtest.Constants.engLanguage = false;
            Log.d(TAG, "onCreate: ======================="+Constants.engLanguage);
        }else {
            ocrtest.Constants.engLanguage = true;
            Log.d(TAG, "onCreate: ======================="+Constants.engLanguage);
        }

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
                    if (ratio > mRatio) {
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
                tvShowText.setText(mContext);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
