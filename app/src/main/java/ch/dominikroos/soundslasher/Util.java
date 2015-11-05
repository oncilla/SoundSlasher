package ch.dominikroos.soundslasher;

import android.content.Context;

/**
 * Created by roosd on 05.11.15.
 */
public class Util {

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}
