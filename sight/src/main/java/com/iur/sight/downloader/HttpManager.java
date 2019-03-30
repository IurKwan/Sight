package com.iur.sight.downloader;

import java.io.File;

/**
 * @author 关志锐
 */
public interface HttpManager {

    void download(String url,String path,DownloadCallback callback);

    interface DownloadCallback{

        void onProgress(float progress);

        void onError(String error);

        void onResponse(File file);

        void onBefore();

    }

}
