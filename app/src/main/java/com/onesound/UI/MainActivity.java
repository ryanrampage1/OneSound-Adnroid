/**
 *  Main.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 8/10/14.
 *  Copyright (c) 2014 OneSound LLC. All rights reserved.
 *
 *  This class is the main activity for the app.
 *  it holds the navigation drawer as well as the container
 *  for all fragments that are displayed.
 *
 */

package com.onesound.UI;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.appevents.AppEventsLogger;
import com.onesound.BaseActivity;
import com.onesound.BaseFragment;
import com.onesound.Cache;
import com.onesound.MusicService;
import com.onesound.PartySettingsActivity;
import com.onesound.R;
import com.onesound.SettingsFragmentActivity;
import com.onesound.UI.adapters.MainPagerAdapter;
import com.onesound.Utility;
import com.onesound.managers.PartyManager;
import com.onesound.managers.UserManager;
import com.onesound.models.GsonMockClasses;
import com.onesound.models.Party;
import com.onesound.models.SimpleStatusModel;
import com.onesound.models.Song;
import com.onesound.networking.APIService;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends BaseActivity {


    @Bind(R.id.pager) ViewPager viewPager;
    @Bind(R.id.tabLayout) TabLayout tabLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;
    /**
     * nowplayingview dj picture for current song
     */
    @Bind(R.id.dj_pic) ImageView djPicture;

    /**
     * nowplayingview artist name text for current song
     */
    @Bind(R.id.Artist) TextView artistName;

    /**
     * nowplayingview song name text for current song
     */
    @Bind(R.id.SongName) TextView songName;

    /**
     * nowplayingview album artwork for current song
     */
    @Bind(R.id.AlbumArt) ImageView albumArt;

    /**
     * View that displays all info on theh current song that is playing
     */
    @Bind(R.id.now_playing_view) View nowPlayingView;

    /**
     * Tag for logs
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "MainActivity";

    /**
     * Flag for if the activity is bound to a music service or not
     */
    private boolean bound = false;

    /**
     * The muisc service that the activity is bound to
     */
    private MusicService musicService;

    /**
     *
     */
    private MenuItem menuLeaveParty;
    private MenuItem menuEdit;
    private MenuItem menuSearch;

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Leave Party?")
                .setMessage("Are you sure you want to leave this party?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Cache.INSTANCE.clear();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private MenuItem menuMC;
    private Menu menu;

    /**
     *
     */
    public static final String CREATE = "create";
    public static final String EDIT = "edit";
    public static final String ACTION = "Action";
    public static final String PROFILE = "profile";

    /// broadcast reciever for network connectivity
    private final BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean mConnected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if(!UserManager.INSTANCE.isLogginComplete() & mConnected) {
//                Toast.makeText(getApplicationContext(), "Connection Restored and user logged in", Toast.LENGTH_SHORT).show();
            }
            else if(!mConnected){
//                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Bundle recieved = getIntent().getExtras();

        String type = "";
        if (recieved != null){
            type = recieved.getString(NearPartiesActivity.TYPE);
        }

        APIService.INSTANCE.initilizeSharedPrefs(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Enable status bar translucency (requires API 19)
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (toolbar != null) toolbar.setPadding(0, Utility.getStatusBarHeight(this),0,0);
        }

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        registerReceivers();

        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        tabLayout.setupWithViewPager(viewPager);

        // scroll to profile fragment in view pager if user wants to go to the profile
        if (type != null && type.equals(PROFILE)) viewPager.setCurrentItem(2, true);

        // register broadcast recievers
        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        if (UserManager.INSTANCE.inParty()) getParty();


    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        if (menu != null) toggleMenuOptions();

        String title = TextUtils.isEmpty(PartyManager.INSTANCE.getName()) ? "OneSound" : PartyManager.INSTANCE.getName();
        setTitle(title);

        setNowPlaying(Cache.INSTANCE.getNowPlaying());
    }

    @Override
    public void onStart() {
        super.onStart();

        // bind to the service if stream control
        if (UserManager.INSTANCE.isStreamable()) {
            bindService();
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Log facebook session
        AppEventsLogger.deactivateApp(this);
    }

    @SuppressWarnings("SameReturnValue")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuLeaveParty = menu.findItem(R.id.action_leave_party);
        menuEdit = menu.findItem(R.id.action_edit_party);
        menuSearch= menu.findItem(R.id.action_search_party);
        menuMC= menu.findItem(R.id.action_music_control);
        this.menu = menu;
        toggleMenuOptions();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            case R.id.action_edit_user:
                intent = new Intent(this, AddEditUserActivity.class);
                intent.putExtra(ACTION, EDIT);
                startActivity(intent);
                return true;

            case R.id.action_create_party:
                intent = new Intent(this, PartySettingsActivity.class);
                intent.putExtra(ACTION, CREATE);
                startActivity(intent);
                return true;

            case R.id.action_edit_party:
                intent = new Intent(this, PartySettingsActivity.class);
                intent.putExtra(ACTION, EDIT);
                startActivity(intent);
                return true;

            case R.id.action_leave_party:
                leaveParty();
                return true;

            case R.id.action_search_party:
                startActivity(new Intent(this, NearPartiesActivity.class));
                return true;

            case R.id.action_music_control:
                if (PartyManager.INSTANCE.isHost()) getMusicControl();
                return true;

            case R.id.action_settings:
                    startActivity(new Intent(this, SettingsFragmentActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Toggle which menu items are visible
     */
    private void toggleMenuOptions(){
        if (UserManager.INSTANCE.inParty()) {
            menuLeaveParty.setEnabled(true);
        } else {
            menuLeaveParty.setEnabled(false);
            menuSearch.setEnabled(true).setVisible(true);
        }
        switch (UserManager.INSTANCE.getState()) {
            case STREAMABLE:
            case NOSTREAM:
                menuEdit.setVisible(true);
                if (UserManager.INSTANCE.isStreamable()) menuMC.setEnabled(false);
                else menuMC.setEnabled(true);
                menuLeaveParty.setVisible(true);
                break;
            case MEMBER:
                menuEdit.setVisible(false);
                menuMC.setVisible(false);
                break;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if(getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mConnReceiver);

        // Unbind from the service
        if (bound) unBindService();

        try {
            this.unregisterReceiver(mybroadcast);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void registerReceivers() {
        registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            bound = true; // set the bound flag to true
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }

    };

    /**
     * Function that unbinds the service from the activity
     */
    private void unBindService() {
        // stop the runnable and free any recources in the service
        musicService.Stop();

        // tell android to kill the service ( it does it when it wants to, not immediatly)
        musicService.stopSelf();

        // unbind the service
        unbindService(mConnection);

        // set the bound flag
        bound = false;
    }

    private void bindService() {
        // success getting music control, bind to the music service
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        MainActivity.this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(getBaseContext(), "Music Control Gained", Toast.LENGTH_SHORT).show();
    }

    /**
     * Broadcastreceiver for the screen turning on and off
     */
    private final BroadcastReceiver mybroadcast = new BroadcastReceiver() {

        //When Event is published, onReceive method is called
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("[BroadcastReceiver]", "MyReceiver");

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i("[BroadcastReceiver]", "Screen ON");
//                UpdateCurrentPage(getPager().getCurrentItem());
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i("[BroadcastReceiver]", "Screen OFF");
            }

        }
    };

    /**
     * Animater for view pager transition. The pages will pivot at  a 20 degree angle as it switches
     */
    private class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        public void transformPage(View view, float position) {
            view.setRotationY(position * -20);
        }
    }

    /**
     * Sets all now playing view elements
     * @param s song to display at the bottom
     */
    private void setNowPlaying(Song s){
        if (s == null){
            // no song playing, hide view and return from function
            nowPlayingView.setVisibility(View.GONE);
            return;
        }

        // set the album art
        if (!TextUtils.isEmpty(s.getAlbum())) {
            try {
                Glide.with(this).load(s.getAlbum()).into(albumArt);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                albumArt.setImageResource(R.drawable.songimagefornosongartwork);
            }
        } else {
            albumArt.setImageResource(R.drawable.songimagefornosongartwork);
        }

        // set now playing user image
        if (!TextUtils.isEmpty(s.getUserPlaying().getPhotoURL())) {
            try {
                Glide.with(this).load(s.getUserPlaying().getPhotoURL()).into(djPicture);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                djPicture.setImageResource(R.drawable.defaultuserimageformainparty);
            }
        } else {
            djPicture.setImageResource(R.drawable.defaultuserimageformainparty);
        }

        // set text views
        songName.setText(s.getTitle());
        artistName.setText(s.getUserPlaying().getName());

        // show the view
        nowPlayingView.setVisibility(View.VISIBLE);
    }

    /**
     *
     * Eventbus method for when the now playing view at the bottom of the screen needs to be
     * updated
     *
     * @param event has occured where the now playing view needs to be updated
     */
    public void onEvent(MusicService.UpdateMainActivity event) {
        setNowPlaying(Cache.INSTANCE.getNowPlaying());
        Fragment f = ((MainPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());
        ((BaseFragment) f).onRefresh();

    }

    /**
     * Make network call to get the users party and set the ui
     */
    private void getParty(){
        APIService.INSTANCE.getRestAdapter().getParty()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Party>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                    }

                    @Override
                    public void onNext(Party p) {
                        if (p != null) PartyManager.INSTANCE.SetParty(p);
                        String title = TextUtils.isEmpty(PartyManager.INSTANCE.getName()) ? "Party" : PartyManager.INSTANCE.getName();
                        setTitle(title);

                        // if the user is host but dose not hav stream control show a dialog
                        // to ask if they want it
                        if (UserManager.INSTANCE.isShowGainStreamDialog() && !UserManager.INSTANCE.isStreamable() && PartyManager.INSTANCE.isHost()) {

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Music Control")
                                    .setMessage("You are the Host of this party but do not have music control. Would you like to gain it?")
                                            // if the user wants stream control, get it
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            getMusicControl();
                                        }
                                    })
                                            // if not show them where to get it and dont ask again
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("Music Control")
                                                    .setMessage("To gain music control, select it from the options button.")
                                                    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // set flag to not show dialog again
                                                            UserManager.INSTANCE.setShowGainStreamDialog(false);
                                                        }
                                                    }).show();
                                        }
                                    }).show();
                        }
                    }
                });
    }

    /**
     * Get music control for the current user
     */
    private void getMusicControl() {
        APIService.INSTANCE.getRestAdapter().getMusicControl(PartyManager.INSTANCE.getPID(), new GsonMockClasses.MusicControl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Subscriber<Object>() {
                            @Override
                            public void onCompleted() { }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                            }

                            @Override
                            public void onNext(Object statusModel) {
                                bindService();
                            }
                        });
    }

    /**
     * Makes network call for a user to leave a party
     */
    private void leaveParty(){
        APIService.INSTANCE.getRestAdapter().leaveParty(UserManager.INSTANCE.getID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Subscriber<SimpleStatusModel>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                            }

                            @Override
                            public void onNext(SimpleStatusModel statusModel) {
                                if (statusModel.getStatus().equals("success")) {
                                    // clear party manager
                                    PartyManager.INSTANCE.clear();

                                    // set the user state
                                    UserManager.INSTANCE.SetState(UserManager.USER_STATE.MEMBER);

                                    toggleMenuOptions();

                                    // go back to party search screen
                                    finish();
                                }
                            }
                        });
    }

}