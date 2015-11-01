package ch.dominikroos.soundslasher;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by roosd on 01.11.15.
 */
public class TimeListAdapter extends RecyclerView.Adapter<TimeListAdapter.ViewHolder> {
    private ArrayList<Float> mDataset;
    private MainActivity mMainActivity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CircledPicker mCircledPicker;
        public ViewHolder(CircledPicker v) {
            super(v);
            mCircledPicker = v;
        }
        public CardView mCardView;
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

    // Create new views (invoked by the layout manager)
    @Override
    public TimeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.circled_picker_card, parent, false); //new CircledPicker(parent.getContext());
        ViewHolder vh = new ViewHolder((CardView)v,mMainActivity);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mTextView.setText("hello");
        holder.mCircledPicker.setValue(mDataset.get(0));
        holder.mCircledPicker.invalidate();
        mMainActivity.setCircledPicker(holder.mCircledPicker);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
