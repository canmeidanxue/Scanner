package com.bulesky.zxinglibrary.test.camera.open;

import android.hardware.Camera;

/**
 * Describe:打开相机实体
 * Created by hsl on 2017/10/11.
 */


public class OpenCamera {
    private int index;//索引值:1表示主摄像头，2表示辅摄像头
    private Camera mCamera;
    private CameraFacing facing;//前置还是后置
    private int orientation;//方向

    public OpenCamera(int index, Camera mCamera, CameraFacing facing, int orientation) {
        this.index = index;
        this.mCamera = mCamera;
        this.facing = facing;
        this.orientation = orientation;
    }

    public int getIndex() {
        return index;
    }


    public Camera getmCamera() {
        return mCamera;
    }

    public CameraFacing getFacing() {
        return facing;
    }

    public int getOrientation() {
        return orientation;
    }

    @Override
    public String toString() {
        return "OpenCamera{" +
                "index=" + index +
                ", mCamera=" + mCamera +
                ", facing=" + facing +
                ", orientation=" + orientation +
                '}';
    }
}
