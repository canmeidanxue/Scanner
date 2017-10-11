package com.bulesky.zxinglibrary.album.entity;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Describe:
 * Created by hsl on 2017/10/10.
 */


public class PictureViewHolder {
    private Context mContext;
    private SparseArray<View> mViews;
    private View mItemView;

    public PictureViewHolder(Context mContext,View mItemView) {
        this.mContext = mContext;
        mViews = new SparseArray<>();
        this.mItemView = mItemView;
        mItemView.setTag(this);
    }

    public static PictureViewHolder get(Context context, int resource, View convertView,
                                    ViewGroup parent) {
        if (convertView == null) {
            View view = LayoutInflater.from(context).inflate(resource, parent, false);
            return new PictureViewHolder(context, view);
        }
        return (PictureViewHolder) convertView.getTag();
    }

    public <T extends View> T findViewById(int id) {
        View view = mViews.get(id);
        if (view == null) {
            view = mItemView.findViewById(id);
            mViews.put(id, view);
        }
        return (T) view;
    }
    /**
     * 设置 TextView 的显示的字符串
     *
     * @param viewId
     * @param text
     * @return
     */
    public PictureViewHolder setText(int viewId, String text) {
        View view = findViewById(viewId);
        if (view instanceof TextView) {
            ((TextView) view).setText(text);
        }
        return this;
    }

    public View getItemView() {
        return mItemView;
    }





}
