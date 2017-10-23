package com.administrator.zxinglibrary.album;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.administrator.zxinglibrary.R;
import com.administrator.zxinglibrary.album.adapter.PictureAdapter;
import com.administrator.zxinglibrary.album.util.SortPictureListByTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Create by hsl 2017-10-10 图片列表
 */
public class PictureListActivity extends AppCompatActivity {
    private GridView mGridView;
    private List<String> mList;//此相册下所有图片的路径集合
    private PictureAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        mGridView = (GridView) findViewById(R.id.child_grid);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setResult(mList.get(position));
            }
        });
        processExtraData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    private void processExtraData() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) return;
        mList = extras.getStringArrayList("data");
        if (mList.size() > 1) {
            SortPictureListByTime sortList = new SortPictureListByTime();
            Collections.sort(mList, sortList);
        }
        mAdapter = new PictureAdapter(this, mList);
        mGridView.setAdapter(mAdapter);
    }

    private void setResult(String picturePath) {
        Intent intent = new Intent();
        intent.putExtra(PictureTotalActivity.EXTRA_PICTURE_PATH, picturePath);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public static void gotoActivity(Activity activity, ArrayList<String> childList) {
        Intent intent = new Intent(activity, PictureListActivity.class);
        intent.putStringArrayListExtra("data", childList);
        activity.startActivityForResult(intent, PictureTotalActivity.REQUEST_CODE_SELECT_ALBUM);
    }
}
