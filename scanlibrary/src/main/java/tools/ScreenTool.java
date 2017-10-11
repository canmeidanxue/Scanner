package tools;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Describe:
 * Created by hsl on 2017/9/27.
 */


public class ScreenTool {
    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context mContext) {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeight(Context mContext) {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
