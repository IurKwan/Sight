package com.example.rui.sight2.sight.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.example.rui.sight2.R;

public class CaptureButton extends View {
    public final String TAG;
    private Paint mPaint;
    private Context mContext;
    private float btn_center_Y;
    private float btn_center_X;
    private float btn_inside_radius;
    private float btn_outside_radius;
    private float btn_before_inside_radius;
    private float btn_before_outside_radius;
    private float btn_after_inside_radius;
    private float btn_after_outside_radius;
    private float btn_return_length;
    private float btn_return_X;
    private float btn_return_Y;
    private float btn_left_X;
    private float btn_right_X;
    private float btn_result_radius;
    private int STATE_SELECTED;
    private final int STATE_LESSNESS;
    private final int STATE_KEY_DOWN;
    private final int STATE_CAPTURED;
    private final int STATE_RECORD;
    private final int STATE_PICTURE_BROWSE;
    private final int STATE_RECORD_BROWSE;
    private final int STATE_READYQUIT;
    private final int STATE_RECORDED;
    private float key_down_Y;
    private RectF rectF;
    private float progress;
    private int captureProgressed;
    private LongPressRunnable longPressRunnable;
    private RecordRunnable recordRunnable;
    private ValueAnimator record_anim;
    private CaptureListener mCaptureListener;
    private boolean supportCapture;
    private int maxDuration;
    private boolean hideSomeView;

    public CaptureButton(Context context) {
        this(context, (AttributeSet)null);
    }

    public CaptureButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.TAG = "Sight-CaptureButton";
        this.STATE_LESSNESS = 0;
        this.STATE_KEY_DOWN = 1;
        this.STATE_CAPTURED = 2;
        this.STATE_RECORD = 3;
        this.STATE_PICTURE_BROWSE = 4;
        this.STATE_RECORD_BROWSE = 5;
        this.STATE_READYQUIT = 6;
        this.STATE_RECORDED = 7;
        this.progress = 0.0F;
        this.captureProgressed = 0;
        this.longPressRunnable = new LongPressRunnable();
        this.recordRunnable = new RecordRunnable();
        this.record_anim = ValueAnimator.ofFloat(new float[]{0.0F, 360.0F});
        this.supportCapture = false;
        this.maxDuration = 10;
        this.hideSomeView = true;
        this.mContext = context;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.STATE_SELECTED = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height = widthSize / 9 * 4;
        this.setMeasuredDimension(widthSize, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.btn_center_X = (float)(this.getWidth() / 2);
        this.btn_center_Y = (float)(this.getHeight() / 2);
        if (!this.hideSomeView) {
            this.btn_outside_radius = (float)(this.getWidth() / 9);
            this.btn_inside_radius = (float)((double)this.btn_outside_radius * 0.75D);
            this.btn_before_outside_radius = (float)(this.getWidth() / 9);
            this.btn_before_inside_radius = (float)((double)this.btn_outside_radius * 0.75D);
            this.btn_after_outside_radius = (float)(this.getWidth() / 6);
            this.btn_after_inside_radius = (float)((double)this.btn_outside_radius * 0.6D);
        } else {
            this.initCaptureButtonRadius();
            this.btn_before_outside_radius = this.getResources().getDimension(R.dimen.sight_capture_button_circle_size_outer);
            this.btn_before_inside_radius = this.getResources().getDimension(R.dimen.sight_capture_button_circle_size_inner);
            this.btn_after_outside_radius = this.getResources().getDimension(R.dimen.sight_capture_button_record_circle_size_outer);
            this.btn_after_inside_radius = this.getResources().getDimension(R.dimen.sight_capture_button_record_circle_size_inner);
        }

        this.btn_return_length = (float)((double)this.btn_outside_radius * 0.35D);
        this.btn_result_radius = (float)(this.getWidth() / 9);
        this.btn_left_X = (float)(this.getWidth() / 2);
        this.btn_right_X = (float)(this.getWidth() / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint;
        if (this.STATE_SELECTED != 0 && this.STATE_SELECTED != 3) {
            if (this.STATE_SELECTED == 5 || this.STATE_SELECTED == 4) {
                if (this.hideSomeView) {
                    return;
                }

                this.mPaint.setColor(this.getResources().getColor(R.color.color_sight_capture_button_circle_outer));
                canvas.drawCircle(this.btn_left_X, this.btn_center_Y, this.btn_result_radius, this.mPaint);
                this.mPaint.setColor(-1);
                canvas.drawCircle(this.btn_right_X, this.btn_center_Y, this.btn_result_radius, this.mPaint);
                paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(-16777216);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3.0F);
                Path path = new Path();
                path.moveTo(this.btn_left_X - 2.0F, this.btn_center_Y + 14.0F);
                path.lineTo(this.btn_left_X + 14.0F, this.btn_center_Y + 14.0F);
                path.arcTo(new RectF(this.btn_left_X, this.btn_center_Y - 14.0F, this.btn_left_X + 28.0F, this.btn_center_Y + 14.0F), 90.0F, -180.0F);
                path.lineTo(this.btn_left_X - 14.0F, this.btn_center_Y - 14.0F);
                canvas.drawPath(path, paint);
                paint.setStyle(Paint.Style.FILL);
                path.reset();
                path.moveTo(this.btn_left_X - 14.0F, this.btn_center_Y - 22.0F);
                path.lineTo(this.btn_left_X - 14.0F, this.btn_center_Y - 6.0F);
                path.lineTo(this.btn_left_X - 23.0F, this.btn_center_Y - 14.0F);
                path.close();
                canvas.drawPath(path, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(-16724992);
                paint.setStrokeWidth(4.0F);
                path.reset();
                path.moveTo(this.btn_right_X - 28.0F, this.btn_center_Y);
                path.lineTo(this.btn_right_X - 8.0F, this.btn_center_Y + 22.0F);
                path.lineTo(this.btn_right_X + 30.0F, this.btn_center_Y - 20.0F);
                path.lineTo(this.btn_right_X - 8.0F, this.btn_center_Y + 18.0F);
                path.close();
                canvas.drawPath(path, paint);
            }
        } else {
            this.mPaint.setColor(this.getResources().getColor(R.color.color_sight_capture_button_circle_outer));
            canvas.drawCircle(this.btn_center_X, this.btn_center_Y, this.btn_outside_radius, this.mPaint);
            this.mPaint.setColor(-1);
            canvas.drawCircle(this.btn_center_X, this.btn_center_Y, this.btn_inside_radius, this.mPaint);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(this.getResources().getColor(R.color.color_sight_primary));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10.0F);
            this.rectF = new RectF(this.btn_center_X - (this.btn_after_outside_radius - 5.0F), this.btn_center_Y - (this.btn_after_outside_radius - 5.0F), this.btn_center_X + (this.btn_after_outside_radius - 5.0F), this.btn_center_Y + (this.btn_after_outside_radius - 5.0F));
            canvas.drawArc(this.rectF, -90.0F, this.progress, false, paint);
            if (!this.hideSomeView) {
                Paint paint2 = new Paint();
                paint2.setAntiAlias(true);
                paint2.setColor(-1);
                paint2.setStyle(Paint.Style.STROKE);
                paint2.setStrokeWidth(4.0F);
                Path path = new Path();
                this.btn_return_X = ((float)(this.getWidth() / 2) - this.btn_outside_radius) / 2.0F;
                this.btn_return_Y = (float)(this.getHeight() / 2 + 10);
                path.moveTo(this.btn_return_X - this.btn_return_length, this.btn_return_Y - this.btn_return_length);
                path.lineTo(this.btn_return_X, this.btn_return_Y);
                path.lineTo(this.btn_return_X + this.btn_return_length, this.btn_return_Y - this.btn_return_length);
                canvas.drawPath(path, paint);
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case 0:
                if (this.STATE_SELECTED == 0) {
                    if (!this.hideSomeView) {
                        if (event.getY() > this.btn_return_Y - 37.0F && event.getY() < this.btn_return_Y + 10.0F && event.getX() > this.btn_return_X - 37.0F && event.getX() < this.btn_return_X + 37.0F) {
                            this.STATE_SELECTED = 6;
                        }
                    } else if (event.getY() > this.btn_center_Y - this.btn_outside_radius && event.getY() < this.btn_center_Y + this.btn_outside_radius && event.getX() > this.btn_center_X - this.btn_outside_radius && event.getX() < this.btn_center_X + this.btn_outside_radius && event.getPointerCount() == 1) {
                        this.key_down_Y = event.getY();
                        this.STATE_SELECTED = 1;
                        this.postCheckForLongTouch();
                    }
                } else if (this.STATE_SELECTED == 5 || this.STATE_SELECTED == 4) {
                    if (event.getY() > this.btn_center_Y - this.btn_result_radius && event.getY() < this.btn_center_Y + this.btn_result_radius && event.getX() > this.btn_left_X - this.btn_result_radius && event.getX() < this.btn_left_X + this.btn_result_radius && event.getPointerCount() == 1) {
                        this.retryRecord();
                    } else if (event.getY() > this.btn_center_Y - this.btn_result_radius && event.getY() < this.btn_center_Y + this.btn_result_radius && event.getX() > this.btn_right_X - this.btn_result_radius && event.getX() < this.btn_right_X + this.btn_result_radius && event.getPointerCount() == 1) {
                        this.submitRecord();
                    }
                }
                break;
            case 1:
                this.removeCallbacks(this.longPressRunnable);
                if (this.STATE_SELECTED == 6) {
                    if (!this.hideSomeView && event.getY() > this.btn_return_Y - 37.0F && event.getY() < this.btn_return_Y + 10.0F && event.getX() > this.btn_return_X - 37.0F && event.getX() < this.btn_return_X + 37.0F) {
                        this.STATE_SELECTED = 0;
                        if (this.mCaptureListener != null) {
                            this.mCaptureListener.quit();
                        }
                    }
                } else if (this.STATE_SELECTED == 1) {
                    if (this.supportCapture) {
                        if (event.getY() > this.btn_center_Y - this.btn_outside_radius && event.getY() < this.btn_center_Y + this.btn_outside_radius && event.getX() > this.btn_center_X - this.btn_outside_radius && event.getX() < this.btn_center_X + this.btn_outside_radius) {
                            this.capture();
                        }
                    } else {
                        this.STATE_SELECTED = 0;
                        this.initCaptureButtonRadius();
                        this.invalidate();
                    }
                } else if (this.STATE_SELECTED == 3) {
                    this.recordEnd(true);
                }
                break;
            case 2:
                if (event.getY() > this.btn_center_Y - this.btn_outside_radius && event.getY() < this.btn_center_Y + this.btn_outside_radius && event.getX() > this.btn_center_X - this.btn_outside_radius && event.getX() < this.btn_center_X + this.btn_outside_radius) {
                    ;
                }

                if (this.mCaptureListener != null) {
                    this.mCaptureListener.scale(this.key_down_Y - event.getY());
                }
        }

        return true;
    }

    public void captureSuccess() {
        this.captureAnimation((float)(this.getWidth() / 5), (float)(this.getWidth() / 5 * 4));
    }

    private void postCheckForLongTouch() {
        this.postDelayed(this.longPressRunnable, 500L);
    }

    private void startAnimation(float outside_start, float outside_end, float inside_start, float inside_end) {
        ValueAnimator outside_anim = ValueAnimator.ofFloat(new float[]{outside_start, outside_end});
        ValueAnimator inside_anim = ValueAnimator.ofFloat(new float[]{inside_start, inside_end});
        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_outside_radius = (Float)animation.getAnimatedValue();
                invalidate();
            }
        });
        outside_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (STATE_SELECTED == 3) {
                    postDelayed(recordRunnable, 100L);
                }

            }
        });
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_inside_radius = (Float)animation.getAnimatedValue();
                invalidate();
            }
        });
        outside_anim.setDuration(100L);
        inside_anim.setDuration(100L);
        outside_anim.start();
        inside_anim.start();
    }

    private void captureAnimation(float left, float right) {
        ValueAnimator left_anim = ValueAnimator.ofFloat(new float[]{this.btn_left_X, left});
        ValueAnimator right_anim = ValueAnimator.ofFloat(new float[]{this.btn_right_X, right});
        left_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_left_X = (Float)animation.getAnimatedValue();
                invalidate();
            }
        });
        right_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_right_X = (Float)animation.getAnimatedValue();
                invalidate();
            }
        });
        left_anim.setDuration(200L);
        right_anim.setDuration(200L);
        left_anim.start();
        right_anim.start();
    }

    public void setCaptureListener(CaptureListener mCaptureListener) {
        this.mCaptureListener = mCaptureListener;
    }

    public void setSupportCapture(boolean support) {
        this.supportCapture = support;
    }

    public void setMaxRecordDuration(int duration) {
        this.maxDuration = duration;
    }

    public void submitRecord() {
        if (this.mCaptureListener != null) {
            if (this.STATE_SELECTED == 5) {
                this.mCaptureListener.getRecordResult();
            } else if (this.STATE_SELECTED == 4) {
                this.mCaptureListener.determine();
            }
        }

        this.btn_left_X = this.btn_center_X;
        this.btn_right_X = this.btn_center_X;
        this.invalidate();
    }

    public void retryRecord() {
        if (this.mCaptureListener != null) {
            if (this.STATE_SELECTED == 5) {
                this.mCaptureListener.deleteRecordResult();
            } else if (this.STATE_SELECTED == 4) {
                this.mCaptureListener.cancel();
            }
        }

        this.STATE_SELECTED = 0;
        this.btn_left_X = this.btn_center_X;
        this.btn_right_X = this.btn_center_X;
        this.invalidate();
    }

    private void capture() {
        if (this.supportCapture) {
            if (this.mCaptureListener != null) {
                this.mCaptureListener.capture();
            }

            this.STATE_SELECTED = 4;
        } else {
            this.STATE_SELECTED = 0;
            this.initCaptureButtonRadius();
            this.invalidate();
        }

    }

    private void recordEnd(boolean needAnimation) {
        long playTime = this.record_anim.getCurrentPlayTime();
        Log.d("Sight-CaptureButton", "recordEnd " + playTime);
        this.progress = 0.0F;
        this.captureProgressed = 0;
        if (playTime < 1000L) {
            Log.d("Sight-CaptureButton", "recordEnd-retryRecord()");
            if (needAnimation) {
                Toast.makeText(this.mContext, R.string.rc_sight_record_too_short_time, Toast.LENGTH_SHORT).show();
            }

            if (this.mCaptureListener != null) {
                this.mCaptureListener.retryRecord();
            }

            this.STATE_SELECTED = 0;
            this.record_anim.cancel();
            this.invalidate();
        } else {
            this.STATE_SELECTED = 5;
            this.removeCallbacks(this.recordRunnable);
            if (needAnimation) {
                this.captureAnimation((float)(this.getWidth() / 5), (float)(this.getWidth() / 5 * 4));
            }

            this.record_anim.cancel();
            this.invalidate();
            if (this.mCaptureListener != null) {
                this.mCaptureListener.recordEnd(playTime);
            }
        }

        if (needAnimation) {
            if (this.btn_outside_radius == this.btn_after_outside_radius && this.btn_inside_radius == this.btn_after_inside_radius) {
                this.startAnimation(this.btn_after_outside_radius, this.btn_before_outside_radius, this.btn_after_inside_radius, this.btn_before_inside_radius);
            } else {
                this.startAnimation(this.btn_after_outside_radius, this.btn_before_outside_radius, this.btn_after_inside_radius, this.btn_before_inside_radius);
            }
        } else {
            this.initCaptureButtonRadius();
        }

    }

    public void onPause() {
        Log.d("Sight-CaptureButton", "onPause");
        this.removeCallbacks(this.longPressRunnable);
        if (this.STATE_SELECTED == 1) {
            this.capture();
        } else if (this.STATE_SELECTED == 3) {
            this.recordEnd(false);
        }

    }

    private void initCaptureButtonRadius() {
        this.btn_outside_radius = this.getResources().getDimension(R.dimen.sight_capture_button_circle_size_outer);
        this.btn_inside_radius = this.getResources().getDimension(R.dimen.sight_capture_button_circle_size_inner);
    }

    public interface CaptureListener {
        void capture();

        void cancel();

        void determine();

        void quit();

        void record();

        void recordEnd(long var1);

        void getRecordResult();

        void deleteRecordResult();

        void scale(float var1);

        void recordProgress(int var1);

        void retryRecord();
    }

    private class RecordRunnable implements Runnable {
        private RecordRunnable() {
        }

        @Override
        public void run() {
            if (mCaptureListener != null) {
                mCaptureListener.record();
            }

            record_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (STATE_SELECTED == 3) {
                        progress = (Float)animation.getAnimatedValue();
                        int newProgress = (int)(animation.getCurrentPlayTime() / 1000L);
                        if (newProgress != captureProgressed) {
                            captureProgressed = newProgress;
                            if (mCaptureListener != null && captureProgressed >= 1) {
                                mCaptureListener.recordProgress(captureProgressed);
                            }
                        }

                        invalidate();
                    }

                }
            });
            record_anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (STATE_SELECTED == 3) {
                        STATE_SELECTED = 5;
                        progress = 0.0F;
                        captureProgressed = 0;
                        invalidate();
                        captureAnimation((float)(getWidth() / 5), (float)(getWidth() / 5 * 4));
                        if (btn_outside_radius == btn_after_outside_radius && btn_inside_radius == btn_after_inside_radius) {
                            startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                        } else {
                            startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                        }

                        if (mCaptureListener != null) {
                            mCaptureListener.recordEnd((long)(maxDuration * 1000));
                        }
                    }

                }
            });
            record_anim.setInterpolator(new LinearInterpolator());
            record_anim.setDuration((long)(maxDuration * 1000));
            record_anim.start();
        }
    }

    private class LongPressRunnable implements Runnable {
        private LongPressRunnable() {
        }

        @Override
        public void run() {
            startAnimation(btn_before_outside_radius, btn_after_outside_radius, btn_before_inside_radius, btn_after_inside_radius);
            STATE_SELECTED = 3;
        }
    }
}

