package ch.dominikroos.soundslasher;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SlashSoundService extends IntentService {
    public static final String ACTION_SLASH_SOUND = "ch.dominikroos.soundslasher.action.slash_sound";
    private static final String TAG = "SLASH_SOUND_SERVICE";


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSlashSound(Context context) {
        Intent intent = new Intent(context, SlashSoundService.class);
        intent.setAction(ACTION_SLASH_SOUND);
    }


    public SlashSoundService() {
        super("SlashSoundService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        slashSound();
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

    private void slashSound() {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(focusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        Log.d(TAG, "Result code:" + result);
    }

}
