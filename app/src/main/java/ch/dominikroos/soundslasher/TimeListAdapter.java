package ch.dominikroos.soundslasher;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by roosd on 01.11.15.
 */
public class TimeListAdapter extends RecyclerView.Adapter<TimeListAdapter.ViewHolder> {
    private static final int VIEW_TYPE_CIRCLED_PICKER = 0;
    private static final int VIEW_TYPE_TIME_VIEW = 1;
    private ArrayList<Float> mDataset;
    private MainActivity mMainActivity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CircledPicker mCircledPicker;
        public TextView mTextView;
        public CardView mCardView;

        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            mTextView = (TextView)mCardView.findViewById(R.id.time_text_view);
        }
        public ViewHolder(CardView v, final MainActivity mainActivity) {
            super(v);
            mCardView = v;
            mCircledPicker = (CircledPicker)mCardView.findViewById(R.id.circled_picker);
            v.findViewById(R.id.start_slash_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainActivity.setAlarm();

                }
            });
            v.findViewById(R.id.cancle_slash_button).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mainActivity.cancelAlarm();
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TimeListAdapter(ArrayList<Float> myDataset, MainActivity mainActivity) {
        mDataset = myDataset;
        mMainActivity = mainActivity;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == -1){
            return VIEW_TYPE_CIRCLED_PICKER;
        }else{
            return VIEW_TYPE_TIME_VIEW;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TimeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {

        if(viewType == VIEW_TYPE_CIRCLED_PICKER){
            View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.circled_picker_card, parent, false); //new CircledPicker(parent.getContext());
            ViewHolder vh = new ViewHolder((CardView)v,mMainActivity);
            return vh;
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_card, parent, false);
            ViewHolder vh = new ViewHolder((CardView)v);
            return vh;
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(getItemViewType(position) == VIEW_TYPE_CIRCLED_PICKER){
            holder.mCircledPicker.setValue((float)mDataset.get(position));
            holder.mCircledPicker.invalidate();
            mMainActivity.setCircledPicker(holder.mCircledPicker);
        }else if(getItemViewType(position)==VIEW_TYPE_TIME_VIEW){
            holder.mTextView.setText(CircledPickerUtils.getMinuesAndSecondsString(mDataset.get(position)));
            holder.mTextView.invalidate();
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
