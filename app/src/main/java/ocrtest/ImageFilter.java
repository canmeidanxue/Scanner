package ocrtest;


import android.graphics.Bitmap;

/**
 * 图片处理工具
 */
public class ImageFilter {
    /**
     * 线性灰度处理
     */
    public static Bitmap grayScale(Bitmap bitmap) {
        //得到图像的宽度和长度
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //创建线性拉升灰度图像
        Bitmap linegray = null;
        linegray = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        //依次循环对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到每点的像素值
                int col = bitmap.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 增加了图像的亮度
                red = (int) (1.1 * red + 30);
                green = (int) (1.1 * green + 30);
                blue = (int) (1.1 * blue + 30);
                //对图像像素越界进行处理
                if (red >= 255)
                {
                    red = 255;
                }

                if (green >= 255) {
                    green = 255;
                }

                if (blue >= 255) {
                    blue = 255;
                }
                // 新的ARGB
                int newColor = alpha | (red << 16) | (green << 8) | blue;
                //设置新图像的RGB值
                linegray.setPixel(i, j, newColor);
            }
        }
        return linegray;
    }

    /**
     * 二值化处理
     */
    public static Bitmap binaryzation(Bitmap bitmap) {

        //得到图形的宽度和长度  
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //创建二值化图像
        Bitmap binarymap = null;
        binarymap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        //依次循环，对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int col = binarymap.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB  
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 +
                        (float) blue * 0.11);
                //对图像进行二值化处理  
                if (gray <= 95) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 新的ARGB  
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                binarymap.setPixel(i, j, newColor);
            }
        }
        return binarymap;
    }


}
