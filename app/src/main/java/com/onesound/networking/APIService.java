/**
 *  APIService.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.networking;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onesound.BuildConfig;
import com.onesound.KEYS;
import com.onesound.managers.UserManager;
import com.onesound.models.RootFavoriteModel;
import com.onesound.models.Song;
import com.onesound.models.User;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

public class APIService {

    /**
     * Singleton instance of the api service
     */
    public final static APIService INSTANCE = new APIService();

    /**
     * Tag for logging
     */
    private static final String TAG = "ApiService";

    /**
     * Retrofit network call interface
     */
    private IAPIService apiService;

    /**
     * Shared preferences object for editing user token shared prefs
     */
    private SharedPreferences prefs;

    private APIService() {

        // create gson object
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .create();

        // add headers to any network requests made
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("CLIENT_ID", "9");
                request.addHeader("CLIENT_SECRET", KEYS.ONESOUND_CLIENT_ID);
                if (UserManager.INSTANCE.getOneSoundToken() != null &&
                        !UserManager.INSTANCE.getOneSoundToken().isEmpty())
                    request.addHeader("ACCESS_TOKEN", UserManager.INSTANCE.getOneSoundToken());
            }
        };

        // request base url
        String serverAPI = "https://sparty.onesoundapp.com";

        // create Retrofit rest adapter
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(serverAPI)
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .build();
        apiService = restAdapter.create(IAPIService.class);
    }

    /**
     * Logout From Facebook
     */
    private static void facebookLogout() {
        LoginManager.getInstance().logOut();
    }

    public IAPIService getRestAdapter() {
        return apiService;
    }

    /**
     * Set the shared prefs for the api service
     *
     * @param context to get the shared prefs
     */
    public void initilizeSharedPrefs(Context context) {
        // initalize the share preference objects
        prefs = context.getSharedPreferences("com.onesound.app", Context.MODE_PRIVATE);
    }

    public void getGuest() {
        facebookLogout();

        Callback<User> GuestUserCallBack = new Callback<User>() {
            @Override
            public void success(User u, retrofit.client.Response response) {
                if (u != null) {
                    // set all local user info based on guest account
                    UserManager.INSTANCE.SetUser(u);

                    setSharedPrefs();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.toString());
            }
        };
        apiService.getGuest(GuestUserCallBack);

    }

    public void getGuest(Callback<User> cb) {
        apiService.getGuest(cb);
    }

    public void loginGuest(Callback<User> cb) {
        apiService.loginGuest(cb);

    }

    public void logInUser(String fbToken, Callback<User> cb) {
        apiService.logInUser(fbToken, cb);
    }


    public void upVoteSong(Callback<Object> UpVoteCallback, final int sid) {

        apiService.upVoteSong(sid, UpVoteCallback);

    }

    public void downVoteSong(Callback<Object> DownVoteCallback, final int sid) {
        apiService.downVoteSong(sid, DownVoteCallback);

    }

    public void removeVote(Callback<Object> RemoveVoteCallback, final int sid) {
        apiService.removeVote(sid, RemoveVoteCallback);
    }

    /**
     * Allow you to set the shared preferences from anywhere in the app
     */
    public void setSharedPrefs() {
        prefs.edit()
                .putBoolean("Guest", false)
                .putString("AccessToken", UserManager.INSTANCE.getOneSoundToken())
                .apply();
    }

    public void addSongToParty(Song song, Callback<Object> cb) {
        apiService.addSongToParty(song, cb);
    }

    public void deleteSong(int sid, Callback<Object> cb) {
        apiService.deleteSong(sid, cb);
    }

    public void getFavorites(int uid, Callback<RootFavoriteModel> cb) {
        apiService.getFavorites(uid, cb);
    }

}


