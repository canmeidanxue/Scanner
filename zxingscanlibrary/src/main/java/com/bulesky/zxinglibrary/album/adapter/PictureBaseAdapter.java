package com.bulesky.zxinglibrary.album.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bulesky.zxinglibrary.album.entity.PictureViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe:图片父类Adapter
 * Created by hsl on 2017/10/10.
 */
public abstract class PictureBaseAdapter<T> extends BaseAdapter {
    private final Object mLock = new Object();
//    private ObjectFilter mFilter;
    private ArrayList<T> mOriginalValues;//原始数据
    protected List<T> mObjects;// 数据源
    protected int mCurrentCheckPosition = -1;// 当前选择的行号
    protected Context mContext;
    protected int mResource;// 视图ID

    public abstract void onBindData(PictureViewHolder viewHolder, final T item, final int position);

    public PictureBaseAdapter(Context context, int resource, List<T> objects) {
        this.mContext = context;
        this.mResource = resource;
        this.mObjects = objects;
    }

    @Override
    public int getCount() {
        if (mObjects == null)
            return 0;
        return mObjects.size();
    }

    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PictureViewHolder viewHolder = PictureViewHolder.get(mContext, mResource, convertView, parent);
        onBindData(viewHolder, getItem(position), position);
        return viewHolder.getItemView();
    }

    /**
     * 添加单个数据对象
     *
     * @param object
     */
    public final void add(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
            }
        }
        notifyDataSetChanged();
    }
}
