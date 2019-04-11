package com.iur.sight.callback;

import com.iur.sight.player.EasyVideoPlayer;

/**
 * @author 关志锐
 */
public interface EasyVideoCallback {

    void onStarted(EasyVideoPlayer var1);

    void onPaused(EasyVideoPlayer var1);

    void onPreparing(EasyVideoPlayer var1);

    void onPrepared(EasyVideoPlayer var1);

    void onBuffering(int var1);

    void onError(EasyVideoPlayer var1, Exception var2);

    void onCompletion(EasyVideoPlayer var1);

    void onClose();

}
