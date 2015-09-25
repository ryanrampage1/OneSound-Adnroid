/**
 *  MusicService.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 1/1/15.
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 *  This class is the service that runs all music playing
 *
 */

package com.onesound;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.onesound.UI.MainActivity;
import com.onesound.managers.PartyManager;
import com.onesound.models.Song;
import com.onesound.networking.APIService;

import de.greenrobot.event.EventBus;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Service that controls all music streaming on the host device
 */
public class MusicService extends Service {

    /** Tag for the service */
    private final String TAG = "Music Service";

    /** Tag for a pause intent. This is used with the status bar notification controls */
    private static final String PAUSE = "Pause";

    /** Tag for play intent. This is used with the status bar notification controls  */
    private static final String PLAY = "Play";

    /** Tag for skip intent. This is used with the status bar notification controls  */
    private static final String SKIP = "Skip";

    /** Media players rotated during playback */
    private MediaPlayer mediaPlayer1, mediaPlayer2;

    /** Flags for which of the media players are ready to play */
    private boolean onePrep = false, twoPrep = false;

    /** Flag for if media player one is active or not */
    private boolean oneIsAcitve = true;

    /** notification manager to build the notifications */
    private NotificationManager mNM;

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /** Variables to track time calculations */
    private int mTimeLeft;

    /** Broadcast receiver to handle pending intents for notification actions */
    private BroadcastReceiver receiver;

    /** song to be displayed in the notification */
    private Song nextSong;

    /** Delay time between each execution of the runnable in milliseconds */
    private int mDelayTime = 1000;

    /** handler for all threads */
    private final Handler mHandler = new Handler();

    /** Wifi lock which ensures service runs while screen is off */
    private WifiManager.WifiLock mWifiLock;

    public enum STATE {
        Playing,            /// Activeplayer playing and updating progress bar
        Paused,             /// user paused the song
        Stopped,            /// no players playing and nothing in playlist
        GettingNext,        /// wating for get next callback
        InitialPrep,        /// prep the active player
        PlayingPrep,        /// Prepare the inactive player while active playing
        PlayingHold,        /// hold when get next returns null
        GetNextPlaying,      /// get the next song from playing state
        Call,
        Skip
    }

    public STATE getState() {
        return mState;
    }

    /** Current state of the music service */
    private STATE mState;

    @Override
    public void onCreate() {
        // set the notification manager
        mNM = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

//        // check if there is a song marked as current song
//        if (PartyManager.INSTANCE.getCurrentSong().getExternalID() != null) {
//            // prepare the song marked as current song in party manager
//            Prepare(PartyManager.INSTANCE.getCurrentSong().getExternalID());
//            mState = STATE.InitialPrep;
//        } else

        mState = STATE.Stopped;                     //no current song, inatilize state to stopped

        mHandler.postDelayed(mUpdate, mDelayTime);  // start the handler for updating the music

        // create a wifi lock for when screen turns off
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        // create the broadcast receiver to handle the pending intents
        receiver = new BroadcastReceiver ()  {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction(); // get the action of the intent

                if(PAUSE.equals(action)) {
                    // pause pressed from playing state
                    SetState(STATE.Paused);
                    showNotification();

                } else if(PLAY.equals(action)) {
                    // play pressed from paused state
                    SetState(STATE.Playing);
                    showNotification();
                }else if (SKIP.equals(action)){
                    SetState(STATE.Skip);
                    showNotification();
                }
            }
        };

        // create an intent filter and register the broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(PLAY);
        filter.addAction(PAUSE);
        filter.addAction(SKIP);
        this.registerReceiver(this.receiver, filter);

        // Get the telephony manager
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mState == STATE.Paused) SetState(STATE.Playing);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (mState == STATE.Playing) SetState(STATE.Paused);
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mState == STATE.Playing) SetState(STATE.Paused);
                        break;
                }
            }
        };
        // Register the listener wit the telephony manager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mNM.cancelAll();                                    // Cancel the persistent notification.
        if (mediaPlayer1 != null) mediaPlayer1.release();   // delete the first media player
        if (mediaPlayer2 != null) mediaPlayer2.release();   // delete the second media player
        mHandler.removeCallbacksAndMessages(null);                  // stop the handler from running
        try{
            this.unregisterReceiver(receiver);                  // unregister the reciever
        }catch (java.lang.IllegalArgumentException e ){
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /** Show a notification while this service is running. */
    private void showNotification() {

//        mNM.cancelAll();

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Pause pending intent which will pause the song
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 0,
                new Intent().setAction(PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);

        // play pending intent which will play a song
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 0,
                new Intent().setAction(PLAY), PendingIntent.FLAG_UPDATE_CURRENT);

        // play pending intent which will play a song
        PendingIntent pendingIntentSkip = PendingIntent.getBroadcast(this, 0,
                new Intent().setAction(SKIP), PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // not lollipop

            final Notification.Builder builder =
                    new Notification.Builder(this)
                            .setContentTitle(nextSong.getTitle())
                            .setContentText(nextSong.getArtist())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentIntent(pIntent)
                            .setUsesChronometer(false);

            if (mState == STATE.Paused)
                builder.addAction(R.drawable.ic_stat_play, "Play", pendingIntentPlay);
            else
                builder.addAction(R.drawable.ic_stat_pause, "Pause", pendingIntentPause);

            builder.addAction(R.drawable.ic_stat_next, "Next", pendingIntentSkip);

            if (nextSong.getAlbum() != null && !nextSong.getAlbum().isEmpty()) {
                try {
                    Glide.with(this)
                            .load(nextSong.getAlbum().replace("large", "t500x500"))
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    super.onLoadFailed(e, errorDrawable);
                                    builder.setLargeIcon(BitmapFactory.decodeResource(MusicService.this.getResources(),
                                            R.drawable.songimagefornosongartwork));
//                            builder.setOngoing(true);
                                    mNM.notify(0, builder.build());
                                }

                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                                    builder.setLargeIcon(bitmap);
//                                  builder.setOngoing(true);
                                    mNM.notify(0, builder.build());
                                }
                            });

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            } else {
                builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.songimagefornosongartwork));
//            builder.setOngoing(true);
                mNM.notify(0, builder.build());
            }
        }
        else {
            // lollipop
            final Notification.Builder builder =
                    new Notification.Builder(this)
                            .setContentTitle(nextSong.getTitle())
                            .setContentText(nextSong.getArtist())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setColor(getResources().getColor(R.color.onesoundBlueDark))
                            .setContentIntent(pIntent)
                            .setDeleteIntent(pIntent)
                            .setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0, 1))
                            .setShowWhen(false)
                            .setUsesChronometer(false);

            if (mState == STATE.Paused)
                builder.addAction(R.drawable.ic_stat_play, "Play", pendingIntentPlay);
            else
                builder.addAction(R.drawable.ic_stat_pause, "Pause", pendingIntentPause);

            builder.addAction(R.drawable.ic_stat_next, "Next", pendingIntentSkip);

            if (nextSong.getAlbum() != null && !nextSong.getAlbum().isEmpty()) {
                Glide.with(this)
                        .load(nextSong.getAlbum().replace("large", "t500x500"))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);
                                builder.setLargeIcon(BitmapFactory.decodeResource(MusicService.this.getResources(),
                                        R.drawable.songimagefornosongartwork));
//                                builder.setOngoing(true);
                                mNM.notify(0, builder.build());
                            }

                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                                builder.setLargeIcon(bitmap);
//                                builder.setOngoing(true);
                                mNM.notify(0, builder.build());
                            }
                        });

            } else {
                builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.songimagefornosongartwork));
                builder.setOngoing(true);
                mNM.notify(0, builder.build());
            }
        }
    }

    /** Get the next song in the playlist */
    private void GetNext() {
        APIService.INSTANCE.getRestAdapter().getNextSong(PartyManager.INSTANCE.getPID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Song>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                        PartyManager.INSTANCE.setNextSong(null);
                        PartyManager.INSTANCE.setSet(true);
                    }

                    @Override
                    public void onNext(Song song) {
                        PartyManager.INSTANCE.setNextSong(song);
                        PartyManager.INSTANCE.setSet(true);

                    }
                });
    }

    /**
     * Prepare the stream . this includes setting the url for the stream
     * as well as switching active players and settign all properties
     * for the media player that is about to start
     */
    private void Prepare() {

        // create a new temp player
        MediaPlayer player = new MediaPlayer();
        String id;
        id = PartyManager.INSTANCE.getNextSong().getExternalID();
        nextSong = PartyManager.INSTANCE.getNextSong();

        // set the player type to stream music
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // ensure player runs while screen is off
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        try {
            // set the source of the strea
            player.setDataSource(BuildUrl(id));

            // when prepared set the prep flag to true for the player
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (mp == mediaPlayer1)
                        onePrep = true;
                    else
                        twoPrep = true;
                }
            });

            //create on error listener
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // handle the error based on what it is
                    switch (what) {

                        // Media server died. In this case, the application must release
                        // the MediaPlayer object and instantiate a new one.
                        case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                                SwitchActivePlayer();
                            break;
                    }

                    Toast.makeText(getApplicationContext(),
                            getString(R.string.song_prepare_error),         // show tost about failure
                            Toast.LENGTH_LONG).show();

                    switch (mState){
                        case InitialPrep:
                            SetState(STATE.GettingNext);
                            break;
                        case PlayingPrep:
                            SetState(STATE.GetNextPlaying);
                            break;
                        default:
                            SetState(STATE.Stopped);
                            break;
                    }
                    return true;
                }
            });

            // when the song is complete, set the time left to 0
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mTimeLeft = 0;
                }
            });


            /*
                future add functionality to the below listener for a song buffering view
             */
            player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        // player is buffering show load circle
//                        if (partyFragment!=null) partyFragment.ToggleProgressBar(true);
                    }
                    else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        // player is done buffering
//                        if (partyFragment!=null) partyFragment.ToggleProgressBar(false);
                    }
                    return false;
                }
            });

            // asyncronusly prepare the stream
            player.prepareAsync();

        } catch (java.io.IOException e) {
            Log.e("Url Exception", e.toString());
        }

        // assign the temp player to the player that is up
        if (oneIsAcitve && mState != STATE.GettingNext) mediaPlayer2 = player;
        else mediaPlayer1 = player;

    }

    /**
     * Handles playing and pausing
     */
    public void PlayPause() {
        switch (mState) {

            case Paused:
                // currently paused, move to playing
                SetState(STATE.Playing);
                break;

            case Stopped:
                // currently stopped move to getting next
                SetState(STATE.GettingNext);
                break;

            case Playing:
                // currently playing move to paused
                SetState(STATE.Paused);
                break;
        }
    }


    /**  Switches the active and inactive media players */
    private void SwitchActivePlayer() {
        if (oneIsAcitve) {
            oneIsAcitve = false;    // set the player that just finished to false
            onePrep = false;        // set the players prep flag to false
            mediaPlayer1.reset();   // reset the player that just finished
            mediaPlayer1.release(); // relese the player that just finished
            mediaPlayer1 = null;    // set the player to null
        } else {
            oneIsAcitve = true;
            twoPrep = false;
            mediaPlayer2.reset();
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }
    }

    /**
     * Create a url for streaming from sound cloud
     * with only a song id #
     *
     * @param ID of the song
     * @return the url for streaming the song
     */
    private String BuildUrl(String ID) {
        return "https://api.soundcloud.com/tracks/"     // base api call string
                + ID                                    // soundcloud id of the song to play
                + "/stream?client_id="                  // tell the api we want to stream
                + "d9b5ddf849438ccddca1256ba5c03067";   // soundcloud client id
    }

    /**
     * Set the state of the state machine
     * This will also do any entry tasks to the state that are needed
     * @param s state to be set
     */
    public void SetState(STATE s) {
        switch (s) {
            case Stopped:
                Log.d(TAG, "STOPPED");

                // relese wifi lock
                if (mWifiLock != null && mWifiLock.isHeld()) mWifiLock.release();

                // stop the active player
                if (mediaPlayer1 != null && mediaPlayer1.isPlaying()) mediaPlayer1.stop();
                if (mediaPlayer2 != null && mediaPlayer2.isPlaying()) mediaPlayer2.stop();

                // update the party fragment if it is visible
//                if (partyFragment != null && mState == STATE.PlayingHold) partyFragment.Update();

                // reset both players so we start fresh
                ResetPlayers();

                // cancel all notifications. may remove this
//                mNM.cancelAll();
                break;

            case GettingNext:
                Log.d(TAG, "GETTINGNEXT");

                mDelayTime = 1000;          // set delaytime back to 1 second
                GetNext();                  // call get next to get the next song
                break;

            case GetNextPlaying:
                Log.d(TAG, "GETNEXTPLAYING");
                GetNext();                  // get the next song
                break;

            case InitialPrep:
                Log.d(TAG, "INITPREP");
                mWifiLock.acquire();        // turn on wifi lock
                break;

            case PlayingPrep:
                Log.d(TAG, "PLAYINGPREP");
                break;

            case Playing:
                if (mediaPlayer1 != null || mediaPlayer2!=null) {

                    Log.d(TAG, "PLAYING");

                    // show notification
//                    if ((mState == STATE.InitialPrep || mState == STATE.PlayingPrep) && partyFragment == null)
                        showNotification();

                    // update the now playing fragment and hide the play button
//                    if (partyFragment != null) {
//                        partyFragment.TogglePlayButton(false);
//                    }

                    Cache.INSTANCE.setNowPlaying(nextSong);
                    EventBus.getDefault().post(new UpdateMainActivity(UpdateMainActivity.PLAYING, nextSong));

                    // start the active player
                    try {
                        if (oneIsAcitve)
                            mediaPlayer1.start();
                        else
                            mediaPlayer2.start();
                    }catch (NullPointerException e){
                        // catch incase of edge case, will not anr
                        Log.e(TAG, e.toString());
                    }
                }
                break;

            case Paused:
                if (mediaPlayer1 != null || mediaPlayer2!=null) {
                    Log.d(TAG, "PAUSED");

                    // if the now playing screen is showing display the play buton
//                    if (partyFragment != null)
//                        partyFragment.TogglePlayButton(true);

                    try {
                        // pause the active player
                        if (oneIsAcitve)
                            mediaPlayer1.pause();
                        else
                            mediaPlayer2.pause();
                    }catch (NullPointerException e){
                        Log.e(TAG, e.toString());
                    }
                }
                break;

            case PlayingHold:
                Log.d(TAG, "PLAYHOLD");
                break;

            case Call:
                break;

            case Skip:
                // get the next song in the playlist
                GetNext();
                break;

            default:
                break;
        }

        mState = s;
    }

    /** Background Runnable thread that updates the state machine */
    private final Runnable mUpdate = new Runnable() {
        @Override
        public void run() {

            switch (mState) {

                // both media players are not playing
                case Stopped:
                    SetState(STATE.GettingNext);        // Set state to getting next
                    break;

                // get next when neither media player is playing
                case GettingNext:
                    if (PartyManager.INSTANCE.isSet()) {
                        if (PartyManager.INSTANCE.getNextSong() == null) {
                            SetState(STATE.Stopped);                   // no next song move to stopped state
                            mDelayTime = 10000;                        // set delay time to 10 seconds
                        } else {
                            Prepare();                     // prepare the active player
                            SetState(STATE.InitialPrep);                // set state to inital prep
                        }
                        PartyManager.INSTANCE.setSet(false);            // reset the flag for song set in party manager
                    }
                    break;

                case GetNextPlaying:
                    if (PartyManager.INSTANCE.isSet()) {
                        if (PartyManager.INSTANCE.getNextSong() == null) {
                            SetState(STATE.PlayingHold);                                 // no next song but playing so move to hold state
                        } else {
                            Prepare();
                            SetState(STATE.PlayingPrep);                                 // next song exists so prepare it
                        }
                        PartyManager.INSTANCE.setSet(false);                                // reset the flag for song set in party manager
                    }
                    break;

                // prep active player when no players playing
                case InitialPrep:
                    if ((oneIsAcitve && onePrep) || (!oneIsAcitve && twoPrep)) {
                        SetState(STATE.Playing);     // change state to playing
                    }
                    break;

                // prepare inactive player while active is playing
                case PlayingPrep:
                    if (((oneIsAcitve && twoPrep) || (!oneIsAcitve && onePrep)) && (mTimeLeft < 5)) {
                        SwitchActivePlayer();        // switch active and inactive players
                        SetState(STATE.Playing);     // move state back to playing
                    }
                    break;

                // song is playing
                case Playing:
                    SetTime();
                    if (mTimeLeft < 101) {              // calculated time left in the song (10th of a seconds) < 100){
                        SetState(STATE.GetNextPlaying); // move to get next playing state
                    }
                    break;

                // song is paused dont do anyting
                case Paused:
                    // paused do nothing
                    break;

                // no song next just doing playing tasks
                case PlayingHold:
                    SetTime();
                    if (mTimeLeft < 101 ){
                        Cache.INSTANCE.setNowPlaying(null);
                        EventBus.getDefault().post(new UpdateMainActivity(UpdateMainActivity.PLAYING, nextSong));
                        SetState(STATE.Stopped);
                    }
                    break;

                case Call:
                    break;

                case Skip:
                    if (PartyManager.INSTANCE.isSet()) {
                        if (PartyManager.INSTANCE.getNextSong() == null) {
                            SetState(STATE.PlayingHold);                                 // no next song but playing so move to hold state
                            Toast.makeText(getBaseContext(), "There are no other songs in the playlist.", Toast.LENGTH_SHORT).show();
                        } else {
                            Prepare();
                        }
                        PartyManager.INSTANCE.setSet(false);                                // reset the flag for song set in party manager
                    }

                    // check if the inactive player is set yet or not
                    if (((oneIsAcitve && twoPrep) || (!oneIsAcitve && onePrep))) {
                        SwitchActivePlayer();        // switch active and inactive players
                        SetState(STATE.Playing);     // move state back to playing
                    }
                    break;

                default:
                    break;
            }

            // wait 200 milliseconds and re run the runable
            mHandler.postDelayed(this, mDelayTime);
        }
    };

    /**
     * set the current time in the song for tracking purposes and deciding when to get next song
     */
    private void SetTime() {
        if (oneIsAcitve && mediaPlayer1!= null)
            mTimeLeft = (mediaPlayer1.getDuration() - mediaPlayer1.getCurrentPosition()) / 100;
        else if (mediaPlayer2!=null)
            mTimeLeft = (mediaPlayer2.getDuration() - mediaPlayer2.getCurrentPosition()) / 100;
    }

    /**
     * Function to stop the service because android may keep the service running and it
     * wont hit the on destroy
     *
     * this will stop the runnable that updates the music player,
     * unregister the recievers, cancel notifications and reset the media players
     */
    public void Stop(){
        mHandler.removeCallbacksAndMessages(null);      // stop the handler from running
        try {
            this.unregisterReceiver(receiver);          // unregister the reciever
        }catch(IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        }
//        mNM.cancelAll();                                // cancel all notificaitons
        ResetPlayers();                                 // reset and relese the music players
    }

    /**
     * Reset and relese both players as well as set all playing conditions
     */
    private void ResetPlayers(){
        onePrep = false; twoPrep = false;               // set both preps to false
        oneIsAcitve = true;                             // set one to acitve
        if (mediaPlayer1 != null) {
            mediaPlayer1.reset();                       // reset the player
            mediaPlayer1.release();                     // relese the player
            mediaPlayer1 = null;                        // set the player to null
        }
        if (mediaPlayer2 != null) {
            mediaPlayer2.reset();
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }
    }

    public static class UpdateMainActivity {
        private int status;
        private Song song;

        public static int PLAYING = 0;
        public static int PAUSED = 1;
        public static int NEW_SONG = 2;

        public UpdateMainActivity(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public UpdateMainActivity(int status, Song song) {
            this.status = status;
            this.song = song;
        }

        public Song getSong() {

            return song;
        }
    }
}
