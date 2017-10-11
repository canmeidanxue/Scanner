package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.scanlibrary.R;

import tools.ScreenTool;

/**
 * Describe: 扫描框
 * Created by hsl on 2017/9/27.
 */


public class ScanBoxView extends RelativeLayout {
    private Paint mPaint;
    private Rect mRect;
    private int mMaskColor;
    private int mFrameColor;
    private int mHornColor;
    private int mTextColor;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;
    private int mScannerAlpha;
    private int mScreenWidth;
    private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };

    public ScanBoxView(Context context) {
        this(context, null);
    }

    public ScanBoxView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context);
    }

    private void initData(Context mContext) {
        mMaskColor = getResources().getColor(R.color.qr_code_finder_mask);
        mFrameColor = getResources().getColor(R.color.qr_code_line);
        mHornColor = getResources().getColor(R.color.qr_code_horn);
        mTextColor = getResources().getColor(R.color.qr_code_text);
        mFocusThick = 1;
        mAngleThick = 3;
        mAngleLength = 30;
        mScannerAlpha = 0;
        mPaint = new Paint();
        if (isInEditMode()) {
            return;
        }
        setWillNotDraw(false);
        LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        RelativeLayout mRelativeLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.layout_scanner, this);
        FrameLayout mFrameLayout = mRelativeLayout.findViewById(R.id.fl_scanner);
        RelativeLayout.LayoutParams mLayoutParams = (LayoutParams) mFrameLayout.getLayoutParams();
        mRect = new Rect();
        mScreenWidth = ScreenTool.getScreenWidth(mContext);
        mRect.left = (mScreenWidth - mLayoutParams.width) / 2;
        mRect.right = mRect.left + mLayoutParams.width;
        mRect.top = mLayoutParams.topMargin;
        mRect.bottom = mRect.top + mLayoutParams.height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        if (null == mRect) {
            return;
        }
        if (null == mPaint) {
            return;
        }
        mPaint.setColor(mMaskColor);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.drawRect(0, 0, width, mRect.top, mPaint);
        canvas.drawRect(0, mRect.top, mRect.left, mRect.bottom + 1, mPaint);
        canvas.drawRect(mRect.left + 1, mRect.top, width, mRect.bottom + 1, mPaint);
        canvas.drawRect(0, mRect.bottom + 1, width, height, mPaint);
        //================================================================边框
        mPaint.setColor(mFrameColor);
//        canvas.drawRect(mRect.left + mAngleLength, mRect.top, mRect.left - mAngleLength, mRect.top + mFocusThick, mPaint);
        canvas.drawRect(mRect.left + mAngleLength, mRect.top, mRect.right - mAngleLength, mRect.top + mFocusThick, mPaint);
        canvas.drawRect(mRect.left, mRect.top + mAngleLength, mRect.left + mFocusThick, mRect.bottom - mAngleLength, mPaint);
        canvas.drawRect(mRect.right - mFocusThick, mRect.top + mAngleLength, mRect.right, mRect.bottom - mAngleLength, mPaint);
        canvas.drawRect(mRect.left + mAngleLength, mRect.bottom - mFocusThick, mRect.right - mAngleLength, mRect.bottom, mPaint);
        //================================================================四角
        mPaint.setColor(mHornColor);
        mPaint.setAlpha(0xFF);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mAngleThick);
        //左上角
        canvas.drawRect(mRect.left, mRect.top, mRect.left + mAngleLength, mRect.top + mAngleThick, mPaint);
        canvas.drawRect(mRect.left, mRect.top, mRect.left + mAngleThick, mRect.top + mAngleLength, mPaint);
        //右上角
        canvas.drawRect(mRect.right - mAngleLength, mRect.top, mRect.right, mRect.top + mAngleThick, mPaint);
        canvas.drawRect(mRect.right - mAngleThick, mRect.top, mRect.right, mRect.top + mAngleLength, mPaint);
        //左下角
        canvas.drawRect(mRect.left, mRect.bottom - mAngleLength, mRect.left + mAngleThick, mRect.bottom, mPaint);
        canvas.drawRect(mRect.left, mRect.bottom - mAngleThick, mRect.left + mAngleLength, mRect.bottom, mPaint);
        //右下角
        canvas.drawRect(mRect.right - mAngleLength, mRect.bottom - mAngleThick, mRect.right, mRect.bottom, mPaint);
        canvas.drawRect(mRect.right - mAngleThick, mRect.bottom - mAngleLength, mRect.right, mRect.bottom, mPaint);
        //==================================================================绘制文字
        int margin = 60;//间距
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(22);
        String showText = "将二维码/条形码放入框内，即可自动扫描";//内容
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float totalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = totalHeight / 2 - fontMetrics.bottom;
        float newY = mRect.bottom + margin + offY;
        float newX = (mScreenWidth - mPaint.getTextSize() * showText.length()) / 2 + mAngleThick * 2;
        canvas.drawText(showText, newX, newY, mPaint);
        //====================================================================绘制中间线条
        mPaint.setColor(Color.RED);
//        mPaint.setAlpha(SCANNER_ALPHA[mScannerAlpha]);
        mScannerAlpha = (mScannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = mRect.height() / 2 + mRect.top;
        canvas.drawRect(mRect.left + 2, middle - 1, mRect.right - 1, middle + 2, mPaint);

    }
}
