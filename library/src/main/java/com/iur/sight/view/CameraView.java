package com.iur.sight.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.iur.rui.sight2.R;
import com.iur.sight.CameraSize;
import com.iur.sight.listener.CameraFocusListener;
import com.iur.sight.util.AudioUtil;
import com.iur.sight.util.CameraParamUtil;
import com.iur.sight.util.ValueAnimatorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guanzhirui
 */
public class CameraView extends RelativeLayout implements SurfaceHolder.Callback, AutoFocusCallback, CameraFocusListener {
    public final String TAG;
    private PowerManager powerManager;
    private WakeLock wakeLock;
    private Context mContext;
    private VideoView mVideoView;
    private ImageView mImageViewClose;
    private ImageView mImageViewSwitch;
    private FocusView mFocusView;
    private TextView mReminderToast;
    private TextView mTextViewProgress;
    private ImageView mImageViewRetry;
    private ImageView mImageViewSubmit;
    private ImageView mImageViewPlayControl;
    private CaptureButton mCaptureButton;
    private int iconWidth;
    private int iconMargin;
    private int controlIconWidth;
    private int controlIconMargin;
    private int controlIconMarginBottom;
    private String saveVideoPath;
    private String videoFileName;
    private MediaRecorder mediaRecorder;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters mParam;
    private boolean autoFocus;
    private boolean isPlay;
    private boolean isInPreviewState;
    private boolean needPause;
    private int playbackPosition;
    private boolean isRecorder;
    private float screenProp;
    private boolean supportCapture;
    private int maxDuration;
    private long recordDuration;
    private boolean paused;
    private String fileName;
    private Bitmap pictureBitmap;
    private int SELECTED_CAMERA;
    private int CAMERA_POST_POSITION;
    private int CAMERA_FRONT_POSITION;
    private CameraViewListener cameraViewListener;
    private int nowScaleRate;
    private AudioManager audioManager;
    private OnAudioFocusChangeListener audioFocusChangeListener;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.TAG = "Sight-CameraView";
        this.powerManager = null;
        this.wakeLock = null;
        this.iconWidth = 0;
        this.iconMargin = 0;
        this.controlIconWidth = 0;
        this.controlIconMargin = 0;
        this.controlIconMarginBottom = 0;
        this.saveVideoPath = "";
        this.videoFileName = "";
        this.mHolder = null;
        this.isPlay = false;
        this.isInPreviewState = false;
        this.playbackPosition = 0;
        this.isRecorder = false;
        this.supportCapture = false;
        this.maxDuration = 10;
        this.recordDuration = 0L;
        this.SELECTED_CAMERA = -1;
        this.CAMERA_POST_POSITION = -1;
        this.CAMERA_FRONT_POSITION = -1;
        this.nowScaleRate = 0;
        this.mContext = context;
        Context var10002 = this.mContext;
        this.powerManager = (PowerManager)this.mContext.getSystemService(Context.POWER_SERVICE);
        this.wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
        this.findAvailableCameras();
        this.SELECTED_CAMERA = this.CAMERA_POST_POSITION;
        this.iconWidth = (int)this.getResources().getDimension(R.dimen.sight_record_top_icon_size);
        this.iconMargin = (int)this.getResources().getDimension(R.dimen.sight_record_top_icon_margin);
        this.controlIconWidth = (int)this.getResources().getDimension(R.dimen.sight_record_control_icon_size);
        this.controlIconMargin = (int)this.getResources().getDimension(R.dimen.sight_record_control_icon_margin_left);
        this.controlIconMarginBottom = (int)this.getResources().getDimension(R.dimen.sight_record_control_icon_margin_bottom);
        this.initView();
        this.mHolder = this.mVideoView.getHolder();
        this.mHolder.addCallback(this);

        this.audioFocusChangeListener = new OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        };
        this.audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        this.mCaptureButton.setCaptureListener(new CaptureButton.CaptureListener() {
            @Override
            public void capture() {
                if (supportCapture) {
                    takeCapture();
                }
            }

            @Override
            public void cancel() {
                mImageViewSwitch.setVisibility(VISIBLE);
                releaseCamera();
                mCamera = getCamera(SELECTED_CAMERA);
                setStartPreview(mCamera, mHolder);
                recordDuration = 0L;
            }

            @Override
            public void determine() {
                if (cameraViewListener != null) {
                    cameraViewListener.captureSuccess(pictureBitmap);
                }

                mImageViewSwitch.setVisibility(VISIBLE);
                releaseCamera();
                mCamera = getCamera(SELECTED_CAMERA);
                setStartPreview(mCamera, mHolder);
            }

            @Override
            public void quit() {
                if (cameraViewListener != null) {
                    cameraViewListener.quit();
                }
            }

            @Override
            public void record() {
                recordDuration = 0L;
                needPause = false;
                mReminderToast.setVisibility(GONE);
                startRecord();
                mImageViewClose.setVisibility(GONE);
                mImageViewSwitch.setVisibility(GONE);
            }

            @Override
            public void recordEnd(long duration) {
                recordDuration = duration;
                isInPreviewState = true;
                if (recordDuration == 10000L) {
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewProgress.setVisibility(GONE);
                            stopRecord();
                        }
                    }, 1000L);
                } else {
                    mTextViewProgress.setVisibility(GONE);
                    stopRecord();
                }

                needPause = true;
                playRecord();
                setRecordControlViewVisibility(true);
                mImageViewSubmit.setEnabled(true);
                mImageViewRetry.setEnabled(true);
            }

            @Override
            public void getRecordResult() {
                if (cameraViewListener != null) {
                    cameraViewListener.recordSuccess(fileName, (int) recordDuration / 1000);
                }
            }

            @Override
            public void deleteRecordResult() {
                deleteRecordFile();
                mVideoView.stopPlayback();
                releaseCamera();
                if (!paused) {
                    mCamera = getCamera(SELECTED_CAMERA);
                    setStartPreview(mCamera, mHolder);
                }

                isPlay = false;
                isInPreviewState = false;
                recordDuration = 0L;
                needPause = false;
                setRecordControlViewVisibility(false);
                mTextViewProgress.setVisibility(GONE);
                updateReminderView();
            }

            @Override
            public void scale(float scaleValue) {
                if (mCamera != null && mParam != null && mParam.isZoomSupported()) {
                    if (scaleValue >= 0.0F) {
                        int scaleRate = (int)(scaleValue / 50.0F);
                        if (scaleRate < mParam.getMaxZoom() && scaleRate >= 0 && nowScaleRate != scaleRate) {
                            try {
                                mParam.setZoom(scaleRate);
                                mCamera.setParameters(mParam);
                            } catch (Exception var4) {
                                var4.printStackTrace();
                            }

                            nowScaleRate = scaleRate;
                        }
                    }

                }
            }

            @Override
            public void recordProgress(int progress) {
                updateProgressView(progress);
            }

            @Override
            public void retryRecord() {
                stopRecord();
                deleteRecordFile();
                mVideoView.stopPlayback();
                releaseCamera();
                if (!paused) {
                    mCamera = getCamera(SELECTED_CAMERA);
                    setStartPreview(mCamera, mHolder);
                    setRecordControlViewVisibility(false);
                }

                mTextViewProgress.setVisibility(GONE);
                recordDuration = 0L;
            }
        });
    }

    public void setCameraViewListener(CameraViewListener cameraViewListener) {
        this.cameraViewListener = cameraViewListener;
    }

    private void initView() {
        this.setWillNotDraw(false);
        this.setBackgroundColor(-16777216);
        this.mVideoView = new VideoView(this.mContext);
        LayoutParams videoViewParam = new LayoutParams(-1, -2);
        videoViewParam.addRule(13, -1);
        this.mVideoView.setLayoutParams(videoViewParam);
        this.mVideoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mCamera.autoFocus(CameraView.this);
                }

            }
        });
        this.autoFocus = true;
        LayoutParams btnParams = new LayoutParams(-2, -2);
        btnParams.addRule(14, -1);
        btnParams.addRule(12, -1);
        this.mCaptureButton = new CaptureButton(this.mContext);
        this.mCaptureButton.setLayoutParams(btnParams);
        this.mCaptureButton.setId(R.id.rc_sight_record_bottom);
        this.mCaptureButton.setSupportCapture(this.supportCapture);
        ValueAnimatorUtil.resetDurationScaleIfDisable();
        this.mFocusView = new FocusView(this.mContext, 120);
        this.mFocusView.setVisibility(INVISIBLE);
        this.initReminderView();
        this.initCloseView();
        this.initSwitchView();
        this.initProgressView();
        this.initRetryView();
        this.initSubmitView();
        this.initPlayControlView();
        this.addView(mVideoView);
        this.addView(mCaptureButton);
        this.addView(mImageViewClose);
        this.addView(mImageViewSwitch);
        this.addView(mReminderToast);
        this.addView(mFocusView);
        this.addView(mTextViewProgress);
        this.addView(mImageViewRetry);
        this.addView(mImageViewSubmit);
        this.addView(mImageViewPlayControl);
        this.updateReminderView();
    }

    private void initReminderView() {
        this.mReminderToast = new TextView(this.mContext);
        this.mReminderToast.setText(R.string.rc_sight_reminder);
        this.mReminderToast.setTextColor(this.getResources().getColor(R.color.color_sight_white));
        this.mReminderToast.setTextSize(0, this.getResources().getDimension(R.dimen.sight_text_size_14));
        this.mReminderToast.setShadowLayer(16.0F, 0.0F, 2.0F, this.getResources().getColor(R.color.color_sight_record_reminder_shadow));
        int paddingHorizontal = (int)this.getResources().getDimension(R.dimen.sight_text_view_padding_horizontal);
        int paddingVertical = (int)this.getResources().getDimension(R.dimen.sight_text_view_padding_vertical);
        this.mReminderToast.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        LayoutParams toastParams = new LayoutParams(-2, -2);
        toastParams.addRule(14, -1);
        toastParams.addRule(2, this.mCaptureButton.getId());
        this.mReminderToast.setLayoutParams(toastParams);
    }

    private void updateReminderView() {
        this.mReminderToast.setVisibility(VISIBLE);
        this.mReminderToast.postDelayed(new Runnable() {
            @Override
            public void run() {
                mReminderToast.setVisibility(GONE);
            }
        }, 5000L);
    }

    private void initCloseView() {
        this.mImageViewClose = new ImageView(this.mContext);
        LayoutParams imageViewCloseParam = new LayoutParams(this.iconWidth, this.iconWidth);
        imageViewCloseParam.addRule(9, -1);
        imageViewCloseParam.setMargins(this.iconMargin, this.iconMargin, 0, 0);
        this.mImageViewClose.setLayoutParams(imageViewCloseParam);
        this.mImageViewClose.setImageResource(R.drawable.rc_ic_sight_close);
        this.mImageViewClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseCamera();
                if (cameraViewListener != null) {
                    cameraViewListener.quit();
                }

            }
        });
    }

    private void initSwitchView() {
        this.mImageViewSwitch = new ImageView(this.mContext);
        LayoutParams imageViewSwitchParam = new LayoutParams(this.iconWidth, this.iconWidth);
        imageViewSwitchParam.addRule(11, -1);
        imageViewSwitchParam.setMargins(0, this.iconMargin, this.iconMargin, 0);
        this.mImageViewSwitch.setLayoutParams(imageViewSwitchParam);
        this.mImageViewSwitch.setImageResource(R.drawable.rc_ic_sight_switch);
        this.mImageViewSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    releaseCamera();
                    if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                        SELECTED_CAMERA = CAMERA_FRONT_POSITION;
                    } else {
                        SELECTED_CAMERA = CAMERA_POST_POSITION;
                    }

                    mCamera = getCamera(SELECTED_CAMERA);
                    setStartPreview(mCamera, mHolder);
                }

            }
        });
    }

    private void initProgressView() {
        this.mTextViewProgress = new TextView(this.mContext);
        this.mTextViewProgress.setVisibility(GONE);
        this.mTextViewProgress.setTextColor(this.getResources().getColor(R.color.color_sight_white));
        LayoutParams progressParams = new LayoutParams(-2, -2);
        progressParams.addRule(14, -1);
        progressParams.addRule(2, this.mCaptureButton.getId());
        this.mTextViewProgress.setLayoutParams(progressParams);
    }

    private void initRetryView() {
        this.mImageViewRetry = new ImageView(this.mContext);
        LayoutParams imageViewRetryParam = new LayoutParams(this.controlIconWidth, this.controlIconWidth);
        imageViewRetryParam.addRule(9, -1);
        imageViewRetryParam.addRule(12, -1);
        imageViewRetryParam.setMargins(this.controlIconMargin, 0, 0, this.controlIconMarginBottom);
        this.mImageViewRetry.setLayoutParams(imageViewRetryParam);
        this.mImageViewRetry.setImageResource(R.drawable.rc_ic_sight_record_retry);
        this.mImageViewRetry.setVisibility(GONE);
        this.mImageViewRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageViewSubmit.setEnabled(false);
                mCaptureButton.retryRecord();
            }
        });
    }

    private void initSubmitView() {
        mImageViewSubmit = new ImageView(mContext);
        LayoutParams imageViewSubmitParam = new LayoutParams(controlIconWidth, controlIconWidth);
        imageViewSubmitParam.addRule(11, -1);
        imageViewSubmitParam.addRule(12, -1);
        imageViewSubmitParam.setMargins(0, 0, controlIconMargin, controlIconMarginBottom);
        mImageViewSubmit.setLayoutParams(imageViewSubmitParam);
        mImageViewSubmit.setImageResource(R.drawable.rc_ic_sight_record_submit);
        mImageViewSubmit.setVisibility(GONE);
        mImageViewSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageViewRetry.setEnabled(false);
                mCaptureButton.submitRecord();
            }
        });
    }

    private void initPlayControlView() {
        this.mImageViewPlayControl = new ImageView(this.mContext);
        LayoutParams imageViewPlayControlParam = new LayoutParams(this.controlIconWidth, this.controlIconWidth);
        imageViewPlayControlParam.addRule(14, -1);
        imageViewPlayControlParam.addRule(12, -1);
        imageViewPlayControlParam.setMargins(0, 0, 0, this.controlIconMarginBottom);
        this.mImageViewPlayControl.setLayoutParams(imageViewPlayControlParam);
        this.mImageViewPlayControl.setImageResource(R.drawable.rc_ic_sight_record_play);
        this.mImageViewPlayControl.setVisibility(GONE);
        this.mImageViewPlayControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlay) {
                    pauseRecord();
                } else {
                    playRecord();
                }

            }
        });
    }

    private void updateProgressView(int progress) {
        if (this.mTextViewProgress != null) {
            this.mTextViewProgress.setVisibility(VISIBLE);
            this.mTextViewProgress.setText(progress + "\"");
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    private Camera getCamera(int position) {
        Camera camera;
        try {
            camera = Camera.open(position);
        } catch (Exception var4) {
            camera = null;
            var4.printStackTrace();
        }

        return camera;
    }

    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        if (camera == null) {
            Log.d(TAG,"Camera is null");
        } else {
            try {
                this.mParam = camera.getParameters();
                camera.getClass();

//                Size preferSize = new Size(camera, 1280, 720);
                CameraSize preferSize = new CameraSize(1280,720);

                List<CameraSize> list = new ArrayList<>();
                for (Camera.Size sizes : mParam.getSupportedVideoSizes()){
                    CameraSize size = new CameraSize(sizes.width,sizes.height);
                    list.add(size);
                }

                CameraSize previewSize = CameraParamUtil.getInstance().getPreviewSize(list, 1000, this.screenProp, preferSize);
                if (previewSize == null) {
                    CameraSize size = new CameraSize(mParam.getPreviewSize().width,mParam.getPreviewSize().height);
                    previewSize = size;
                }

                if (previewSize != null) {
                    this.mParam.setPreviewSize(previewSize.width, previewSize.height);
                }

                if (this.supportCapture) {

                    List<CameraSize> li = new ArrayList<>();
                    for (Camera.Size size : mParam.getSupportedPictureSizes()){
                        CameraSize size1 = new CameraSize(size.width,size.height);
                        li.add(size1);
                    }

                    CameraSize pictureSize = CameraParamUtil.getInstance().getPictureSize(li, 1200, this.screenProp);
                    this.mParam.setPictureSize(pictureSize.width, pictureSize.height);
                    if (CameraParamUtil.getInstance().isSupportedPictureFormats(this.mParam.getSupportedPictureFormats(), 256)) {
                        this.mParam.setPictureFormat(256);
                        this.mParam.setJpegQuality(100);
                    }
                }

                if (CameraParamUtil.getInstance().isSupportedFocusMode(this.mParam.getSupportedFocusModes(), "auto")) {
                    this.mParam.setFocusMode("auto");
                }

                camera.setParameters(this.mParam);
                this.mParam = camera.getParameters();
                camera.setPreviewDisplay(holder);
                camera.setDisplayOrientation(90);
                camera.startPreview();
                camera.autoFocus(this);
            } catch (Exception var6) {
                Log.d(TAG,"startPreview failed");
                var6.printStackTrace();
            }

        }
    }

    private void releaseCamera() {
        if (this.mCamera != null) {
            this.mCamera.setPreviewCallback((PreviewCallback)null);
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    public void takeCapture() {
        if (this.supportCapture) {
            if (this.autoFocus) {
                this.mCamera.autoFocus(this);
            } else if (this.SELECTED_CAMERA == this.CAMERA_POST_POSITION) {
                this.mCamera.takePicture(null, null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90.0F);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        pictureBitmap = bitmap;
                        mImageViewSwitch.setVisibility(INVISIBLE);
                        mCaptureButton.captureSuccess();
                    }
                });
            } else if (this.SELECTED_CAMERA == this.CAMERA_FRONT_POSITION) {
                this.mCamera.takePicture(null, null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(270.0F);
                        matrix.postScale(-1.0F, 1.0F);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        pictureBitmap = bitmap;
                        mImageViewSwitch.setVisibility(INVISIBLE);
                        mCaptureButton.captureSuccess();
                    }
                });
            }

        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (this.autoFocus) {
            if (this.SELECTED_CAMERA == this.CAMERA_POST_POSITION && success) {
                this.mCamera.takePicture(null, null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90.0F);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        pictureBitmap = bitmap;
                        mImageViewSwitch.setVisibility(INVISIBLE);
                        mCaptureButton.captureSuccess();
                    }
                });
            } else if (this.SELECTED_CAMERA == this.CAMERA_FRONT_POSITION) {
                this.mCamera.takePicture(null, null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(270.0F);
                        matrix.postScale(-1.0F, 1.0F);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        pictureBitmap = bitmap;
                        mImageViewSwitch.setVisibility(INVISIBLE);
                        mCaptureButton.captureSuccess();
                    }
                });
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = (float)MeasureSpec.getSize(widthMeasureSpec);
        float heightSize = (float)MeasureSpec.getSize(heightMeasureSpec);
        this.screenProp = heightSize / widthSize;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG,"surfaceCreated");

        if (!this.isInPreviewState && !this.paused) {
            this.setStartPreview(this.mCamera, holder);
        }

        this.audioManager.requestAudioFocus(this.audioFocusChangeListener, 3, 1);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG,"surfaceChanged");
        this.mHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG,"surfaceDestroyed");

        if (this.cameraViewListener != null) {
            this.cameraViewListener.quit();
        }

        this.releaseCamera();
        this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);
    }

    public void onResume() {
        Log.i(TAG,"onResume isInPreviewState = " + this.isInPreviewState);

        this.mCamera = this.getCamera(this.SELECTED_CAMERA);
        if (this.mCamera == null) {
            Log.i(TAG,"Camera is null!");

        }

        if (this.isInPreviewState) {
            this.mVideoView.resume();
        } else {
            this.paused = false;
        }

        this.wakeLock.acquire();
    }

    public void onPause() {
        Log.i(TAG,"onPause");

        this.paused = true;
        this.mCaptureButton.onPause();
        this.releaseCamera();
        if (this.isInPreviewState) {
            this.playbackPosition = this.mVideoView.getCurrentPosition();
        }

        this.wakeLock.release();
    }

    private void startRecord() {
        Log.i(TAG,"startRecord");

        if (this.isRecorder) {
            try {
                this.mediaRecorder.stop();
                this.mediaRecorder.release();
            } catch (IllegalStateException var8) {
                Log.e(TAG,var8.getMessage());

                this.mediaRecorder = null;
                this.mediaRecorder = new MediaRecorder();
                this.mediaRecorder.stop();
                this.mediaRecorder.release();
            } catch (Exception var9) {
                Log.e(TAG,var9.getMessage());

            }

            this.mediaRecorder = null;
        }

        if (this.mCamera == null) {
            Log.e(TAG,"Camera is null");
            this.stopRecord();
        } else {
            this.mCamera.unlock();

            try {
                if (this.mediaRecorder == null) {
                    this.mediaRecorder = new MediaRecorder();
                }

                this.mediaRecorder.reset();
                this.mediaRecorder.setCamera(this.mCamera);
                this.mediaRecorder.setVideoSource(1);
                this.mediaRecorder.setAudioSource(1);
                this.mediaRecorder.setOutputFormat(2);
                this.mediaRecorder.setVideoEncoder(2);
                this.mediaRecorder.setAudioEncoder(3);
                if (this.mParam == null) {
                    this.mParam = this.mCamera.getParameters();
                }

                Camera var10002 = this.mCamera;
                this.mCamera.getClass();
//                Camera.Size preferSize = new Camera.Size(var10002, 1280, 720);
//                Camera.Size videoSize = CameraParamUtil.getInstance().getVideoSize(this.mParam.getSupportedVideoSizes(), 1000, this.screenProp, preferSize);

                CameraSize preferSize = new CameraSize(1280, 720);
                List<CameraSize> list = new ArrayList<>();
                for (Camera.Size sizes : mParam.getSupportedVideoSizes()){
                    CameraSize size = new CameraSize(sizes.width,sizes.height);
                    list.add(size);
                }
                CameraSize videoSize = CameraParamUtil.getInstance().getVideoSize(list, 1000, this.screenProp, preferSize);

                if (videoSize == null) {
                    Log.e(TAG,"mParam.getSupportedVideoSizes() return null");

                    String defaultVideoSize = this.mParam.get("video-size");
                    if (defaultVideoSize != null) {
                        String[] sizes = defaultVideoSize.split("x");
                        if (sizes.length == 2) {
                            try {
                                var10002 = this.mCamera;
                                this.mCamera.getClass();

                                videoSize = new CameraSize(Integer.parseInt(sizes[0]), Integer.parseInt(sizes[1]));


                            } catch (NumberFormatException var6) {
                                Log.e(TAG,"get video-size got NumberFormatException");

                            }
                        }
                    }
                }

                if (videoSize != null) {
                    this.mediaRecorder.setVideoSize(videoSize.width, videoSize.height);
                }

                if (this.SELECTED_CAMERA == this.CAMERA_FRONT_POSITION) {
                    this.mediaRecorder.setOrientationHint(270);
                } else {
                    this.mediaRecorder.setOrientationHint(90);
                }

                this.mediaRecorder.setMaxDuration(this.maxDuration * 1000);
                this.mediaRecorder.setVideoEncodingBitRate(3145728);
                this.mediaRecorder.setPreviewDisplay(this.mHolder.getSurface());
                this.videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
                if (this.saveVideoPath.equals("")) {
                    this.saveVideoPath = Environment.getExternalStorageDirectory().getPath();
                }

                this.mediaRecorder.setOutputFile(this.saveVideoPath + "/" + this.videoFileName);
                this.mediaRecorder.prepare();
                this.mediaRecorder.start();
                this.isRecorder = true;
                this.updateProgressView(0);
            } catch (Exception var7) {
                Log.e(TAG,"startRecord got exception");

                var7.printStackTrace();
            }

        }
    }

    private void stopRecord() {
        Log.e(TAG,"stopRecord");

        if (this.mediaRecorder != null) {
            this.mediaRecorder.setOnErrorListener(null);
            this.mediaRecorder.setOnInfoListener(null);
            this.mediaRecorder.setPreviewDisplay((Surface)null);

            try {
                this.mediaRecorder.stop();
                this.mediaRecorder.release();
            } catch (Exception var5) {
                Log.e(TAG,"stopRecord got exception");

                var5.printStackTrace();
            } finally {
                this.isRecorder = false;
                this.mediaRecorder = null;
            }

            this.releaseCamera();
            this.fileName = this.saveVideoPath + "/" + this.videoFileName;
            if (this.isInPreviewState) {
                this.mVideoView.setVideoPath(this.fileName);
            }
        }

    }

    private void playRecord() {
        Log.i(TAG,"playRecord");

        if (!this.needPause) {
            this.isPlay = true;
        }

        this.mImageViewPlayControl.setImageResource(this.needPause ? R.drawable.rc_ic_sight_record_play : R.drawable.rc_ic_sight_record_pause);

        try {
            this.mVideoView.start();
            this.mVideoView.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG,"\"playRecord mVideoView onPrepared");

                    if (mp != null) {
                        int duration = mp.getDuration();
                        if (duration != -1 && duration > 1000) {
                            recordDuration = (long)duration;
                        }

                        if (paused) {
                            isPlay = true;
                            needPause = false;
                            paused = false;
                            mImageViewPlayControl.setImageResource(R.drawable.rc_ic_sight_record_pause);
                        }

                        try {
                            if (playbackPosition > 0 || needPause) {
                                if (playbackPosition > 0 || needPause) {
                                    mp.seekTo(playbackPosition);
                                }

                                mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                                    @Override
                                    public void onSeekComplete(MediaPlayer mp) {
                                        if (isInPreviewState && !isPlay || needPause) {
                                            needPause = false;
                                            mp.pause();
                                        }

                                    }
                                });
                                playbackPosition = 0;
                            }

                            if (!needPause) {
                                mp.start();
                            }

                            mp.setLooping(true);
                            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    Log.i(TAG,"record play error on MediaPlayer onPrepared ,what = " + what + " extra = " + extra);

                                    return true;
                                }
                            });
                        } catch (Exception var4) {
                            Log.i(TAG,"mVideoView onPrepared got error");

                            var4.printStackTrace();
                        }

                    }
                }
            });
            this.mVideoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mp != null) {
                        mp.setDisplay((SurfaceHolder)null);
                        mp.reset();
                        mp.setDisplay(mVideoView.getHolder());
                    }

                    mVideoView.setVideoPath(fileName);
                    mVideoView.start();
                }
            });
            this.mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.i(TAG,"record play error,what = " + what + " extra = " + extra);

                    mImageViewSubmit.setEnabled(false);
                    return true;
                }
            });
        } catch (Exception var2) {
            this.mImageViewSubmit.setEnabled(false);
            Log.i(TAG,"mVideoView play error");

            var2.printStackTrace();
        }

    }

    private void pauseRecord() {
        Log.i(TAG,"pauseRecord");

        this.mImageViewPlayControl.setImageResource(R.drawable.rc_ic_sight_record_play);
        this.mVideoView.pause();
        this.isPlay = false;
    }

    public void setSaveVideoPath(String saveVideoPath) {
        this.saveVideoPath = saveVideoPath;
    }

    private void findAvailableCameras() {
        CameraInfo info = new CameraInfo();
        int numCamera = Camera.getNumberOfCameras();

        for(int i = 0; i < numCamera; ++i) {
            Camera.getCameraInfo(i, info);
            if (info.facing == 1) {
                this.CAMERA_FRONT_POSITION = info.facing;
                Log.i(TAG,"POSITION = " + this.CAMERA_FRONT_POSITION);

            }

            if (info.facing == 0) {
                this.CAMERA_POST_POSITION = info.facing;
                Log.i(TAG,"POSITION = " + this.CAMERA_POST_POSITION);

            }
        }

    }

    public void setAutoFocus(boolean autoFocus) {
        this.autoFocus = autoFocus;
    }

    @Override
    public void onFocusBegin(float x, float y) {
        this.mFocusView.setVisibility(VISIBLE);
        this.mFocusView.setX(x - (float)(this.mFocusView.getWidth() / 2));
        this.mFocusView.setY(y - (float)(this.mFocusView.getHeight() / 2));
        if (this.mCamera != null) {
            try {
                this.mCamera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            mCamera.cancelAutoFocus();
                            onFocusEnd();
                        }

                    }
                });
            } catch (Exception var4) {
                Log.e(TAG,"autoFocus failed ");
                this.onFocusEnd();
            }
        }

    }

    @Override
    public void onFocusEnd() {
        this.mFocusView.setVisibility(INVISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!this.autoFocus && event.getAction() == 0 && this.SELECTED_CAMERA == this.CAMERA_POST_POSITION && !this.isInPreviewState) {
            this.onFocusBegin(event.getX(), event.getY());
        }

        return super.onTouchEvent(event);
    }

    public void cancelAudio() {
        AudioUtil.setAudioManage(this.mContext);
    }

    public void setSupportCapture(boolean support) {
        this.supportCapture = support;
        if (this.mCaptureButton != null) {
            this.mCaptureButton.setSupportCapture(support);
        }

    }

    public void setMaxRecordDuration(int duration) {
        this.maxDuration = duration;
        if (this.mCaptureButton != null) {
            this.mCaptureButton.setMaxRecordDuration(duration);
        }
    }

    private void setRecordControlViewVisibility(boolean visual) {
        this.mImageViewClose.setVisibility(visual ? GONE : VISIBLE);
        this.mImageViewSwitch.setVisibility(visual ? GONE : VISIBLE);
        this.mImageViewSubmit.setVisibility(visual ? VISIBLE : GONE);
        this.mImageViewRetry.setVisibility(visual ? VISIBLE : GONE);
        this.mImageViewPlayControl.setVisibility(visual ? VISIBLE :GONE);
    }

    private void deleteRecordFile() {
        if (this.fileName == null || TextUtils.isEmpty(this.fileName)) {
            this.fileName = this.saveVideoPath + "/" + this.videoFileName;
        }

        File file = new File(this.fileName);
        if (file.exists()) {
            file.delete();
        }

    }

    public interface CameraViewListener {
        void quit();

        void captureSuccess(Bitmap var1);

        void recordSuccess(String var1, int var2);
    }
}
