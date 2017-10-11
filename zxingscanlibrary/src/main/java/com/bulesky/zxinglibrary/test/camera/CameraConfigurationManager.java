package com.bulesky.zxinglibrary.test.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.bulesky.zxinglibrary.test.camera.open.CameraFacing;
import com.bulesky.zxinglibrary.test.camera.open.OpenCamera;

/**
 * Describe:设置相机基本参数管理类
 * Created by hsl on 2017/10/11.
 */


public class CameraConfigurationManager {
    private Context context;

    private int cwRotationFromDisplayToCamera;
    // 屏幕分辨率
    private Point screenResolution;
    // 相机分辨率
    private Point cameraResolution;
    private Point bestPreviewSize;
    public CameraConfigurationManager(Context context) {
        this.context = context;
    }

    void initFromCameraParameters(OpenCamera camera) {
        Camera.Parameters parameters = camera.getmCamera().getParameters();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int displayRotation = display.getRotation();
        int cwRotionFromNaturalToDisplay;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                cwRotionFromNaturalToDisplay = 0;
                break;
            case Surface.ROTATION_90:
                cwRotionFromNaturalToDisplay = 90;
                break;
            case Surface.ROTATION_180:
                cwRotionFromNaturalToDisplay = 180;
                break;
            case Surface.ROTATION_270:
                cwRotionFromNaturalToDisplay = 270;
                break;
            default:
                if (displayRotation % 90 == 0) {
                    cwRotionFromNaturalToDisplay = (360 + displayRotation) % 360;
                } else {
                    throw new IllegalArgumentException("wrong rotation:" + displayRotation);
                }
                break;
        }
        int cwRotationFromNaturalToCamera = camera.getOrientation();

        if (camera.getFacing() == CameraFacing.FRONT) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
        }
        cwRotationFromDisplayToCamera = (360 + cwRotationFromNaturalToCamera - cwRotionFromNaturalToDisplay) % 360;
        Point currentScreenResolution = new Point();
        display.getSize(currentScreenResolution);
        screenResolution = currentScreenResolution;
        //如果拉伸
        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = screenResolution.x;
        screenResolutionForCamera.y = screenResolution.y;
        if (screenResolution.x  < screenResolution.y) {
            screenResolutionForCamera.x = screenResolution.x;
            screenResolutionForCamera.y = screenResolution.x;
        }

    }

}
