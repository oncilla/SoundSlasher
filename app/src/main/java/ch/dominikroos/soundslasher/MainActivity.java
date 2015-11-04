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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{


    private static final int ALARM_REQUEST_CODE = 0;
    private static final long SECONDS_TO_MILLIS = 1000;
    private static final String ALARM_TIME = "ALARM_TIME";
    private static final String ALARM_SET = "ALARM_SET";
    private static final String DEFAULT_VALUE = "DEFAULT_VALUE";
    private static final String TAG = "MAIN_ACTIVITY";
    private BroadcastReceiver mBroadcastReceiver;
    private AudioManager mAudioManager;
    private AlarmManager mAlarmManager;
    private CircledPicker mCircledPicker;
    private CardView mCircledPickerCard;
    private long mAlarmTime;
    private boolean mAlarmSet = false;
    private SharedPreferences mSharedPreferences;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private TimeListAdapter mAdapter;
    private ArrayList<Float> mDataset;

    private int mInitialCardSize;

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

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mBroadcastReceiver = getBroadcastReceiver();
        registerReceiver(mBroadcastReceiver, new IntentFilter("ch.dominikroos.soundslasher"));

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mDataset = new ArrayList<>();
        mDataset.add(700f);
        mDataset.add(700f);
        mDataset.add(700f);
        mAdapter = new TimeListAdapter(mDataset,this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int scrollOffset;
                if((scrollOffset = recyclerView.computeVerticalScrollOffset()) > 50){
                    mCircledPickerCard.setLayoutParams(new LinearLayout.LayoutParams(200, (int) (mInitialCardSize * ((Math.max(1000 - scrollOffset, 500) / (float) 1000)))));
                    Log.i(TAG,"Scrolloffset: "+scrollOffset);
                }

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAlarmTime = mSharedPreferences.getLong(ALARM_TIME,-1);
        mAlarmSet = mSharedPreferences.getBoolean(ALARM_SET,false);
        if(mAlarmSet && mAlarmTime > System.currentTimeMillis()){
            float timeToAlarm = (float)(mAlarmTime - System.currentTimeMillis())/1000;
            mDataset.set(0,timeToAlarm);
            mAdapter.notifyDataSetChanged();

        }else{
            mAlarmSet = false;
            mDataset.set(0, 700f);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        mCircledPicker.stop();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if(mAlarmSet){
            editor.putLong(ALARM_TIME, mAlarmTime);
        }else{
            editor.remove(ALARM_TIME);
        }
        editor.putBoolean(ALARM_SET,mAlarmSet);
        editor.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    public void setCircledPicker(CircledPicker circledPicker){
        mCircledPicker = circledPicker;

        mAlarmTime = mSharedPreferences.getLong(ALARM_TIME,-1);
        mAlarmSet = mSharedPreferences.getBoolean(ALARM_SET,false);
        if(mAlarmSet && mAlarmTime > System.currentTimeMillis()){
            float timeToAlarm = (float)(mAlarmTime - System.currentTimeMillis())/1000;
            mCircledPicker.setValue(timeToAlarm);
            mCircledPicker.start(mAlarmTime);
        }
    }

    public void setAlarm(){

        if(!mAlarmSet){
            mAlarmTime = ((long)mCircledPicker.getValue()) * SECONDS_TO_MILLIS + System.currentTimeMillis();
            setAlarm(mAlarmTime);
            mCircledPicker.start(mAlarmTime);
        }
    }

    public void cancelAlarm(){
        PendingIntent pi = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, new Intent("ch.dominikroos.soundslasher"), PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.cancel(pi);
        mCircledPicker.stop();
        mAlarmSet = false;
    }

    private void setAlarm (long ms){
        PendingIntent pi = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, new Intent("ch.dominikroos.soundslasher"), PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT) {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, ms, pi);
        } else {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, ms, pi);
        }
        mAlarmSet = true;
    }


    public void stopAllAudio(View view){
        // Request audio focus for playback
        int result = mAudioManager.requestAudioFocus(focusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);


        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // other app had stopped playing song now , so u can do u stuff now .
            Snackbar.make((CoordinatorLayout)findViewById(R.id.coordinatorLayout), "Success?", Snackbar.LENGTH_SHORT).show();
        }
    }

    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) :
                            // Lower the volume while ducking.
                            //mediaPlayer.setVolume(0.2f, 0.2f);
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                            //pause();
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS) :
                            //stop();
                            //ComponentName component = new ComponentName(AudioPlayerActivity.this,MediaControlReceiver.class);
                            //am.unregisterMediaButtonEventReceiver(component);
                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN) :
                            // Return the volume to normal and resume if paused.
                            //mediaPlayer.setVolume(1f, 1f);
                            //mediaPlayer.start();
                            break;
                        default: break;
                    }
                }
            };

    public BroadcastReceiver getBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopAllAudio(null);
                if(mCircledPicker != null){
                    mCircledPicker.stop();
                }
                mAlarmSet = false;
            }
        };
    }
}
