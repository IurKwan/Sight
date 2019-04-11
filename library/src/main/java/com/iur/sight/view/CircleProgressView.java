package com.iur.sight.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CircleProgressView extends View {
    private Paint paintBgCircle;
    private Paint paintCircle;
    private Paint paintProgressCircle;
    private float startAngle = -90.0F;
    private float sweepAngle = 0.0F;
    private int progressCirclePadding = 3;
    private boolean fillIn = false;
    private int animDuration = 2000;
    private CircleProgressView.CircleProgressViewAnim mCircleProgressViewAnim;

    public CircleProgressView(Context context) {
        super(context);
        this.init();
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    public int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    private void init() {
        this.mCircleProgressViewAnim = new CircleProgressView.CircleProgressViewAnim();
        this.mCircleProgressViewAnim.setDuration((long)this.animDuration);
        this.paintBgCircle = new Paint();
        this.paintBgCircle.setAntiAlias(true);
        this.paintBgCircle.setStyle(Paint.Style.STROKE);
        this.paintBgCircle.setColor(-855638017);
        this.paintCircle = new Paint();
        this.paintCircle.setAntiAlias(true);
        this.paintCircle.setStyle(Paint.Style.FILL);
        this.paintCircle.setColor(-7829368);
        this.paintProgressCircle = new Paint();
        this.paintProgressCircle.setAntiAlias(true);
        this.paintProgressCircle.setStyle(Paint.Style.FILL);
        this.paintProgressCircle.setColor(-855638017);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle((float)(this.getMeasuredWidth() / 2), (float)(this.getMeasuredWidth() / 2), (float)(this.getMeasuredWidth() / 2), this.paintBgCircle);
        RectF f = new RectF((float)this.progressCirclePadding, (float)this.progressCirclePadding, (float)(this.getMeasuredWidth() - this.progressCirclePadding), (float)(this.getMeasuredWidth() - this.progressCirclePadding));
        canvas.drawArc(f, this.startAngle, this.sweepAngle, true, this.paintProgressCircle);
        if (!this.fillIn) {
            canvas.drawCircle((float)(this.getMeasuredWidth() / 2), (float)(this.getMeasuredWidth() / 2), (float)(this.getMeasuredWidth() / 2 - this.progressCirclePadding * 2), this.paintCircle);
        }

    }

    public void startAnimAutomatic(boolean fillIn) {
        this.fillIn = fillIn;
        if (this.mCircleProgressViewAnim != null) {
            this.clearAnimation();
        }

        this.startAnimation(this.mCircleProgressViewAnim);
    }

    public void stopAnimAutomatic() {
        if (this.mCircleProgressViewAnim != null) {
            this.clearAnimation();
        }

    }

    public void setProgress(int progress, boolean fillIn) {
        this.fillIn = fillIn;
        this.sweepAngle = (float)(3.6D * (double)progress);
        this.invalidate();
    }

    private class CircleProgressViewAnim extends Animation {
        private CircleProgressViewAnim() {
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (interpolatedTime < 1.0F) {
                CircleProgressView.this.sweepAngle = 360.0F * interpolatedTime;
                CircleProgressView.this.invalidate();
            } else {
                CircleProgressView.this.startAnimAutomatic(CircleProgressView.this.fillIn);
            }

        }
    }
}
