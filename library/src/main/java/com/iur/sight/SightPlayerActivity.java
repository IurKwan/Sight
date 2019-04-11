package com.iur.sight;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.iur.rui.sight2.R;
import com.iur.sight.downloader.HttpManager;
import com.iur.sight.fragment.PlaybackVideoFragment;
import com.iur.sight.util.FileUtils;
import com.iur.sight.view.CircleProgressView;

import java.io.File;

/**
 * @author guanzhirui
 */
public class SightPlayerActivity extends AppCompatActivity {

    private int mProgress;
    private ImageView mThumbImageView;
    private FrameLayout mContainer;
    private RelativeLayout rlSightDownload;
    private CircleProgressView mSightDownloadProgress;
    private RelativeLayout mSightDownloadFailedReminder;

    private String mUrl;
    private String mSavePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_sight_player);

        mProgress = 0;
        mContainer = findViewById(R.id.container);
        rlSightDownload = findViewById(R.id.rl_sight_download);

        mUrl = getIntent().getStringExtra("url");
        mSavePath = getIntent().getStringExtra("path");

        if (isSightDownloaded(mUrl)){
            initSightPlayer();
        }else {
            initDownloadView();
            if (mProgress == 0){
                downloadSight();
            }
        }
    }

    private void initDownloadView() {
        rlSightDownload.setVisibility(View.VISIBLE);
        mThumbImageView = findViewById(R.id.rc_sight_thumb);

        if (Sight.mInterface != null){
            Sight.mInterface.displayImage(this,mUrl,mThumbImageView);
        }

        mSightDownloadProgress = findViewById(R.id.rc_sight_download_progress);
        mSightDownloadProgress.setVisibility(View.VISIBLE);
        mSightDownloadProgress.setProgress(mProgress, true);
        findViewById(R.id.rc_sight_download_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void downloadSight() {
        Sight.mHttpManager.download(mUrl, mSavePath, new HttpManager.DownloadCallback() {
            @Override
            public void onProgress(final float progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgress = (int) progress;
                        mSightDownloadProgress.setVisibility(View.VISIBLE);
                        mSightDownloadProgress.setProgress(mProgress,true);
                    }
                });
            }

            @Override
            public void onError(String error) {
                mSightDownloadProgress.setVisibility(View.GONE);
                initDownloadFailedReminder();
            }

            @Override
            public void onResponse(File file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rlSightDownload.setVisibility(View.GONE);
                        mThumbImageView.setVisibility(View.GONE);
                        mSightDownloadProgress.setVisibility(View.GONE);
                        initSightPlayer();
                    }
                });
            }

            @Override
            public void onBefore() {

            }
        });
    }

    private void initDownloadFailedReminder() {
        this.mSightDownloadFailedReminder = this.findViewById(R.id.rc_sight_download_failed_reminder);
        this.mSightDownloadFailedReminder.setVisibility(View.VISIBLE);
        this.mSightDownloadFailedReminder.findViewById(R.id.rc_sight_download_failed_iv_reminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSightDownloadFailedReminder.setVisibility(View.GONE);
                mProgress = 0;
                downloadSight();
            }
        });
    }

    private void initSightPlayer() {
        if (!isFinishing()) {
            mContainer.setVisibility(View.VISIBLE);

            String name = mUrl;
            if (FileUtils.createOrExistsDir(mSavePath)) {
                //一定是找最后一个'/'出现的位置
                int i = name.lastIndexOf('/');
                if (i != -1) {
                    name = name.substring(i);
                }
            }
            String mVideoPath = mSavePath + name;
            PlaybackVideoFragment frag = PlaybackVideoFragment.newInstance(mVideoPath, "",  false, false);
            getSupportFragmentManager().beginTransaction().replace(R.id.container,frag).commitAllowingStateLoss();
        }
    }

    private boolean isSightDownloaded(String url) {
        String name = url;
        if (FileUtils.createOrExistsDir(mSavePath)) {
            //一定是找最后一个'/'出现的位置
            int i = name.lastIndexOf('/');
            if (i != -1) {
                name = name.substring(i);
            }
        }
        String mVideoPath = mSavePath + name;
        File file = new File(mVideoPath);
        return file.exists();
    }
}
















