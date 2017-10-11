package interfaces;

import com.google.zxing.Result;

/**
 * Created by hsl 2017-9-25
 */

public interface OnScanerListener {
    void onSuccess(String type, Result result);

    void onFail(String type, String message);
}
