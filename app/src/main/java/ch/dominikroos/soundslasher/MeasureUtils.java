package ch.dominikroos.soundslasher;

/**
 * Created by Dominik on 09.06.2015.
 */

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class MeasureUtils {
    public static float convertDpToPixel(Context context, float densityPixel) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return densityPixel * (metrics.densityDpi / 160f);
    }
}
