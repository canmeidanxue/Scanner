package com.example.administrator.scannerdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.scannerdemo.view.CameraView;


public class CameraActivity extends AppCompatActivity {

    View xianView;
    int height, distanceHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        init();
    }

    private void init() {
        //扫描动画
        xianView = findViewById(R.id.xian);
        height = Dp2Px(this, 56);
        distanceHeight = Dp2Px(this, 3);
        setAnimation();

        CameraView cameraView = (CameraView) findViewById(R.id.main_camera);
        TextView tv_content = (TextView) findViewById(R.id.tv_content);
        ImageView img = (ImageView) findViewById(R.id.main_image);
        cameraView.setTag(R.id.tag_text, tv_content);
        cameraView.setTag(R.id.tag_img, img);
    }

    /**
     * 动画设置
     */
    void setAnimation() {
        Animation animation = new TranslateAnimation(0, 0, 0, height - distanceHeight);
        animation.setDuration(5000);
        animation.setRepeatMode(Animation.REVERSE);// 设置反方向执行
        animation.setRepeatCount(Animation.INFINITE);
        xianView.setAnimation(animation);
        animation.startNow();
    }

    public int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
