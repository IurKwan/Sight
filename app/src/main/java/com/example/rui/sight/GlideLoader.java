package com.example.rui.sight;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.rui.sight2.sight.imageloader.ImageLoaderInterface;

/**
 * @author 关志锐
 */
public class GlideLoader implements ImageLoaderInterface {

    @Override
    public void displayImage(Context context, String path, ImageView imageView) {
        Glide.with(context)
                .load(path)
                .into(imageView);
    }

}
