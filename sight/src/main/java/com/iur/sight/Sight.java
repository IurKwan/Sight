package com.iur.sight;

import android.app.Activity;
import android.content.Intent;

import com.iur.sight.downloader.HttpManager;
import com.iur.sight.imageloader.ImageLoaderInterface;

public class Sight {

    private String url;
    private String path;

    static ImageLoaderInterface mInterface;
    static HttpManager mHttpManager;

    public Sight(){

    }

    public Sight url(String url){
        this.url = url;
        return this;
    }

    public Sight path(String path){
        this.path = path;
        return this;
    }

    public Sight start(Activity activity){
        Intent intent = new Intent(activity,SightPlayerActivity.class);
        intent.putExtra("url",url);
        intent.putExtra("path",path);
        activity.startActivity(intent);
        return this;
    }

    public Sight setInterface(ImageLoaderInterface anInterface) {
        mInterface = anInterface;
        return this;
    }

    public Sight setHttpManager(HttpManager httpManager) {
        mHttpManager = httpManager;
        return this;
    }
}
