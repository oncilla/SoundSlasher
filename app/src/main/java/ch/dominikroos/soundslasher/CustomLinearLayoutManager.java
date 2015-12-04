package ch.dominikroos.soundslasher;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by roosd on 04.12.15.
 */
public class CustomLinearLayoutManager extends LinearLayoutManager {


    private static final String TAG = "CustomLLM";

    public CustomLinearLayoutManager(Context context) {
        super(context, VERTICAL, false);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        Log.d(TAG, "onLayoutChildren");
    }
}
