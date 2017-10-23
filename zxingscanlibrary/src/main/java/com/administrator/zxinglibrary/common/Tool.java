package com.administrator.zxinglibrary.common;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Update by hsl on 2017-10-7
 */
public final class Tool {
    //开始扫描
    public static final int RESTART_PREVIEW = 0;
    //扫描成功
    public static final int DECODE_SUCCEEDED = 1;
    //扫描失败
    public static final int DECODE_FAILED = 2;
    //开始解码
    public static final int DECODE = 3;
    //停止扫码
    public static final int QUIT = 4;

    public static class color {
        public static final int VIEWFINDER_MASK = 0x60000000;
        public static final int RESULT_VIEW = 0xb0000000;
        public static final int VIEWFINDER_LASER = 0xff00ff00;
        public static final int RESULT_POINTS = 0xc099cc00;
    }

    /**
     * 单种条码解析【已知扫码类型】
     */
    public static class ScanMode {

        /**
         * 商品条码：PC and EAN
         */
        public static final String PRODUCT_MODE = "PRODUCT_MODE";

        /**
         * 一维码
         */
        public static final String ONE_D_MODE = "ONE_D_MODE";

        /**
         * 二维码
         */
        public static final String QR_CODE_MODE = "QR_CODE";

        /**
         * 矩阵码
         */
        public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue
                , context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue
                , context.getResources().getDisplayMetrics());
    }

    public static int getSreenWidth(Context mContext) {
        Resources resources = mContext.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return displayMetrics.widthPixels;

    }
}
