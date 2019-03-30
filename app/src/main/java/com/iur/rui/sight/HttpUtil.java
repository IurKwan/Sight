package com.iur.rui.sight;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.iur.sight.downloader.HttpManager;
import com.iur.sight.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author 关志锐
 */
public class HttpUtil implements HttpManager {

    private DownloadService mApi;
    private Call<ResponseBody> mCall;
    private File mFile;
    private Thread mThread;
    private String mVideoPath;

    @Override
    public void download(String url, String path, final DownloadCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.baidu.com")
                .build();

        mApi = retrofit.create(DownloadService.class);

        //通过Url得到保存到本地的文件名
        String name = url;
        if (FileUtils.createOrExistsDir(path)) {
            //一定是找最后一个'/'出现的位置
            int i = name.lastIndexOf('/');
            if (i != -1) {
                name = name.substring(i);
                mVideoPath = path + name;
            }
        }
        if (TextUtils.isEmpty(mVideoPath)) {
            return;
        }
        //建立一个文件
        mFile = new File(mVideoPath);
        if (!FileUtils.isFileExists(mFile) && FileUtils.createOrExistsFile(mFile)) {
            if (mApi == null) {
                return;
            }
            mCall = mApi.downloadFile(url);
            mCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                    //下载文件放在子线程
                    mThread = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            //保存到本地
                            writeFile2Disk(response, mFile, callback);
                        }
                    };
                    mThread.start();
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onError("网络错误！");
                }
            });
        } else {
            callback.onError(mVideoPath);
        }
    }

    private void writeFile2Disk(Response<ResponseBody> response, File file, HttpManager.DownloadCallback downloadListener) {
        downloadListener.onBefore();
        long currentLength = 0;
        OutputStream os = null;

        if (response.body() == null) {
            downloadListener.onError("资源错误！");
            return;
        }
        InputStream is = response.body().byteStream();
        long totalLength = response.body().contentLength();

        try {
            os = new FileOutputStream(file);
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
                currentLength += len;
                downloadListener.onProgress((int) (100 * currentLength / totalLength));
                if ((int) (100 * currentLength / totalLength) == 100) {
                    downloadListener.onResponse(new File(mVideoPath));
                }
            }
        } catch (FileNotFoundException e) {
            downloadListener.onError("未找到文件！");
            e.printStackTrace();
        } catch (IOException e) {
            downloadListener.onError("IO错误！");
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
