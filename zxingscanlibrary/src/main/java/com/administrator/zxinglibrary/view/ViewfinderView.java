/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.administrator.zxinglibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.administrator.zxinglibrary.R;
import com.administrator.zxinglibrary.camera.CameraManager;
import com.administrator.zxinglibrary.common.Tool;

/**
 * 扫描界面view绘制
 */
final class ViewfinderView extends View {

    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int POINT_SIZE = 6;
    private static final int DEFAULT_LASER_LINE_HEIGHT = 2;//扫描线默认高度
    private static final int DEFAULT_LASER_MOVE_SPEED = 6;//默认每毫秒移动6px

    private final Paint paint;
    private Bitmap resultBitmap;

    private int animationDelay = 0;
    private Bitmap laserLineBitmap;

    private int maskColor = Tool.color.VIEWFINDER_MASK;//扫描框以外区域半透明黑色
    private int resultColor = Tool.color.RESULT_VIEW;//扫描成功后扫描框以外区域白色
    private int laserColor = Tool.color.VIEWFINDER_LASER;//扫描线颜色
    private int laserFrameBoundColor = laserColor;//扫描框4角颜色
    private int laserLineTop;// 扫描线最顶端位置
    private int laserLineHeight;//扫描线默认高度
    private int laserMoveSpeed;// 扫描线默认移动距离px
    private int laserFrameCornerWidth;//扫描框4角宽
    private int laserFrameCornerLength;//扫描框4角高
    private int laserLineResId;//扫描线图片资源
    private String drawText = "将二维码放入框内，即可自动扫描";//提示文字
    private int drawTextSize;//提示文字大小
    private int drawTextColor = Color.WHITE;//提示文字颜色
    private boolean drawTextGravityBottom = true;//提示文字位置
    private int drawTextMargin;//提示文字与扫描框距离
    private boolean isLaserGridLine;//是否为网格资源文件
    private int flashImageWidth;
    private Bitmap flashOpen;
    private boolean isVisibleFalsh;
    private Context mContext;
    private boolean isOpen;
    private double bottom;
    private CameraManager cameraManager;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        laserMoveSpeed = DEFAULT_LASER_MOVE_SPEED;//默认每毫秒移动6px
        laserLineHeight = Tool.dp2px(context, DEFAULT_LASER_LINE_HEIGHT);
        laserFrameCornerWidth = Tool.dp2px(context, 2f);
        laserFrameCornerLength = Tool.dp2px(context, 15f);
        drawTextSize = Tool.sp2px(context, 15f);
        drawTextMargin = Tool.dp2px(context, 20f);
        flashOpen = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_flash_defaut);
        flashImageWidth = flashOpen.getWidth();
    }

    void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return;
        }
        Rect frame = cameraManager.getFramingRect();//取扫描框
        //取屏幕预览
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        // 绘制扫描框以外4个区域
        drawMask(canvas, frame);
        // 如果有二维码结果的Bitmap，在扫取景框内绘制不透明的result Bitmap
        if (resultBitmap != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            drawFrame(canvas, frame);//绘制扫描框
            drawFrameCorner(canvas, frame);//绘制扫描框4角
            drawText(canvas, frame);// 画扫描框下面的字
            drawLaserLine(canvas, frame);//绘制扫描线
            moveLaserSpeed(frame);//计算移动位置
            drawLight(canvas, frame);//小灯
        }
    }

    private void drawLight(Canvas canvas, Rect frame) {
        if (flashOpen != null) {
            if (isVisibleFalsh) {
                paint.setColor(Color.WHITE);
                canvas.drawBitmap(flashOpen, (Tool.getSreenWidth(mContext) - flashImageWidth) / 2, frame.bottom - Tool.dp2px(mContext, 60), paint);
                paint.setTextSize(drawTextSize);
                canvas.drawText("轻触点亮", (Tool.getSreenWidth(mContext) - flashImageWidth) / 2 - Tool.dp2px(mContext,12), frame.bottom - Tool.dp2px(mContext, 10), paint);
            } else {
                if (!isOpen) {
                    paint.setColor(Color.TRANSPARENT);
                    canvas.drawBitmap(flashOpen, (Tool.getSreenWidth(mContext) - flashImageWidth) / 2, frame.bottom - Tool.dp2px(mContext, 60), paint);
                    canvas.drawText("轻触点亮", (Tool.getSreenWidth(mContext) - flashImageWidth) / 2 + Tool.dp2px(mContext, 10), frame.bottom - Tool.dp2px(mContext, 10), paint);
                } else {

                }
            }
        }
        bottom = frame.bottom - Tool.dp2px(mContext, 60);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (isVisibleFalsh) {
            if (
                    x > (Tool.getSreenWidth(mContext) - flashImageWidth) / 2 - Tool.dp2px(mContext, 20)
                            && x < (Tool.getSreenWidth(mContext) - flashImageWidth) / 2 + Tool.dp2px(mContext, 20)) {
                if (y > bottom - Tool.dp2px(mContext, 20) && y < bottom + Tool.dp2px(mContext, 50)) {
                    if (!isOpen) {//开灯
                        flashOpen = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_flash_open);
                        cameraManager.setTorch(true);
                        isOpen = true;
                        flashImageWidth = flashOpen.getWidth();
                    } else {  // 关灯
                        flashOpen = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_flash_defaut);
                        cameraManager.setTorch(false);
                        isOpen = false;
                        flashImageWidth = flashOpen.getWidth();
                    }
                    invalidate();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void moveLaserSpeed(Rect frame) {
        //初始化扫描线最顶端位置
        if (laserLineTop == 0) {
            laserLineTop = frame.top;
        }
        // 每次刷新界面，扫描线往下移动 LASER_VELOCITY
        laserLineTop += laserMoveSpeed;
        if (laserLineTop >= frame.bottom) {
            laserLineTop = frame.top;
        }
        if (animationDelay == 0) {
            animationDelay = (int) ((1.0f * 1000 * laserMoveSpeed) / (frame.bottom - frame.top));
        }

        // 只刷新扫描框的内容，其他地方不刷新
        postInvalidateDelayed(animationDelay, frame.left - POINT_SIZE, frame.top - POINT_SIZE
                , frame.right + POINT_SIZE, frame.bottom + POINT_SIZE);
    }

    /**
     * 画扫描框外区域
     *
     * @param canvas
     * @param frame
     */
    private void drawMask(Canvas canvas, Rect frame) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
    }

    /**
     * 绘制提示文字
     *
     * @param canvas
     * @param frame
     */
    private void drawText(Canvas canvas, Rect frame) {
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(drawTextColor);
        textPaint.setTextSize(drawTextSize);

        float x = frame.left;//文字开始位置
        //根据 drawTextGravityBottom 文字在扫描框上方还是下文，默认下方
        float y = drawTextGravityBottom ? frame.bottom + drawTextMargin
                : frame.top - drawTextMargin;

        StaticLayout staticLayout = new StaticLayout(drawText, textPaint, frame.width()
                , Layout.Alignment.ALIGN_CENTER, 1.0f, 0, false);
        canvas.save();
        canvas.translate(x, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * 绘制扫描框4角
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameCorner(Canvas canvas, Rect frame) {
        paint.setColor(laserFrameBoundColor);
        paint.setStyle(Paint.Style.FILL);
        // 左上角
        canvas.drawRect(frame.left - laserFrameCornerWidth, frame.top, frame.left, frame.top
                + laserFrameCornerLength, paint);
        canvas.drawRect(frame.left - laserFrameCornerWidth, frame.top - laserFrameCornerWidth
                , frame.left + laserFrameCornerLength, frame.top, paint);
        // 右上角
        canvas.drawRect(frame.right, frame.top, frame.right + laserFrameCornerWidth,
                frame.top + laserFrameCornerLength, paint);
        canvas.drawRect(frame.right - laserFrameCornerLength, frame.top - laserFrameCornerWidth,
                frame.right + laserFrameCornerWidth, frame.top, paint);
        // 左下角
        canvas.drawRect(frame.left - laserFrameCornerWidth, frame.bottom - laserFrameCornerLength,
                frame.left, frame.bottom, paint);
        canvas.drawRect(frame.left - laserFrameCornerWidth, frame.bottom, frame.left
                + laserFrameCornerLength, frame.bottom + laserFrameCornerWidth, paint);
        // 右下角
        canvas.drawRect(frame.right, frame.bottom - laserFrameCornerLength, frame.right
                + laserFrameCornerWidth, frame.bottom, paint);
        canvas.drawRect(frame.right - laserFrameCornerLength, frame.bottom, frame.right
                + laserFrameCornerWidth, frame.bottom + laserFrameCornerWidth, paint);
    }

    /**
     * 画扫描框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrame(Canvas canvas, Rect frame) {
        paint.setColor(Color.WHITE);//扫描边框白色
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(frame, paint);
    }

    /**
     * 画扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawLaserLine(Canvas canvas, Rect frame) {
        if (laserLineResId == 0) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(laserColor);// 设置扫描线颜色
            canvas.drawRect(frame.left, laserLineTop, frame.right
                    , laserLineTop + laserLineHeight, paint);
        } else {
            if (laserLineBitmap == null)//图片资源文件转为 Bitmap
                laserLineBitmap = BitmapFactory.decodeResource(getResources(), laserLineResId);
            int height = laserLineBitmap.getHeight();//取原图高
            //网格图片
            if (isLaserGridLine) {
                RectF dstRectF = new RectF(frame.left, frame.top, frame.right, laserLineTop);
                Rect srcRect = new Rect(0, (int) (height - dstRectF.height())
                        , laserLineBitmap.getWidth(), height);
                canvas.drawBitmap(laserLineBitmap, srcRect, dstRectF, paint);
            }
            //线条图片
            else {
                //如果没有设置线条高度，则用图片原始高度
                if (laserLineHeight == Tool.dp2px(getContext(), DEFAULT_LASER_LINE_HEIGHT)) {
                    laserLineHeight = laserLineBitmap.getHeight() / 2;
                }
                Rect laserRect = new Rect(frame.left, laserLineTop, frame.right
                        , laserLineTop + laserLineHeight);
                canvas.drawBitmap(laserLineBitmap, null, laserRect, paint);
            }
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }


    public void setLaserColor(int laserColor) {
        this.laserColor = laserColor;
    }

    public void setLaserLineResId(int laserLineResId) {
        this.laserLineResId = laserLineResId;
        this.isLaserGridLine = false;
    }

    public void setLaserGridLineResId(int laserLineResId) {
        this.laserLineResId = laserLineResId;
        this.isLaserGridLine = true;
    }

    public void setFlashBitmap(boolean isVisible) {
        isVisibleFalsh = isVisible;
        invalidate();
    }

    public void setLaserLineHeight(int laserLineHeight) {
        this.laserLineHeight = Tool.dp2px(getContext(), laserLineHeight);
    }

    public void setLaserFrameBoundColor(int laserFrameBoundColor) {
        this.laserFrameBoundColor = laserFrameBoundColor;
    }

    public void setLaserFrameCornerLength(int laserFrameCornerLength) {
        this.laserFrameCornerLength = Tool.dp2px(getContext(), laserFrameCornerLength);
    }

    public void setLaserFrameCornerWidth(int laserFrameCornerWidth) {
        this.laserFrameCornerWidth = Tool.dp2px(getContext(), laserFrameCornerWidth);
    }

    public void setDrawText(String text, int textSize, int textColor
            , boolean isBottom, int textMargin) {
        if (!TextUtils.isEmpty(text))
            drawText = text;
        if (textSize > 0)
            drawTextSize = Tool.sp2px(getContext(), textSize);
        if (textColor != 0)
            drawTextColor = textColor;
        drawTextGravityBottom = isBottom;
        if (textMargin > 0)
            drawTextMargin = Tool.dp2px(getContext(), textMargin);
    }

    public void setDrawTextColor(int textColor) {
        if (textColor != 0)
            drawTextColor = textColor;
    }

    public void setDrawTextSize(int textSize) {
        if (textSize > 0)
            drawTextSize = Tool.sp2px(getContext(), textSize);
    }

    public void setLaserMoveSpeed(int moveSpeed) {
        if (moveSpeed <= 0) {
            laserMoveSpeed = DEFAULT_LASER_MOVE_SPEED;
        } else {
            laserMoveSpeed = moveSpeed;
        }
    }

    public void laserLineBitmapRecycle() {
        if (laserLineBitmap != null) {
            laserLineBitmap.recycle();
            laserLineBitmap = null;
        }
    }
}
