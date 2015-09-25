package com.onesound.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.onesound.BuildConfig;
import com.onesound.MyApplication;
import com.onesound.R;
import com.onesound.managers.PartyManager;
import com.onesound.managers.UserManager;
import com.onesound.models.User;
import com.onesound.networking.APIService;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by ryan on 11/13/14.
 * Logging fragment that handles logging and settings
 */

public class LoginFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "LoginFragment";  // tag for log
    private TextView LoginStatus;                       // text view that display s status
    private static final String WEB_URL = "http://www.onesoundapp.com"; // website url
    private CallbackManager callbackManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // add google analytics if app is not debug mode
        if (!BuildConfig.DEBUG) {
            Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
                    MyApplication.TrackerName.APP_TRACKER);
            t.setScreenName("SettingsFragment");

            t.send(new HitBuilders.AppViewBuilder().build());
        }

        View view = inflater.inflate(R.layout.activity_login, container, false);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email,  user_friends");
        loginButton.setFragment(this);

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    // Log out logic
                    PartyManager.INSTANCE.clear();
                    // log out the user by getting a guest account
                    APIService.INSTANCE.getGuest(GuestUserCallBack);
                    UserManager.INSTANCE.SetUserState(false, false);
                }
            }
        };

        accessTokenTracker.startTracking();

        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        PartyManager.INSTANCE.clear();

                        if (getView() != null) {
                            getView().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        }

                        AccessToken.setCurrentAccessToken(loginResult.getAccessToken());
                        APIService.INSTANCE.logInUser(loginResult.getAccessToken().getToken(), GuestUserCallBack);
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    }

                });

        // text view for logged in status
        LoginStatus = (TextView) view.findViewById(R.id.loginStatusText);

        /*
         * Button that goes to website
         */
        Button web = (Button) view.findViewById(R.id.websitebutton);
        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(WEB_URL));
                startActivity(i);
            }
        });

        SetText();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();

    }

    private void SetText(){
        LoginStatus.setText("Currently signed in as " + UserManager.INSTANCE.getName());
    }

    private final Callback<User> GuestUserCallBack = new Callback<User>() {
        @Override
        public void success(User u, retrofit.client.Response response) {
            Log.i(TAG, "User Callback Success");

            if (getView() != null) {
                getView().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }

            if(u != null){

                // set all local user info based on guest account
                UserManager.INSTANCE.SetUser(u);

                APIService.INSTANCE.setSharedPrefs();
                Toast.makeText(getActivity(), "Logged in as " + u.getName() + "!", Toast.LENGTH_SHORT).show();
            }
            SetText();

            startActivity(new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Override
        public void failure(RetrofitError error) {
            Log.i(TAG, "Guest Callback Fail");
            Log.i("Error: ", error.toString());
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
