package com.iur.sight.fragment;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iur.rui.sight2.R;
import com.iur.sight.callback.EasyVideoCallback;
import com.iur.sight.player.EasyVideoPlayer;

/**
 * @author 关志锐
 */
public class PlaybackVideoFragment extends Fragment implements EasyVideoCallback {
    private EasyVideoPlayer mPlayer;
    private static String targetId;
    private static boolean isFromSightList;
    static boolean fromSightListImageVisible = true;

    public PlaybackVideoFragment() {
    }

    public static PlaybackVideoFragment newInstance(String outputUri, String id,  boolean fromSightList, boolean sightListImageVisible) {
        PlaybackVideoFragment fragment = new PlaybackVideoFragment();
        fragment.setRetainInstance(true);
        Bundle args = new Bundle();
        args.putString("output_uri", outputUri);
        targetId = id;
        isFromSightList = fromSightList;
        fromSightListImageVisible = sightListImageVisible;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.getActivity() != null) {
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mPlayer != null) {
            this.mPlayer.pause();
        }

    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sight_palyer_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mPlayer = view.findViewById(R.id.playbackView);
        this.mPlayer.setCallback(this);
        this.mPlayer.setSource(Uri.parse(this.getArguments().getString("output_uri")));
        if (!fromSightListImageVisible) {
            this.mPlayer.setFromSightListImageInVisible();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (this.mPlayer != null) {
            this.mPlayer.release();
            this.mPlayer = null;
        }

    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
    }

    @Override
    public void onBuffering(int percent) {
    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
    }

    @Override
    public void onClose() {
        if (this.getActivity() != null) {
            this.getActivity().finish();
        }

    }
}
