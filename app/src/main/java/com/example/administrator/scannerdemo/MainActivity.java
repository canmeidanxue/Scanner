package com.example.administrator.scannerdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bulesky.zxinglibrary.activity.ScannerActivity;
import com.google.zxing.Result;

import activity.ScanerCodeActivity;
import interfaces.OnScanerListener;
import ocrtest.TestOrcActivity;
import ocrtest.baidu.BaiduRecogniseActivity;
import tools.MyPermissionsTool;

public class MainActivity extends Activity implements OnScanerListener{
    private TextView tv_ma_show_result;
    private TextView tvShowResult;
    private int REQUEST_CODE = 0x001;
    private int REQUEST_BAIDU_CODE = 0x002;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        OcrUtil.initOcr(MainActivity.this);
        MyPermissionsTool.
                with(MainActivity.this).
                addPermission(Manifest.permission.READ_EXTERNAL_STORAGE).
                addPermission(Manifest.permission.CAMERA).
                initPermission();
        tv_ma_show_result = findViewById(R.id.tv_ma_show_result);
        tvShowResult = findViewById(R.id.tvShowResult);
        findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanerCodeActivity.setScanerListener(MainActivity.this);
                startActivity(new Intent(MainActivity.this, ScanerCodeActivity.class));
            }
        });
        findViewById(R.id.btn_recognise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, TestOrcActivity.class),REQUEST_CODE);
            }
        });
        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });
        findViewById(R.id.btn_new_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                intent.putExtra(ScannerActivity.EXTRA_LASER_LINE_MODE,0);
                intent.putExtra(ScannerActivity.EXTRA_SCAN_MODE,0);
                intent.putExtra(ScannerActivity.EXTRA_SHOW_THUMBNAIL,true);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_baidu_recognise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, BaiduRecogniseActivity.class),REQUEST_BAIDU_CODE);
            }
        });
    }

    @Override
    public void onSuccess(String type, Result result) {
        tv_ma_show_result.setText(type + result);
    }

    @Override
    public void onFail(String type, String message) {
        tv_ma_show_result.setText(type + message);
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
