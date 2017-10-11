package com.bulesky.zxinglibrary.test.camera;

import android.hardware.Camera;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;

/**
 * Describe:自动对焦管理类
 * Created by hsl on 2017/10/11.
 */

@SuppressWarnings("deprecation")
public class AutoFocusManager implements Camera.AutoFocusCallback {
    private long AUTO_FOUCUS_INTEERVAL_MS = 1000L;
    private static final Collection<String> fCollection;

    static {
        fCollection = new ArrayList<>();
        fCollection.add(Camera.Parameters.FOCUS_MODE_AUTO);//	自动对焦模式
        fCollection.add(Camera.Parameters.FOCUS_MODE_MACRO);//	宏观（特写）对焦模式
    }

    private boolean stopped;//停止对焦
    private boolean useAutoFocus;//需要自动对焦
    private boolean focusing;//正在对焦
    private Camera mCamera;
    private AsyncTask<?, ?, ?> outstandingTask;

    public AutoFocusManager(Camera mCamera) {
        this.mCamera = mCamera;
        String currentFoucusMode = mCamera.getParameters().getFocusMode();
        useAutoFocus = fCollection.contains(currentFoucusMode);
        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean b, Camera camera) {
        focusing = false;
        autoFocusAgainLater();
    }

    private final class AutoFocusTask extends AsyncTask<Objects, Objects, Objects> {

        @Override
        protected Objects doInBackground(Objects... objectses) {
            try {
                Thread.sleep(AUTO_FOUCUS_INTEERVAL_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            start();
            return null;
        }
    }

    synchronized void start() {
        if (useAutoFocus) {
            outstandingTask = null;
            if (!stopped && !focusing) {
                try {
                    mCamera.autoFocus(this);
                    focusing = true;
                } catch (RuntimeException e) {
                    autoFocusAgainLater();
                }
            }
        }
    }

    /**
     * 再次自动对焦
     */
    private synchronized void autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            try {
                AutoFocusTask newAutoFocusTask = new AutoFocusTask();
                newAutoFocusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                outstandingTask = newAutoFocusTask;
            } catch (RejectedExecutionException e) {
                e.getStackTrace();
            }
        }
    }

    /**
     * 停止对焦
     */
    synchronized void stop(){
        stopped = true;
        if (useAutoFocus) {
            cancelOutStandingTask();
            try {
                mCamera.cancelAutoFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 取消异步线程
     */
    private synchronized void cancelOutStandingTask() {
        if (null != outstandingTask) {
            if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
                outstandingTask.cancel(true);
            }
            outstandingTask = null;
        }
    }
}
