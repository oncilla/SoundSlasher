package ch.dominikroos.soundslasher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static ch.dominikroos.soundslasher.CircledPicker.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, TimerStopListener {


    private static final int ALARM_REQUEST_CODE = 0;
    private static final long SECONDS_TO_MILLIS = 1000;
    private static final String ALARM_TIME = "ALARM_TIME";
    private static final String ALARM_SET = "ALARM_SET";
    private static final String DEFAULT_VALUE = "DEFAULT_VALUE";
    private static final String TAG = "MAIN_ACTIVITY";
    private static final String DATA_PAIRS = "DATA_PAIRS";
    private static final String OLD_PICKER_VALUE = "OLD_PICKER_VALUE";
    private AlarmManager mAlarmManager;
    private CircledPicker mCircledPicker;
    private CardView mCircledPickerCard;
    private long mAlarmTime;
    private boolean mAlarmSet = false;
    private SharedPreferences mSharedPreferences;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private TimeListAdapter mAdapter;
    private ArrayList<DataPair> mDataset;

    private int mInitialCardSize;
    private ShrinkScrollListener mOnScrollListener;
    private FloatingActionButton mFloatingActionButton;
    private int mCircledPickerCardHeight = -1;
    private float mOldCircularPickerValue = -1;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);

        mCircledPicker = (CircledPicker)findViewById(R.id.circled_picker);
        mCircledPickerCard = (CardView)findViewById(R.id.card_view);
        mInitialCardSize = mCircledPickerCard.getHeight();
        mCircledPicker.setOnTimerStopListener(this);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.content_view_relative_layout);

        mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        mDataset = createDataSetFromSharedPreferences();
        setUpRecyclerView();

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(this);
    }

    private void setUpRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TimeListAdapter(mDataset,this,mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(mOnScrollListener = new ShrinkScrollListener() {
            @Override
            protected void onMoved(int height, int offset) {
                if (isActive) {
                    setCircledPickerHeight(height);
                    float ratio = (offset)/(float)((RelativeLayout.LayoutParams)mCircledPickerCard.getLayoutParams()).leftMargin;
                    ratio = (ratio<0)?0:ratio;
                    mCircledPickerCard.setCardElevation(48 * Math.min(1,ratio)+ 6);
                }
                mRelativeLayout.bringChildToFront(mCircledPickerCard);
                mRelativeLayout.invalidate();

            }

        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                removeElementFromDataset(mRecyclerView.getChildAdapterPosition(((TimeListAdapter.ViewHolder) viewHolder).mCardView));
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }



    @Override
    protected void onResume() {
        super.onResume();

        if(mCircledPickerCardHeight != -1){
            setCircledPickerHeight(mCircledPickerCardHeight);
            mLayoutManager.scrollToPosition(0);
            mOnScrollListener.resetOffset();
        }

        mAlarmTime = mSharedPreferences.getLong(ALARM_TIME, -1);
        mAlarmSet = mSharedPreferences.getBoolean(ALARM_SET, false);
        mOldCircularPickerValue = mSharedPreferences.getFloat(OLD_PICKER_VALUE, -1);
        Log.i(TAG, mOldCircularPickerValue + "");

        if (mAlarmSet && mAlarmTime > System.currentTimeMillis()) {
            float timeToAlarm = (float) (mAlarmTime - System.currentTimeMillis()) / 1000;
            mCircledPicker.setValue(timeToAlarm);
            mCircledPicker.start(mAlarmTime);
            setmAlarmSet(true);
        }else if(mOldCircularPickerValue > -0.5){
            setCircledPickerValue(mOldCircularPickerValue);
            setmAlarmSet(false);
        }else{
            setmAlarmSet(false);
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "onWindowFocusChanged was called");
        if(mCircledPickerCardHeight == -1)
            mCircledPickerCardHeight = mCircledPickerCard.getHeight();
        mOnScrollListener.setmCircledPickerHeight(mCircledPickerCardHeight);
        mOnScrollListener.setIsActive(true);
        mDataset.get(0).mOffset = mCircledPickerCardHeight ;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause(){
        super.onPause();
        mCircledPicker.stop(false);
        mOnScrollListener.setIsActive(false);
        mOldCircularPickerValue = mCircledPicker.getValue();

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat(OLD_PICKER_VALUE, mOldCircularPickerValue);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setAlarm(){

        if(!mAlarmSet){
            final float value = mCircledPicker.getValue();
            mAlarmTime = ((long)value) * SECONDS_TO_MILLIS + System.currentTimeMillis();
            PendingIntent service = PendingIntent.getService(this, ALARM_REQUEST_CODE, new Intent(this,SlashSoundService.class), PendingIntent.FLAG_UPDATE_CURRENT)
;
            if (Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT) {
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, mAlarmTime, service);
            } else {
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, mAlarmTime, service);
            }
            mCircledPicker.start(mAlarmTime);
            setmAlarmSet(true);
            Log.i(TAG,value+"");
            if(!increaseDataSetCounter(value))
                Snackbar.make(mCircledPickerCard, getResources().getString(R.string.message_start_alarm), Snackbar.LENGTH_LONG).
                        setAction(getResources().getString(R.string.message_start_alarm_action), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addElementToDataset(new DataPair(value,1));
                                Log.i(TAG, value + "");
                            }
                        })
                        .show();
            }
        }

    private boolean increaseDataSetCounter(float value) {
        for(int i = 0; i < mDataset.size(); i++){
            if(mDataset.get(i).mTime == value){
                int j = i-1;
                int k = i;

                DataPair temp = mDataset.get(i);
                temp.mCounter++;
                while (j > 0 && mDataset.get(j).mCounter <= temp.mCounter){
                    mDataset.set(k,mDataset.get(j));
                    mDataset.set(j,temp);
                    mAdapter.notifyItemMoved(j,k);
                    j--;
                    k--;
                }
                saveDataSetToSharedPreferences();
                return true;


                /*float[] times = new float[mDataset.size()];
                for(int j = 0; j < mDataset.size(); j++){
                    times[j] = mDataset.get(j).mTime;
                }
                mDataset.get(i).mCounter++;
                sortDataset(mDataset);
                for(int j = 0; j < mDataset.size(); j++){
                    if(mDataset.get(j).mTime != times[j]){
                        for(int k = j+1; k < mDataset.size(); k++){
                            if(times[k] != mDataset.get(k).mTime){
                            mAdapter.notifyItemRangeChanged(j,k-j);
                            saveDataSetToSharedPreferences();
                            return true;
                            }
                        }
                    }
                }
                saveDataSetToSharedPreferences();
                return true;*/
            }
        }
        return false;
    }

    public void cancelAlarm(){
        PendingIntent service = PendingIntent.getService(this, ALARM_REQUEST_CODE, new Intent(this, SlashSoundService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.cancel(service);
        mCircledPicker.stop(true);
        setmAlarmSet(false);
    }

    @Override
    public void onClick(View view) {
        if(mAlarmSet){
            cancelAlarm();
        }else{
            setAlarm();
        }

    }

    public void setmAlarmSet(boolean mAlarmSet) {
        this.mAlarmSet = mAlarmSet;
        if(mAlarmSet){
            mFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this,android.R.drawable.ic_media_pause));
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(ALARM_TIME, mAlarmTime);
            editor.putBoolean(ALARM_SET, true);
            editor.commit();
        }else{
            mFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this,android.R.drawable.ic_media_play));
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(ALARM_TIME);
            editor.putBoolean(ALARM_SET, false);
            editor.commit();
        }
    }

    public void setCircledPickerValue(float mTime) {
        if(!mAlarmSet){
            mCircledPicker.setValue(mTime);
            mCircledPicker.invalidate();
        }
    }


    private void setCircledPickerHeight(int height) {

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCircledPickerCard.getLayoutParams();
        layoutParams.height = height;
        mCircledPickerCard.setLayoutParams(layoutParams);
    }

    @Override
    public void onTimerStop(boolean timerCancled) {
        Log.i(TAG, "OnTimerStop");
        if(timerCancled)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setmAlarmSet(false);
                }
            });
    }

    public class DataPair implements Comparable<DataPair>{
        float mTime;
        int mCounter;
        int mOffset = 0;

        public DataPair(float mTime, int mCounter) {
            this.mTime = mTime;
            this.mCounter = mCounter;
        }
        public DataPair(){mTime = -1;}

        @Override
        public int compareTo(DataPair rhs) {
            return rhs.mCounter - mCounter ;
        }
    }

    private ArrayList<DataPair> createDataSetFromSharedPreferences() {
        ArrayList<DataPair> dataPairs;
        String json = mSharedPreferences.getString(DATA_PAIRS,"null");

        if(!json.equals("null")){
            Gson gson = new Gson();
            dataPairs = new ArrayList<>(Arrays.asList(gson.fromJson(json, DataPair[].class)));
            sortDataset(dataPairs);
        }else{
            dataPairs = new ArrayList<>();
            dataPairs.add(new DataPair());
            dataPairs.add(new DataPair(120,1));
            dataPairs.add(new DataPair(300,1));
            dataPairs.add(new DataPair(600,1));
        }
        return dataPairs;
    }

    private void sortDataset(ArrayList<DataPair> dataPairs) {
        DataPair dataPair = dataPairs.remove(0);
        Collections.sort(dataPairs);
        dataPairs.add(0,dataPair);
    }

    private void saveDataSetToSharedPreferences(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(mDataset);

        editor.putString(DATA_PAIRS, json);
        editor.commit();
    }


    private void removeElementFromDataset(int position) {
        if(position <= 0)
            return;
        mDataset.remove(position);

        for(int i = position; i < mDataset.size(); i++){
            mAdapter.notifyItemMoved(i,i+1);
        }

        mAdapter.notifyItemRemoved(mDataset.size());
        saveDataSetToSharedPreferences();
    }

    private void addElementToDataset(DataPair dataPair) {
        mDataset.add(dataPair);
        mAdapter.notifyItemInserted(mDataset.size()-1);
        saveDataSetToSharedPreferences();
    }


}
