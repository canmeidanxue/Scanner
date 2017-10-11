package ocrtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.scannerdemo.R;


public class TestOrcActivity extends AppCompatActivity {
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
        mCameraView.setTag(mImageView);
            mCameraView.setOcrResult(new OcrResult() {
                @Override
                public void onResult(String result) {
                    if (!TextUtils.isEmpty(result)) {
                        Intent intent = new Intent();
                        intent.putExtra(RESULT_KEY,result);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }
            });

    }


}
