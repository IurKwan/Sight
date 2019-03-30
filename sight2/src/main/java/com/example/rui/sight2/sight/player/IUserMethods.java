package com.example.rui.sight2.sight.player;

import android.net.Uri;
import android.support.annotation.CheckResult;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.example.rui.sight2.sight.callback.EasyVideoCallback;

public interface IUserMethods {

    void setSource(@NonNull Uri var1);

    void setCallback(@NonNull EasyVideoCallback var1);

    void setProgressCallback(@NonNull EasyVideoProgressCallback var1);

    void setHideControlsOnPlay(boolean var1);

    void setAutoPlay(boolean var1);

    void setInitialPosition(@IntRange(from = 0L, to = 2147483647L) int var1);

    void showControls();

    void hideControls();

    @CheckResult
    boolean isControlsShown();

    void toggleControls();

    void enableControls(boolean var1);

    void disableControls();

    @CheckResult
    boolean isPrepared();

    @CheckResult
    boolean isPlaying();

    @CheckResult
    int getCurrentPosition();

    @CheckResult
    int getDuration();

    void start();

    void seekTo(@IntRange(from = 0L, to = 2147483647L) int var1);

    void setVolume(@FloatRange(from = 0.0D, to = 1.0D) float var1, @FloatRange(from = 0.0D, to = 1.0D) float var2);

    void pause();

    void stop();

    void reset();

    void release();

    void setAutoFullscreen(boolean var1);

    void setLoop(boolean var1);
}

