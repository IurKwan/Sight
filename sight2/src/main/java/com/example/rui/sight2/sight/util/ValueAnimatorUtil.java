package com.example.rui.sight2.sight.util;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;

public class ValueAnimatorUtil {

     public ValueAnimatorUtil() {
    }

    public static void resetDurationScaleIfDisable() {
        if (getDurationScale() == 0.0F) {
            resetDurationScale();
        }

    }

    public static void resetDurationScale() {
        try {
            getField().setFloat((Object)null, 1.0F);
        } catch (Exception var1) {
            var1.printStackTrace();
        }

    }

    private static float getDurationScale() {
        try {
            return getField().getFloat((Object)null);
        } catch (Exception var1) {
            var1.printStackTrace();
            return -1.0F;
        }
    }

    @NonNull
    private static Field getField() throws NoSuchFieldException {
        Field field = ValueAnimator.class.getDeclaredField("sDurationScale");
        field.setAccessible(true);
        return field;
    }

}
