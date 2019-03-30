package com.example.rui.sight2.sight.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class FocusView extends View {

    private int focusView_size;
    private int x;
    private int y;
    private int length;
    private Paint mPaint;

    public FocusView(Context context, int size) {
        super(context);
        this.focusView_size = size;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mPaint.setColor(-16724992);
        this.mPaint.setStrokeWidth(1.0F);
        this.mPaint.setStyle(Paint.Style.STROKE);
    }

    private FocusView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.x = this.y = (int)((double)this.focusView_size / 2.0D);
        this.length = (int)((double)this.focusView_size / 2.0D) - 2;
        this.setMeasuredDimension(this.focusView_size, this.focusView_size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect((float)(this.x - this.length), (float)(this.y - this.length), (float)(this.x + this.length), (float)(this.y + this.length), this.mPaint);
        canvas.drawLine(2.0F, (float)(this.getHeight() / 2), (float)(this.focusView_size / 10), (float)(this.getHeight() / 2), this.mPaint);
        canvas.drawLine((float)(this.getWidth() - 2), (float)(this.getHeight() / 2), (float)(this.getWidth() - this.focusView_size / 10), (float)(this.getHeight() / 2), this.mPaint);
        canvas.drawLine((float)(this.getWidth() / 2), 2.0F, (float)(this.getWidth() / 2), (float)(this.focusView_size / 10), this.mPaint);
        canvas.drawLine((float)(this.getWidth() / 2), (float)(this.getHeight() - 2), (float)(this.getWidth() / 2), (float)(this.getHeight() - this.focusView_size / 10), this.mPaint);
    }

}
