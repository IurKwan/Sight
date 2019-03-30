package com.example.rui.sight2.sight.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rui.sight2.R;
import com.example.rui.sight2.sight.callback.EasyVideoCallback;

import java.io.IOException;

@TargetApi(14)
public class EasyVideoPlayer extends FrameLayout implements IUserMethods,
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnErrorListener,
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnInfoListener {

    public static final String TAG = "Sight-EasyVideoPlayer";
    private static final int UPDATE_INTERVAL = 100;
    private TextureView mTextureView;
    private Surface mSurface;
    private View mControlsFrame;
    private View mClickFrame;
    private ImageView mImageViewClose;
    private ImageView mImageViewSightList;
    private ImageView mImageViewPlay;
    private SeekBar mSeeker;
    private TextView mCurrent;
    private TextView mTime;
    private ImageView mBtnPlayPause;
    private MediaPlayer mPlayer;
    private boolean mSurfaceAvailable;
    private boolean mIsPrepared;
    private boolean mWasPlaying;
    private Handler mHandler;
    private Uri mSource;
    private EasyVideoCallback mCallback;
    private EasyVideoProgressCallback mProgressCallback;
    private boolean mHideControlsOnPlay = true;
    private boolean mAutoPlay;
    private int mInitialPosition = -1;
    private boolean mControlsDisabled;
    private int mThemeColor = 0;
    private boolean mAutoFullscreen = false;
    private boolean mLoop = false;
    private EasyVideoPlayer.OnInfoCallBack onInfoCallBack;
    private final Runnable mUpdateCounters = new Runnable() {
        @Override
        public void run() {
            if (EasyVideoPlayer.this.mHandler != null && EasyVideoPlayer.this.mIsPrepared && EasyVideoPlayer.this.mSeeker != null && EasyVideoPlayer.this.mPlayer != null) {
                int pos = EasyVideoPlayer.this.mPlayer.getCurrentPosition();
                int dur = EasyVideoPlayer.this.mPlayer.getDuration();
                if (pos > dur) {
                    pos = dur;
                }

                EasyVideoPlayer.this.mCurrent.setText(Util.getDurationString((long)pos));
                EasyVideoPlayer.this.mTime.setText(Util.getDurationString((long)dur));
                EasyVideoPlayer.this.mSeeker.setProgress(pos);
                EasyVideoPlayer.this.mSeeker.setMax(dur);
                if (EasyVideoPlayer.this.mProgressCallback != null) {
                    EasyVideoPlayer.this.mProgressCallback.onVideoProgressUpdate(pos, dur);
                }

                if (EasyVideoPlayer.this.mHandler != null) {
                    EasyVideoPlayer.this.mHandler.postDelayed(this, 100L);
                }

            }
        }
    };

    public EasyVideoPlayer(Context context) {
        super(context);
        this.init(context, (AttributeSet)null);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.setBackgroundColor(-16777216);
        this.mHideControlsOnPlay = true;
        this.mAutoPlay = true;
        this.mControlsDisabled = false;
        this.mThemeColor = 4149685;
        this.mAutoFullscreen = false;
        this.mLoop = false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.setKeepScreenOn(true);
        this.mHandler = new Handler();
        this.mPlayer = new MediaPlayer();
        this.mPlayer.setOnPreparedListener(this);
        this.mPlayer.setOnBufferingUpdateListener(this);
        this.mPlayer.setOnCompletionListener(this);
        this.mPlayer.setOnVideoSizeChangedListener(this);
        this.mPlayer.setOnErrorListener(this);
        this.mPlayer.setAudioStreamType(3);
        this.mPlayer.setLooping(this.mLoop);
        this.mPlayer.setOnInfoListener(this);
        LayoutParams textureLp = new LayoutParams(-1, -1);
        textureLp.gravity = 16;
        this.mTextureView = new TextureView(this.getContext());
        this.addView(this.mTextureView, textureLp);
        this.mTextureView.setSurfaceTextureListener(this);
        LayoutInflater li = LayoutInflater.from(this.getContext());
        this.mClickFrame = new FrameLayout(this.getContext());
        this.addView(this.mClickFrame, new android.view.ViewGroup.LayoutParams(-1, -1));
        this.initPlayView();
        this.initCloseView();
        this.initSightListView();
        this.mControlsFrame = li.inflate(R.layout.rc_sight_play_control, this, false);
        LayoutParams controlsLp = new LayoutParams(-1, -2);
        controlsLp.gravity = 80;
        this.addView(this.mControlsFrame, controlsLp);
        if (this.mControlsFrame != null) {
            this.mControlsFrame.setVisibility(INVISIBLE);
        }

        if (this.mControlsDisabled) {
            this.mClickFrame.setOnClickListener(null);
            this.mControlsFrame.setVisibility(GONE);
        } else {
            this.mClickFrame.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    EasyVideoPlayer.this.toggleControls();
                }
            });
        }

        this.mSeeker = (SeekBar)this.mControlsFrame.findViewById(R.id.seeker);
        this.mSeeker.setOnSeekBarChangeListener(this);
        this.mCurrent = (TextView)this.mControlsFrame.findViewById(R.id.current);
        this.mCurrent.setText(Util.getDurationString(0L));
        this.mTime = (TextView)this.mControlsFrame.findViewById(R.id.time);
        this.mTime.setText(Util.getDurationString(0L));
        this.mBtnPlayPause = (ImageView)this.mControlsFrame.findViewById(R.id.btnPlayPause);
        this.mBtnPlayPause.setOnClickListener(this);
        this.mBtnPlayPause.setImageResource(R.drawable.rc_ic_sight_pause);
        this.invalidateThemeColors();
        this.setControlsEnabled(false);
        this.prepare();
    }

    private void initPlayView() {
        this.mImageViewPlay = new ImageView(this.getContext());
        LayoutParams imageViewPlayParam = new LayoutParams(-2, -2);
        imageViewPlayParam.gravity = 17;
        this.mImageViewPlay.setImageResource(R.drawable.rc_ic_sight_player_paly);
        this.addView(this.mImageViewPlay, imageViewPlayParam);
        this.mImageViewPlay.setVisibility(GONE);
        this.mImageViewPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyVideoPlayer.this.start();
            }
        });
    }

    private void initCloseView() {
        this.mImageViewClose = new ImageView(this.getContext());
        LayoutParams imageViewCloseParam = new LayoutParams(-2, -2);
        imageViewCloseParam.gravity = 51;
        int iconMargin = (int)this.getResources().getDimension(R.dimen.sight_record_top_icon_margin);
        imageViewCloseParam.setMargins(iconMargin, iconMargin, 0, 0);
        this.mImageViewClose.setImageResource(R.drawable.rc_ic_sight_close);
        this.addView(this.mImageViewClose, imageViewCloseParam);
        this.mImageViewClose.setVisibility(GONE);
        this.mImageViewClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EasyVideoPlayer.this.mCallback != null) {
                    EasyVideoPlayer.this.mCallback.onClose();
                }

            }
        });
    }

    private void initSightListView() {
        this.mImageViewSightList = new ImageView(this.getContext());
        LayoutParams imageViewSightListParam = new LayoutParams(-2, -2);
        imageViewSightListParam.gravity = 53;
        int iconMargin = (int)this.getResources().getDimension(R.dimen.sight_record_top_icon_margin);
        imageViewSightListParam.setMargins(0, iconMargin, iconMargin, 0);
        this.mImageViewSightList.setImageResource(R.drawable.rc_ic_sight_list);
        this.addView(this.mImageViewSightList, imageViewSightListParam);
//        this.mImageViewSightList.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                if (EasyVideoPlayer.this.mCallback != null) {
//                    EasyVideoPlayer.this.mCallback.onSightListRequest();
//                }
//
//            }
//        });
    }

    @Override
    public void setSource(@NonNull Uri source) {
        boolean hadSource = this.mSource != null;
        if (hadSource) {
            this.stop();
        }

        this.mSource = source;
        if (this.mPlayer != null) {
            if (hadSource) {
                this.sourceChanged();
            } else {
                this.prepare();
            }
        }

    }

    @Override
    public void setCallback(@NonNull EasyVideoCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void setProgressCallback(@NonNull EasyVideoProgressCallback callback) {
        this.mProgressCallback = callback;
    }

    @Override
    public void setHideControlsOnPlay(boolean hide) {
        this.mHideControlsOnPlay = hide;
    }

    @Override
    public void setAutoPlay(boolean autoPlay) {
        this.mAutoPlay = autoPlay;
    }

    @Override
    public void setInitialPosition(@IntRange(from = 0L,to = 2147483647L) int pos) {
        this.mInitialPosition = pos;
    }

    private void sourceChanged() {
        this.setControlsEnabled(false);
        this.mSeeker.setProgress(0);
        this.mSeeker.setEnabled(false);
        this.mPlayer.reset();
        if (this.mCallback != null) {
            this.mCallback.onPreparing(this);
        }

        try {
            this.setSourceInternal();
        } catch (IOException var2) {
            this.throwError(var2);
        }

    }

    private void setSourceInternal() throws IOException {
        if (this.mSource.getScheme() == null || !this.mSource.getScheme().equals("http") && !this.mSource.getScheme().equals("https")) {
            AssetFileDescriptor afd;
            if (this.mSource.getScheme() != null && this.mSource.getScheme().equals("file") && this.mSource.getPath().contains("/android_assets/")) {
                Log.d(TAG, "Loading assets URI: " + this.mSource.toString());
                afd = this.getContext().getAssets().openFd(this.mSource.toString().replace("file:///android_assets/", ""));
                this.mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            } else if (this.mSource.getScheme() != null && this.mSource.getScheme().equals("asset")) {
                Log.d(TAG, "Loading assets URI: " + this.mSource.toString());
                afd = this.getContext().getAssets().openFd(this.mSource.toString().replace("asset://", ""));
                this.mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            } else {
                Log.d(TAG,  "Loading local URI: " + this.mSource.toString());
                this.mPlayer.setDataSource(this.getContext(), this.mSource);
            }
        } else {
            Log.d(TAG,  "Loading web URI: " + this.mSource.toString());
            this.mPlayer.setDataSource(this.mSource.toString());
        }

        this.mPlayer.prepareAsync();
    }

    private void prepare() {
        if (this.mSurfaceAvailable && this.mSource != null && this.mPlayer != null && !this.mIsPrepared) {
            if (this.mCallback != null) {
                this.mCallback.onPreparing(this);
            }

            try {
                this.mPlayer.setSurface(this.mSurface);
                this.setSourceInternal();
            } catch (IOException var2) {
                this.throwError(var2);
            }

        }
    }

    private void setControlsEnabled(boolean enabled) {
        if (this.mSeeker != null) {
            this.mSeeker.setEnabled(enabled);
            this.mBtnPlayPause.setEnabled(enabled);
            float disabledAlpha = 0.4F;
            this.mBtnPlayPause.setAlpha(enabled ? 1.0F : 0.4F);
            this.mClickFrame.setEnabled(enabled);
        }
    }

    public void setFromSightListImageInVisible() {
        this.mImageViewSightList.setVisibility(INVISIBLE);
    }

    @Override
    public void showControls() {
        if (!this.mControlsDisabled && !this.isControlsShown() && this.mSeeker != null) {
            this.mControlsFrame.animate().cancel();
            this.mControlsFrame.setAlpha(0.0F);
            this.mControlsFrame.setVisibility(VISIBLE);
            this.mImageViewClose.setVisibility(VISIBLE);
            this.mControlsFrame.animate().alpha(1.0F).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (EasyVideoPlayer.this.mAutoFullscreen) {
                        EasyVideoPlayer.this.setFullscreen(false);
                    }

                }
            }).start();
        }
    }

    @Override
    public void hideControls() {
        if (!this.mControlsDisabled && this.isControlsShown() && this.mSeeker != null) {
            this.mControlsFrame.animate().cancel();
            this.mControlsFrame.setAlpha(1.0F);
            this.mControlsFrame.setVisibility(VISIBLE);
            this.mControlsFrame.animate().alpha(0.0F).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    EasyVideoPlayer.this.setFullscreen(true);
                    if (EasyVideoPlayer.this.mControlsFrame != null) {
                        EasyVideoPlayer.this.mImageViewClose.setVisibility(INVISIBLE);
                        EasyVideoPlayer.this.mControlsFrame.setVisibility(INVISIBLE);
                    }

                }
            }).start();
        }
    }

    @Override
    @CheckResult
    public boolean isControlsShown() {
        return !this.mControlsDisabled && this.mControlsFrame != null && this.mControlsFrame.getAlpha() > 0.5F;
    }

    @Override
    public void toggleControls() {
        if (!this.mControlsDisabled) {
            if (this.isControlsShown()) {
                this.hideControls();
            } else {
                this.showControls();
            }

        }
    }

    @Override
    public void enableControls(boolean andShow) {
        this.mControlsDisabled = false;
        if (andShow) {
            this.showControls();
        }

        this.mClickFrame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyVideoPlayer.this.toggleControls();
            }
        });
        this.mClickFrame.setClickable(true);
    }

    @Override
    public void disableControls() {
        this.mControlsDisabled = true;
        this.mControlsFrame.setVisibility(GONE);
        this.mClickFrame.setOnClickListener((OnClickListener)null);
        this.mClickFrame.setClickable(false);
    }

    @Override
    @CheckResult
    public boolean isPrepared() {
        return this.mPlayer != null && this.mIsPrepared;
    }

    @Override
    @CheckResult
    public boolean isPlaying() {
        return this.mPlayer != null && this.mPlayer.isPlaying();
    }

    @Override
    @CheckResult
    public int getCurrentPosition() {
        return this.mPlayer == null ? -1 : this.mPlayer.getCurrentPosition();
    }

    @Override
    @CheckResult
    public int getDuration() {
        return this.mPlayer == null ? -1 : this.mPlayer.getDuration();
    }

    @Override
    public void start() {
        if (this.mPlayer != null) {
            this.mPlayer.start();
            if (this.mCallback != null) {
                this.mCallback.onStarted(this);
            }

            if (this.mHandler == null) {
                this.mHandler = new Handler();
            }

            this.mHandler.post(this.mUpdateCounters);
            this.mBtnPlayPause.setImageResource(R.drawable.rc_ic_sight_pause);
            this.mImageViewPlay.setVisibility(GONE);
        }
    }

    @Override
    public void seekTo(@IntRange(from = 0L,to = 2147483647L) int pos) {
        if (this.mPlayer != null) {
            this.mPlayer.seekTo(pos);
        }
    }

    @Override
    public void setVolume(@FloatRange(from = 0.0D,to = 1.0D) float leftVolume, @FloatRange(from = 0.0D,to = 1.0D) float rightVolume) {
        if (this.mPlayer != null && this.mIsPrepared) {
            this.mPlayer.setVolume(leftVolume, rightVolume);
        } else {
            throw new IllegalStateException("You cannot use setVolume(float, float) until the player is prepared.");
        }
    }

    @Override
    public void pause() {
        if (this.mPlayer != null && this.isPlaying()) {
            this.mPlayer.pause();
            this.mCallback.onPaused(this);
            if (this.mHandler != null) {
                this.mHandler.removeCallbacks(this.mUpdateCounters);
                this.mBtnPlayPause.setImageResource(R.drawable.rc_ic_sight_play);
                this.mImageViewPlay.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void stop() {
        if (this.mPlayer != null) {
            try {
                this.mPlayer.stop();
            } catch (Throwable var2) {
                ;
            }

            if (this.mHandler != null) {
                this.mHandler.removeCallbacks(this.mUpdateCounters);
                this.mBtnPlayPause.setImageResource(R.drawable.rc_ic_sight_pause);
            }
        }
    }

    @Override
    public void reset() {
        if (this.mPlayer != null) {
            this.mPlayer.reset();
            this.mIsPrepared = false;
        }
    }

    @Override
    public void release() {
        this.mIsPrepared = false;
        if (this.mPlayer != null) {
            try {
                this.mPlayer.release();
            } catch (Throwable var2) {
                ;
            }

            this.mPlayer = null;
        }

        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mUpdateCounters);
            this.mHandler = null;
        }

        Log.d(TAG,  "Released player and Handler");
    }

    @Override
    public void setAutoFullscreen(boolean autoFullscreen) {
        this.mAutoFullscreen = autoFullscreen;
    }

    @Override
    public void setLoop(boolean loop) {
        this.mLoop = loop;
        if (this.mPlayer != null) {
            this.mPlayer.setLooping(loop);
        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Log.d(TAG,   "Surface texture available: " + width + " " + height);
        this.mSurfaceAvailable = true;
        this.mSurface = new Surface(surfaceTexture);
        if (this.mIsPrepared) {
            this.mPlayer.setSurface(this.mSurface);
        } else {
            this.prepare();
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        Log.d(TAG,   "Surface texture changed: " + width + " " + height);
        this.adjustAspectRatio(width, height, this.mPlayer.getVideoWidth(), this.mPlayer.getVideoHeight());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d(TAG,    "Surface texture destroyed");
        this.mSurfaceAvailable = false;
        this.mSurface = null;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG,    "onPrepared");
        this.mIsPrepared = true;
        if (this.mCallback != null) {
            this.mCallback.onPrepared(this);
        }

        this.mCurrent.setText(Util.getDurationString(0L));
        this.mTime.setText(Util.getDurationString((long)mediaPlayer.getDuration()));
        this.mSeeker.setProgress(0);
        this.mSeeker.setMax(mediaPlayer.getDuration());
        this.setControlsEnabled(true);
        if (this.mAutoPlay) {
            if (!this.mControlsDisabled && this.mHideControlsOnPlay) {
                this.hideControls();
            }

            this.start();
            if (this.mInitialPosition > 0) {
                this.seekTo(this.mInitialPosition);
                this.mInitialPosition = -1;
            }
        } else {
            this.mPlayer.start();
            this.mPlayer.pause();
        }

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        Log.d(TAG,     "Buffering: " + percent + "%");
        if (this.mCallback != null) {
            this.mCallback.onBuffering(percent);
        }

        if (this.mSeeker != null) {
            if (percent == 100) {
                this.mSeeker.setSecondaryProgress(0);
            } else {
                this.mSeeker.setSecondaryProgress(this.mSeeker.getMax() * (percent / 100));
            }
        }

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG,     "onCompletion");
        if (this.mLoop) {
            if (this.mHandler != null) {
                this.mHandler.removeCallbacks(this.mUpdateCounters);
            }

            this.mSeeker.setProgress(this.mSeeker.getMax());
            this.showControls();
        } else {
            this.seekTo(0);
            this.mSeeker.setProgress(0);
            this.mCurrent.setText(Util.getDurationString(0L));
            this.mBtnPlayPause.setImageResource(R.drawable.rc_ic_sight_play);
            this.mImageViewPlay.setVisibility(VISIBLE);
        }

        if (this.mCallback != null) {
            this.mCallback.onCompletion(this);
            if (this.mLoop) {
                this.mCallback.onStarted(this);
            }
        }

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        Log.d(TAG,     "Video size changed: " + width + " " + height);
        this.setFitToFillAspectRatio(mediaPlayer, width, height);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        if (what == -38) {
            return false;
        } else {
            String errorMsg = "Preparation/playback error (" + what + "): ";
            switch(what) {
                case -1010:
                    errorMsg = errorMsg + "Unsupported";
                    break;
                case -1007:
                    errorMsg = errorMsg + "Malformed";
                    break;
                case -1004:
                    errorMsg = errorMsg + "I/O error";
                    break;
                case -110:
                    errorMsg = errorMsg + "Timed out";
                    break;
                case 100:
                    errorMsg = errorMsg + "Server died";
                    break;
                case 200:
                    errorMsg = errorMsg + "Not valid for progressive playback";
                    break;
                default:
                    errorMsg = errorMsg + "Unknown error";
            }

            this.throwError(new Exception(errorMsg));
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnPlayPause) {
            if (this.mPlayer.isPlaying()) {
                this.pause();
            } else {
                if (this.mHideControlsOnPlay && !this.mControlsDisabled) {
                    this.hideControls();
                }

                this.start();
            }
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            this.seekTo(value);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mWasPlaying = this.isPlaying();
        if (this.mWasPlaying) {
            this.mPlayer.pause();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (this.mWasPlaying) {
            this.mPlayer.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG,     "Detached from window");
        this.release();
        this.mSeeker = null;
        this.mCurrent = null;
        this.mTime = null;
        this.mBtnPlayPause = null;
        this.mControlsFrame = null;
        this.mClickFrame = null;
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mUpdateCounters);
            this.mHandler = null;
        }

    }

    private void setFitToFillAspectRatio(MediaPlayer mp, int videoWidth, int videoHeight) {
        if (mp != null && this.mTextureView != null) {
            Integer screenWidth = ((Activity)this.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            Integer screenHeight = ((Activity)this.getContext()).getWindowManager().getDefaultDisplay().getHeight();
            android.view.ViewGroup.LayoutParams videoParams = this.mTextureView.getLayoutParams();
            int screenWidthFillHeight;
            int screenFitHeight;
            if (videoWidth > videoHeight) {
                screenWidthFillHeight = screenWidth * videoHeight / videoWidth;
                screenFitHeight = Math.min(screenHeight, screenWidthFillHeight);
                videoParams.width = screenWidth * screenFitHeight / screenWidthFillHeight;
                videoParams.height = screenFitHeight;
            } else {
                screenWidthFillHeight = screenHeight * videoWidth / videoHeight;
                screenFitHeight = Math.min(screenWidth, screenWidthFillHeight);
                videoParams.width = screenFitHeight;
                videoParams.height = screenHeight * screenFitHeight / screenWidthFillHeight;
            }

            this.mTextureView.setLayoutParams(videoParams);
        }

    }

    private void adjustAspectRatio(int viewWidth, int viewHeight, int videoWidth, int videoHeight) {
        if (viewWidth >= viewHeight) {
            double aspectRatio = (double)videoHeight / (double)videoWidth;
            int newWidth;
            int newHeight;
            if (viewHeight > (int)((double)viewWidth * aspectRatio)) {
                newWidth = viewWidth;
                newHeight = (int)((double)viewWidth * aspectRatio);
            } else {
                newWidth = (int)((double)viewHeight / aspectRatio);
                newHeight = viewHeight;
            }

            int xoff = (viewWidth - newWidth) / 2;
            int yoff = (viewHeight - newHeight) / 2;
            Matrix txform = new Matrix();
            this.mTextureView.getTransform(txform);
            txform.setScale((float)newWidth / (float)viewWidth, (float)newHeight / (float)viewHeight);
            txform.postTranslate((float)xoff, (float)yoff);
            this.mTextureView.setTransform(txform);
        }
    }

    private void throwError(Exception e) {
        if (this.mCallback != null) {
            this.mCallback.onError(this, e);
        } else {
            throw new RuntimeException(e);
        }
    }

    private static void setTint(@NonNull SeekBar seekBar, @ColorInt int color) {
        ColorStateList s1 = ColorStateList.valueOf(color);
        if (Build.VERSION.SDK_INT >= 21) {
            seekBar.setThumbTintList(s1);
            seekBar.setProgressTintList(s1);
            seekBar.setSecondaryProgressTintList(s1);
        } else if (Build.VERSION.SDK_INT > 10) {
            Drawable progressDrawable = DrawableCompat.wrap(seekBar.getProgressDrawable());
            seekBar.setProgressDrawable(progressDrawable);
            DrawableCompat.setTintList(progressDrawable, s1);
            if (Build.VERSION.SDK_INT >= 16) {
                Drawable thumbDrawable = DrawableCompat.wrap(seekBar.getThumb());
                DrawableCompat.setTintList(thumbDrawable, s1);
                seekBar.setThumb(thumbDrawable);
            }
        } else {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
            if (Build.VERSION.SDK_INT <= 10) {
                mode = PorterDuff.Mode.MULTIPLY;
            }

            if (seekBar.getIndeterminateDrawable() != null) {
                seekBar.getIndeterminateDrawable().setColorFilter(color, mode);
            }

            if (seekBar.getProgressDrawable() != null) {
                seekBar.getProgressDrawable().setColorFilter(color, mode);
            }
        }

    }

    private void invalidateThemeColors() {
        int labelColor = Util.isColorDark(this.mThemeColor) ? -1 : -16777216;
        this.mControlsFrame.setBackgroundColor(Util.adjustAlpha(this.mThemeColor, 0.8F));
        this.mTime.setTextColor(labelColor);
        this.mTime.setTextColor(labelColor);
        setTint(this.mSeeker, labelColor);
    }

    @TargetApi(14)
    private void setFullscreen(boolean fullscreen) {
        if (this.mAutoFullscreen) {
            int flags = !fullscreen ? 0 : 1;
            if (Build.VERSION.SDK_INT >= 19) {
                flags |= 1792;
                if (fullscreen) {
                    flags |= 2054;
                }
            }

            this.mClickFrame.setSystemUiVisibility(flags);
        }

    }

    public ImageView getImageViewSightList() {
        return this.mImageViewSightList;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == 3 && this.onInfoCallBack != null) {
            this.onInfoCallBack.onInfo();
        }

        return false;
    }

    public void setOnInfoListener(EasyVideoPlayer.OnInfoCallBack callBack) {
        this.onInfoCallBack = callBack;
    }

    public interface OnInfoCallBack {
        void onInfo();
    }
}
