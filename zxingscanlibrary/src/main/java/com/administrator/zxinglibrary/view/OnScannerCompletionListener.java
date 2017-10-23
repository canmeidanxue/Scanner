package com.administrator.zxinglibrary.view;

import com.google.zxing.Result;

/**
 * 结果回调
 * Created by hsl on 2017-10-11
 *
 */
public interface OnScannerCompletionListener {
    /**
     * 扫描成功后将调用
     * @param rawResult    扫描结果
     */
    void OnScannerCompletion(Result rawResult);
    void OnScannerCompletion(String rawResult);
}
