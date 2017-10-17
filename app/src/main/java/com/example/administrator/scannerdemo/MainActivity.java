package com.example.administrator.scannerdemo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bulesky.zxinglibrary.activity.ScannerActivity;
import com.google.zxing.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import activity.ScanerCodeActivity;
import interfaces.OnScanerListener;
import ocrtest.TestOrcActivity;
import ocrtest.baidu.BaiduRecogniseActivity;
import tools.MyPermissionsTool;

public class MainActivity extends Activity implements OnScanerListener {
    private TextView tv_ma_show_result;
    private TextView tvShowResult;
    private int REQUEST_CODE = 0x001;
    private int REQUEST_BAIDU_CODE = 0x002;
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

    private static String CHI_LANGUAGE = "chi_sim";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";

    private static final String CHI_LANGUAGE_NAME = CHI_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String ENG_LANGUAGE_PATH = TESSDATA + File.separator + DEFAULT_LANGUAGE_NAME;
    private static final String CHI_LANGUAGE_PATH = TESSDATA + File.separator + "chi_sim" + ".traineddata";
    private ProgressDialog mProgressDialg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyPermissionsTool.
                with(MainActivity.this).
                addPermission(Manifest.permission.READ_EXTERNAL_STORAGE).
                addPermission(Manifest.permission.CAMERA).
                initPermission();
        mProgressDialg = ProgressDialog.show(this,null,"初始化数据中...");
        makeFile();
        tv_ma_show_result = findViewById(R.id.tv_ma_show_result);
        tvShowResult = findViewById(R.id.tvShowResult);
        findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanerCodeActivity.setScanerListener(MainActivity.this);
                startActivity(new Intent(MainActivity.this, ScanerCodeActivity.class));
            }
        });
        findViewById(R.id.btn_recognise_chi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TestOrcActivity.class);
                intent.putExtra(TestOrcActivity.languageKey, false);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        findViewById(R.id.btn_recognise_eng).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TestOrcActivity.class);
                intent.putExtra(TestOrcActivity.languageKey, true);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        findViewById(R.id.btn_new_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                intent.putExtra(ScannerActivity.EXTRA_LASER_LINE_MODE, 0);
                intent.putExtra(ScannerActivity.EXTRA_SCAN_MODE, 0);
                intent.putExtra(ScannerActivity.EXTRA_SHOW_THUMBNAIL, true);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_baidu_recognise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, BaiduRecogniseActivity.class), REQUEST_BAIDU_CODE);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    mProgressDialg.dismiss();
            }
        },5000);
    }

    private void makeFile() {
        new Thread(new Runnable() {
                @Override
                public void run() {
                    copyToSD(ENG_LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
                    copyToSD(CHI_LANGUAGE_PATH, CHI_LANGUAGE_NAME);
                }
            }).start();
    }

    @Override
    public void onSuccess(String type, Result result) {
        tv_ma_show_result.setText(type + result);
    }

    @Override
    public void onFail(String type, String message) {
        tv_ma_show_result.setText(type + message);
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
                    makeFile();
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
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                String result = data.getStringExtra(TestOrcActivity.RESULT_KEY);
                if (!TextUtils.isEmpty(result)) {
                    tvShowResult.setText(result);
                }

            }
        }
    }
}
