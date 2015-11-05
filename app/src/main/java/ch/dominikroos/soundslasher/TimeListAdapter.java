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
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_TIME_VIEW = 1;
    private ArrayList<MainActivity.DataPair> mDataset;
    private MainActivity mMainActivity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public CardView mCardView;

        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            mTextView = (TextView)mCardView.findViewById(R.id.time_text_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TimeListAdapter(ArrayList<MainActivity.DataPair> myDataset, MainActivity mainActivity) {
        mDataset = myDataset;
        mMainActivity = mainActivity;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return VIEW_TYPE_HEADER;
        }else{
            return VIEW_TYPE_TIME_VIEW;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TimeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {

        if(viewType == VIEW_TYPE_HEADER){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_card, parent, false);
            ViewHolder vh = new ViewHolder((CardView)v);
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
        if(getItemViewType(position) == VIEW_TYPE_HEADER){
            holder.mTextView.setText(CircledPickerUtils.getMinuesAndSecondsString(mDataset.get(position).mTime));
            holder.mTextView.invalidate();
        }else if(getItemViewType(position)==VIEW_TYPE_TIME_VIEW){
            holder.mTextView.setText(CircledPickerUtils.getMinuesAndSecondsString(mDataset.get(position).mTime));
            holder.mTextView.invalidate();
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
