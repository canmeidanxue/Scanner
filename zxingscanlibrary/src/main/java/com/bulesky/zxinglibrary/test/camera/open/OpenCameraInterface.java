package com.bulesky.zxinglibrary.test.camera.open;

import android.hardware.Camera;

/**
 * Describe:打开相机
 * Created by hsl on 2017/10/11.
 */


@SuppressWarnings("deprecation")
public class OpenCameraInterface {

    public OpenCameraInterface() {
    }

    public static OpenCamera open(int cameraId) {
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            return null;
        }

        boolean explicitRequest = cameraId >= 0;
        Camera.CameraInfo selectedCameraInfo = null;
        int index;
        if (explicitRequest) {
            index = cameraId;
            selectedCameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, selectedCameraInfo);
        } else {
            index = 0;
            while (index < numCameras) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(index, cameraInfo);
                CameraFacing reportedFacing = CameraFacing.values()[cameraInfo.facing];
                if (reportedFacing == CameraFacing.BACK) {
                    selectedCameraInfo = cameraInfo;
                    break;
                }
                index++;
            }
        }
        Camera camera;
        if (index < numCameras) {
            camera = Camera.open(index);
        } else {
            if (explicitRequest) {
                camera = null;
            } else {
                camera = Camera.open(0);
                selectedCameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(0, selectedCameraInfo);
            }
        }
        if (camera == null) {
            return null;
        }
        return new OpenCamera(index, camera, CameraFacing.values()[selectedCameraInfo.facing], selectedCameraInfo.orientation);
    }
}
