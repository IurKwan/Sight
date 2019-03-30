package com.example.rui.sight2.sight.util;

import android.util.Log;

import com.example.rui.sight2.sight.CameraSize;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CameraParamUtil {

    private static final String TAG = "Sight-CameraView";
    private CameraParamUtil.CameraSizeComparator sizeComparator = new CameraParamUtil.CameraSizeComparator();
    private static CameraParamUtil cameraParamUtil = null;

    private CameraParamUtil() {
    }

    public static CameraParamUtil getInstance() {
        if (cameraParamUtil == null) {
            cameraParamUtil = new CameraParamUtil();
            return cameraParamUtil;
        } else {
            return cameraParamUtil;
        }
    }

    public CameraSize getPreviewSize(List<CameraSize> list, int th, float rate, CameraSize preferSize) {
        if (list == null) {
            return null;
        } else {
            CameraSize prefer = this.getPreferSize(list, preferSize);
            if (prefer != null) {
                return prefer;
            } else {
                Collections.sort(list, this.sizeComparator);
                int i = 0;

                for(Iterator var7 = list.iterator(); var7.hasNext(); ++i) {
                    CameraSize s = (CameraSize)var7.next();
                    if (s.width > th && this.equalRate(s, rate)) {
                        Log.i("Sight-CameraView", "MakeSure Preview :w = " + s.width + " h = " + s.height);
                        break;
                    }
                }

                return i == list.size() ? this.getBestSize(list, rate) : (CameraSize)list.get(i);
            }
        }
    }

    public CameraSize getVideoSize(List<CameraSize> list, int th, float rate, CameraSize preferSize) {
        if (list == null) {
            return null;
        } else {
            CameraSize prefer = this.getPreferSize(list, preferSize);
            return prefer != null ? prefer : this.getPictureSize(list, th, rate);
        }
    }

    public CameraSize getPictureSize(List<CameraSize> list, int th, float rate) {
        if (list == null) {
            return null;
        } else {
            Collections.sort(list, this.sizeComparator);
            int i = 0;

            for(Iterator var5 = list.iterator(); var5.hasNext(); ++i) {
                CameraSize s = (CameraSize)var5.next();
                if (s.width > th && this.equalRate(s, rate)) {
                    Log.i("Sight-CameraView", "MakeSure Picture :w = " + s.width + " h = " + s.height);
                    break;
                }
            }

            return i == list.size() ? this.getBestSize(list, rate) : (CameraSize)list.get(i);
        }
    }

    public CameraSize getBestSize(List<CameraSize> list, float rate) {
        float previewDisparity = 100.0F;
        int index = 0;

        for(int i = 0; i < list.size(); ++i) {
            CameraSize cur = (CameraSize)list.get(i);
            float prop = (float)cur.width / (float)cur.height;
            if (Math.abs(rate - prop) < previewDisparity) {
                previewDisparity = Math.abs(rate - prop);
                index = i;
            }
        }

        return (CameraSize)list.get(index);
    }

    public boolean equalRate(CameraSize s, float rate) {
        float r = (float)s.width / (float)s.height;
        return (double)Math.abs(r - rate) <= 0.2D;
    }

    public boolean isSupportedFocusMode(List<String> focusList, String focusMode) {
        for(int i = 0; i < focusList.size(); ++i) {
            if (focusMode.equals(focusList.get(i))) {
                Log.i("Sight-CameraView", "FocusMode supported " + focusMode);
                return true;
            }
        }

        Log.i("Sight-CameraView", "FocusMode not supported " + focusMode);
        return false;
    }

    public boolean isSupportedPictureFormats(List<Integer> supportedPictureFormats, int jpeg) {
        for(int i = 0; i < supportedPictureFormats.size(); ++i) {
            if (jpeg == (Integer)supportedPictureFormats.get(i)) {
                Log.i("Sight-CameraView", "Formats supported " + jpeg);
                return true;
            }
        }

        Log.i("Sight-CameraView", "Formats not supported " + jpeg);
        return false;
    }

    public CameraSize getPreferSize(List<CameraSize> list, CameraSize preferSize) {
        Iterator var3 = list.iterator();

        CameraSize size;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            size = (CameraSize)var3.next();
        } while(!size.equals(preferSize));

        return preferSize;
    }

    public class CameraSizeComparator implements Comparator<CameraSize> {
        public CameraSizeComparator() {
        }

        @Override
        public int compare(CameraSize lhs, CameraSize rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else {
                return lhs.width > rhs.width ? 1 : -1;
            }
        }
    }
}
