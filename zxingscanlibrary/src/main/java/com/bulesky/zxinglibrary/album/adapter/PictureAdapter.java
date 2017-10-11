package com.bulesky.zxinglibrary.album.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.bulesky.zxinglibrary.R;
import com.bulesky.zxinglibrary.album.entity.PictureViewHolder;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Describe:单张图片呈现
 * Created by hsl on 2017/10/10.
 */


public class PictureAdapter extends PictureBaseAdapter<String> {
    public PictureAdapter(Context mContext,List<String> objects) {
        super(mContext, R.layout.activity_pick_picture_grid_item, objects);
    }

    @Override
    public void onBindData(PictureViewHolder viewHolder, String item, int position) {
        ImageView imageView = viewHolder.findViewById(R.id.activity_pick_picture_grid_item_image);
        Glide.with(mContext).load(item).into(imageView);
    }
}
