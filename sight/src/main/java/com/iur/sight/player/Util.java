package com.iur.sight.player;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Util {
    Util() {
    }

    public static String getDurationString(long durationMs) {
        return String.format(Locale.getDefault(), "%s%02d:%02d", "", TimeUnit.MILLISECONDS.toMinutes(durationMs), TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMs)));
    }

    public static boolean isColorDark(int color) {
        double darkness = 1.0D - (0.299D * (double)Color.red(color) + 0.587D * (double)Color.green(color) + 0.114D * (double)Color.blue(color)) / 255.0D;
        return darkness >= 0.5D;
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round((float) Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int resolveColor(Context context, @AttrRes int attr) {
        return resolveColor(context, attr, 0);
    }

    public static int resolveColor(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});

        int var4;
        try {
            var4 = a.getColor(0, fallback);
        } finally {
            a.recycle();
        }

        return var4;
    }

    public static Drawable resolveDrawable(Context context, @AttrRes int attr) {
        return resolveDrawable(context, attr, (Drawable)null);
    }

    private static Drawable resolveDrawable(Context context, @AttrRes int attr, Drawable fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});

        Drawable var5;
        try {
            Drawable d = a.getDrawable(0);
            if (d == null && fallback != null) {
                d = fallback;
            }

            var5 = d;
        } finally {
            a.recycle();
        }

        return var5;
    }
}
