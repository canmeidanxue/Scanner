package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import interfaces.OnScanResultListener;

/**
 * Describe:
 * Created by hsl on 2017/9/28.
 */


public class OcrUtil {

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

    private static final String TESS_DATA = DATA_PATH + File.separator + "tessdata";
    /**
     * 默认语言
     */
    private static final String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * assest文件夹下资源文件
     *
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * SD卡下数据路径
     */
    private static final String LANGUAGE_PATH = TESS_DATA + File.separator + DEFAULT_LANGUAGE_NAME;


    /**
     * 初始化OCR
     */
    public static void initOcr(Context mContext) {
        copyData(mContext, LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
    }

    /**
     * 将数据copy到SD卡上
     *
     * @param mContext
     * @param languagePath
     * @param defaultLanguageName
     */

    private static void copyData(Context mContext, String languagePath, String defaultLanguageName) {
        File file = new File(languagePath);
        if (file.exists()) {
            file.delete();
        }
        if (!file.exists()) {
            File pareFile = new File(file.getParent());
            if (!pareFile.exists()) {
                pareFile.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = mContext.getAssets().open(defaultLanguageName);
            File nFile = new File(languagePath);
            os = new FileOutputStream(nFile);
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 扫描中文获取结果
     *
     * @param bm
     * @param scanResult
     */
    public static void scanChinese(final Bitmap bm, final OnScanResultListener scanResult) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TessBaseAPI tessBaseApi = new TessBaseAPI();
                if (tessBaseApi.init(DATA_PATH, DEFAULT_LANGUAGE)) {
                    tessBaseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
                    tessBaseApi.setImage(bm);
                    String result = tessBaseApi.getUTF8Text();
                    tessBaseApi.clear();
                    tessBaseApi.end();
                    scanResult.response(result);
                }
            }
        }).start();

    }
}
