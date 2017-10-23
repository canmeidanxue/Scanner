package com.administrator.zxinglibrary.album.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.administrator.zxinglibrary.R;
import com.administrator.zxinglibrary.album.entity.Picture;
import com.administrator.zxinglibrary.album.entity.PictureViewHolder;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Describe:列表形式呈现图片
 * Created by hsl on 2017/10/10.
 */


public class PictureTotalAdapter extends PictureBaseAdapter<Picture> {
    public PictureTotalAdapter(Context mContext, List<Picture> objects) {
        super(mContext, R.layout.activity_pick_picture_total_list_item, objects);
    }

    @Override
    public void onBindData(PictureViewHolder viewHolder, Picture item, int position) {
        viewHolder.setText(R.id.pick_picture_total_list_item_group_title, item.getFolderName());
        viewHolder.setText(R.id.pick_picture_total_list_item_group_count
                , "(" + Integer.toString(item.getPictureCount()) + ")");
        ImageView imageView = viewHolder.findViewById(R.id.pick_picture_total_list_item_group_image);
        Glide.with(mContext).load(item.getTopPicturePath()).into(imageView);
    }
}
