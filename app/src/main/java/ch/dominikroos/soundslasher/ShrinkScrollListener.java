package ch.dominikroos.soundslasher;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by roosd on 04.11.15.
 */
abstract class ShrinkScrollListener extends RecyclerView.OnScrollListener {

    private static final String TAG = "SHRINK_SCROLL_LISTENER";
    private int mRecyclerviewOffset = 0;
    private int mRecyclerviewAbsoluteOffset = 0;
    private int mCircledPickerHeight;
    private int mCircledPickerWidth;
    protected boolean isActive = false;
    int[] lastValues = new int[2];
    private int mOffsetAccumulator = 0;


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        mRecyclerviewOffset += dy;
        mRecyclerviewAbsoluteOffset += dy;
        clipOffset();
        onMoved(computeHeight(), mRecyclerviewAbsoluteOffset);
        /*if(lastValuesHaveSameSign(dy)) {
            mOffsetAccumulator += dy;
            mRecyclerviewOffset += mOffsetAccumulator;
            mOffsetAccumulator = 0;
        }
        addLastValue(dy);*/
    }

    private void addLastValue(int dy) {
        for(int i = 1; i <lastValues.length; i++){
            lastValues[i-1] = lastValues[i];
        }
        lastValues[lastValues.length-1] = dy;
    }


    private boolean lastValuesHaveSameSign(int dy) {
        for(int i = 0; i < lastValues.length; i++){
            if(dy < 0 && lastValues[i] > 0 || dy > 0 && lastValues[i] < 0){
                return false;
            }
        }
        return true;
    }

    protected abstract void onMoved(int height, int offset);

    private void clipOffset() {
        if(mRecyclerviewOffset < 0){
            mRecyclerviewOffset = 0;
        }else if(mRecyclerviewOffset > mCircledPickerHeight>>1){
            mRecyclerviewOffset = mCircledPickerHeight>>1;
        }
    }

    public int computeHeight() {
        float height = (mCircledPickerHeight*(1f - (mRecyclerviewOffset / (float) mCircledPickerHeight)));

        return height < mCircledPickerHeight ? (int)height : mCircledPickerHeight;
    }

    public void setmCircledPickerHeight(int mCircledPickerHeight) {
        this.mCircledPickerHeight = mCircledPickerHeight;
    }

    public void setmCircledPickerWidth(int mCircledPickerWidth) {
        this.mCircledPickerWidth = mCircledPickerWidth;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
