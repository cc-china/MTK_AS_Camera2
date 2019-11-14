
package com.mediatek.camera.feature.mode.beautyface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

@SuppressLint("AppCompatCustomView")
public class VerticalSeekBar extends SeekBar {
    private VerticalSeekBarListener mListener;

    public void setListener(VerticalSeekBarListener l) {
        mListener = l;
    }

    public interface VerticalSeekBarListener {
        public void onVerticalSeekBarStopTrackingTouch(VerticalSeekBar seekBar);
    }

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int i = 0;
                i = getMax() - (int) (getMax() * event.getY() / getHeight());
                setProgress(i);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                int progress = 0;
                progress = getMax() - (int) (getMax() * event.getY() / getHeight());
                setProgress(progress);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                if (mListener != null) {
                    mListener.onVerticalSeekBarStopTrackingTouch(VerticalSeekBar.this);
                }
                break;
        }
        return true;
    }

}
