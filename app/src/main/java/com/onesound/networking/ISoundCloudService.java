/**
 *  SoundCloudClient.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 12/17/14.
 *  Copyright (c) 2014 OneSound LLC. All rights reserved.
 *
 *  This interface is for all soundcloud retrofit network calls
 */

package com.onesound.networking;

import com.onesound.models.SoundCloudSong;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface ISoundCloudService {

    /**
     *  Search for a soundcloud song with a string query
     */
    @GET("/tracks.json")
    void SearchSoundcloudMusic(@QueryMap Map <String,String> options, Callback<List<SoundCloudSong>> cb);

}
