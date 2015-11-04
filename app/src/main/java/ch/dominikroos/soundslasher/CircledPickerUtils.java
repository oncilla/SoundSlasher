package ch.dominikroos.soundslasher;

/**
 * Created by Dominik on 09.06.2015.
 */

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class CircledPickerUtils {
    public static float convertDpToPixel(Context context, float densityPixel) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return densityPixel * (metrics.densityDpi / 160f);
    }

    private static String addAZeroIfNeeds(int value) {
        return value > 9 ? value + "" : "0" + value;
    }

    private static String getMinutesString(float value) {
        int hour = (int) (value / 60);
        int minutes = (int) Math.ceil(value - (hour * 60));
        return addAZeroIfNeeds(minutes==60?0:minutes);
    }

    private static String getHoursString(float value) {
        int hour = (int) (value / 60);
        return addAZeroIfNeeds(hour);
    }

    public static String getMinuesAndSecondsString(float value) {
        return CircledPickerUtils.getHoursString(value) + ":" + CircledPickerUtils.getMinutesString(value);
    }

    public static String getHourAndMinutesString(float value) {
        return CircledPickerUtils.getHoursString(value) + "h " + CircledPickerUtils.getMinutesString(value) + "m";
    }
}
