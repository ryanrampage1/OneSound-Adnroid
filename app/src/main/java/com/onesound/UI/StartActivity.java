/**
 * StartActivity.Java
 * OneSound
 * <p/>
 * Created by Ryan Casler on 8/10/14.
 * Copyright (c) 2014 OneSound LLC. All rights reserved.
 * <p/>
 * This class is the start activity for the app.
 * displays the splash screen while the user is being logged in
 */
package com.onesound.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.newrelic.agent.android.NewRelic;
import com.onesound.BaseActivity;
import com.onesound.KEYS;
import com.onesound.R;
import com.onesound.Utility;
import com.onesound.managers.UserManager;
import com.onesound.models.GsonMockClasses;
import com.onesound.models.User;
import com.onesound.networking.APIService;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class StartActivity extends BaseActivity {

    private static final int CORRECT_API = 0;
    private static final int DEPERICATED_API = 1;
    private static final int INCTIVE_API = 2;
    private String accessToken;

    @Bind(R.id.rootView) View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ButterKnife.bind(this);

        // check the users android version, if it lollipop or greter, color the statusbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // Enable status bar translucency
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            rootView.setPadding(0, Utility.getStatusBarHeight(this), 0, 0);
        }

        NewRelic.withApplicationToken(KEYS.NEW_RELOC).start(this.getApplication());

        APIService.INSTANCE.initilizeSharedPrefs(this);

        FacebookSdk.sdkInitialize(getApplicationContext());

        LoginManager.getInstance().registerCallback(CallbackManager.Factory.create(), new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {}

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException e) {}
        });

        accessToken = AccessToken.getCurrentAccessToken() == null ? null : AccessToken.getCurrentAccessToken().getToken();

        compositeSubscription.add(APIService.INSTANCE.getRestAdapter().isValid()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GsonMockClasses.VersionCallback>() {
                    @Override
                    public void call(GsonMockClasses.VersionCallback versionCallback) {
                        handleVersionCB(versionCallback.getVersion_status());
                    }
                }));
    }

    /**
     * Privat function called anytime you want to exit the start activity
     */
    private void exitStart() {
        Intent mainIntent = new Intent(StartActivity.this, NearPartiesActivity.class);
        StartActivity.this.startActivity(mainIntent);
        finish();
    }

    /**
     * Function to display the app in the app store, used for either ratings of updating
     * the app
     */
    private void showInAppStore() {
        final String appPackageName = getPackageName();
        try {
            // google play store if it is installed
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            // not found, open in browser
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    /**
     * @param status Status of the api. 0 is valid, 1 is depricated and 2 is unusable
     */
    private void handleVersionCB(int status) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setTitle("App Version Out of Date");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showInAppStore();
            }
        });

        switch (status) {
            case CORRECT_API:
                loginUser(accessToken);
                break;

            case DEPERICATED_API:
                builder.setMessage("It is reccomended that you update this app. Would you like to do so now?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                loginUser(accessToken);
                            }
                        }).create().show();
                break;

            case INCTIVE_API:
                builder.setMessage("You can not use this app without updating. Would you like to do so now?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog Kill the app
                                int pid = android.os.Process.myPid();
                                android.os.Process.killProcess(pid);
                                System.exit(0);
                            }
                        }).create().show();
                break;
        }
    }

    public void loginUser(final String token) {
        SharedPreferences prefs = getSharedPreferences("com.onesound.app", Context.MODE_PRIVATE);
        String SharedPrefsS = prefs.getString("AccessToken", null);

        UserManager.INSTANCE.setOneSoundToken(SharedPrefsS);
        Observable<User> userObservable;

        if (SharedPrefsS == null) userObservable = APIService.INSTANCE.getRestAdapter().getGuest();
        else if (!TextUtils.isEmpty(token))
            userObservable = APIService.INSTANCE.getRestAdapter().logInUser(token);
        else userObservable = APIService.INSTANCE.getRestAdapter().loginGuest();

        compositeSubscription.add(userObservable.subscribe(new Action1<User>() {
            @Override
            public void call(User user) {
                UserManager.INSTANCE.SetUser(user);
                APIService.INSTANCE.setSharedPrefs();
                exitStart();
            }
        }));
    }

}