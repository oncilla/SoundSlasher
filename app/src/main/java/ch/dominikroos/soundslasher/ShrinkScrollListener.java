package ch.dominikroos.soundslasher;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by roosd on 04.11.15.
 */
abstract class ShrinkScrollListener extends RecyclerView.OnScrollListener {

    private static final String TAG = "SHRINK_SCROLL_LISTENER";
    private int mRecyclerViewOffset = 0;
    private int mRecyclerViewCardAbsolutOffset = 0;
    private int mCircledPickerHeight;
    private int mCircledPickerWidth;
    protected boolean isActive = false;
    int[] lastValues = new int[2];
    private int mOffsetAccumulator = 0;


    @Override
    public void onScrolled(RecyclerView RecyclerView, int dx, int dy) {
        super.onScrolled(RecyclerView, dx, dy);

        mRecyclerViewOffset += dy;
        mRecyclerViewCardAbsolutOffset += dy;
        clipOffset();
        Log.i(TAG,mCircledPickerHeight + " " + mRecyclerViewOffset + " " + mRecyclerViewCardAbsolutOffset);
       // onMoved(computeHeight(), mRecyclerViewCardAbsolutOffset - mRecyclerViewOffset);
        onMoved(computeHeight());
        /*if(lastValuesHaveSameSign(dy)) {
            mOffsetAccumulator += dy;
            mRecyclerViewOffset += mOffsetAccumulator;
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

   // protected abstract void onMoved(int height, int offset);
    protected abstract void onMoved(int height);


    private boolean clipOffset() {
        if(mRecyclerViewOffset < 0){
            mRecyclerViewOffset = 0;
            return true;
        }
        if(mRecyclerViewOffset > mCircledPickerHeight>>1){
            mRecyclerViewOffset = mCircledPickerHeight>>1;
            return true;
        }
        return false;
    }

    public int computeHeight() {
        float height = (mCircledPickerHeight*(1f - (mRecyclerViewOffset / (float) mCircledPickerHeight)));

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

    public void resetOffset() {
        mRecyclerViewOffset = 0;
        mRecyclerViewCardAbsolutOffset = 0;
    }
}
