/**
 *  Login.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 1/5/15.
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 *  This class modeles loggin in
 *
 */
package com.onesound.networking;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.AccessToken;
import com.onesound.managers.UserManager;
import com.onesound.models.User;

import retrofit.Callback;

public class Login {
    private final SharedPreferences prefs;
    private AccessToken accessToken;

    public Login(Context c){
        // initalize all share preference objects
        prefs = c.getSharedPreferences("com.onesound.app", Context.MODE_PRIVATE);
    }

    public void LoginUser(Callback<User> cb, String fbtoken){

        // The activity has become visible (it is now "resumed").
        String SharedPrefsS = prefs.getString("AccessToken", null);

        // get the cache session

        if (accessToken!=null && fbtoken != null){
//            Log.i(TAG, "Facebook Session Open");

            if(SharedPrefsS == null){
                /*
                 *  Case: facebook token exists, NO shared prefs exist
                 *
                 *  Action: create guest account
                 */
//                Log.i(TAG, "SharedPref user NOT found");
                // create the guest account
                APIService.INSTANCE.getGuest(cb);
            }
            else{
                /*
                 *  Case: facebook token exists, shared prefs DO exists
                 *
                 *  Action: Login user
                 */
//                Log.i(TAG, "SharedPref user IS found");
                SetUserManager(); // set the local user based on shared prefs
                // login user
                APIService.INSTANCE.logInUser(fbtoken, cb);
//                Log.i(TAG, "Login user call made");
            }
        }
        else{
//            Log.i(TAG, "Facebook Session Closed, Check for guest");
            if(SharedPrefsS == null){
                /*
                 * Case: NO facebook token exists, NO shared prefs exist
                 *
                 *  Action: creat guest account
                 */
//                Log.i(TAG, "SharedPref user NOT found");
                // create the guest account
                APIService.INSTANCE.getGuest(cb);
            }

            else{
                /*
                 * Case: NO facebook token exists, shared prefs DO exists
                 * Action: Login guest
                 */
//                Log.i(TAG, "SharedPref user IS found");
                SetUserManager();

                // login the guest account
                APIService.INSTANCE.loginGuest(cb);
            }
        }
    }


    /**
    * Set the user manager from shared prefs
    */
    private void SetUserManager(){
        UserManager.INSTANCE.setOneSoundToken(prefs.getString("AccessToken", null));
        UserManager.INSTANCE.setGuest(prefs.getBoolean("Guest", false));
    }
}
