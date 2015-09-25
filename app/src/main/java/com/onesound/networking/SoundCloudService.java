/**
 *  SoundCloudService.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onesound.KEYS;
import com.onesound.models.SoundCloudSong;

import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class SoundCloudService {

    /**
     * A instance of this class for the singelton
     */
    public final static SoundCloudService INSTANCE = new SoundCloudService();
    /**
     * Soundcloud interface
     */
    private ISoundCloudService mSCC = null;

    /**
     * Private constructor to make it a singleton class
     */
    private SoundCloudService() {

        // New gson object
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .serializeNulls()
                .create();

        // the base url for the soundcloud api
        String mBASEURL = "https://api.soundcloud.com";

        // Retrofit adapter for the
        mSCC = new RestAdapter.Builder()
                .setEndpoint(mBASEURL)
                .setConverter(new GsonConverter(gson))
//                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()
                .create(ISoundCloudService.class);
    }

    /**
     * Search for a favorite from the soundcloud api
     * @param q string query which will be whatever the user enters in the search box
     */
    public void searchForSong(String q, Callback<List<SoundCloudSong>> cb) {
        HashMap<String,String> options = new HashMap<>();
        options.put("client_id", KEYS.SOUNDCLOUD_CLIENT_ID);
        options.put("q",q);
        options.put("filter", "streamable");
        options.put("order", "hotness");
        options.put("duration-to", "600,000");
        options.put("limit", "25");

        mSCC.SearchSoundcloudMusic(options, cb);
    }

    /**
     * Search for a favorite from the soundcloud api
     * @param q string query which will be the title of the song
     * @param cb callback of a list of songs returned from the search, in this case one song
     */
    public void searchForFavorite(String q, Callback<List<SoundCloudSong>> cb) {
        // use a hash to store all optional soundcloud parameters
        HashMap<String,String> options = new HashMap<>();

        options.put("client_id", KEYS.SOUNDCLOUD_CLIENT_ID);
        options.put("q",q);
        options.put("filter", "streamable");
        options.put("order", "hotness");
        options.put("duration-to", "600,000");
        options.put("limit", "1");

        mSCC.SearchSoundcloudMusic(options, cb);
    }
}
