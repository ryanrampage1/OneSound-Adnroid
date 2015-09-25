/**
 *  IApiService.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 12/16/14.
 *  Copyright (c) 2014 OneSound LLC. All rights reserved.
 *
 *  This is an interface class for retrofit to work with all OneSound
 *  networking calls
 *
 */

package com.onesound.networking;

import com.onesound.models.GsonMockClasses;
import com.onesound.models.Party;
import com.onesound.models.RootFavoriteModel;
import com.onesound.models.RootMemberModel;
import com.onesound.models.RootPlaylistModel;
import com.onesound.models.SimpleStatusModel;
import com.onesound.models.Song;
import com.onesound.models.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Interface for all retrofit calls made in the app to the onesound server
 */
public interface IAPIService {

    @GET("/guest")
    void getGuest(Callback<User> cb);

    @GET("/login/guest")
    void loginGuest(Callback<User> cb);

    @GET("/login/facebook")
    void logInUser(@Query("token") String token, Callback<User> cb);

    @DELETE("/song/{sid}")
    void deleteSong(@Path("sid") int s, Callback<Object> cb);

    @POST("/song/{sid}/upvote")
    void upVoteSong(@Path("sid") int sid, Callback<Object> cb);

    @POST("/song/{sid}/downvote")
    void downVoteSong(@Path("sid") int sid, Callback<Object> cb);

    @POST("/song")
    void addSongToParty(@Body Song song, Callback<Object> cb);

    @DELETE("/song/{sid}/vote")
    void removeVote(@Path("sid") int sid, Callback<Object> cb);

    @GET("/user/{uid}/favorites")
    void getFavorites(@Path("uid") int uid, Callback<RootFavoriteModel> cb);

    // RX

    @GET("/public/info")
    Observable<GsonMockClasses.VersionCallback> isValid();

    @GET("/party/current")
    Observable<Party> getParty();

    @GET("/guest")
    Observable<User> getGuest();

    @GET("/login/guest")
    Observable<User> loginGuest();

    @GET("/login/facebook")
    Observable<User> logInUser(@Query("token") String token);

    @GET("/party/search/nearby")
    Observable<List<Party>> getNearbyParties( @Query("latitude") double lat, @Query("longitude") double lon);

    @GET("/party/search")
    Observable<List<Party>> searchForParty(@Query("q") String q);

    @GET("/party/{pid}")
    Observable<Party> getParty(@Path("pid") int pid);

    @PUT("/user/{uid}")
    Observable<Object> updateUser(@Path("uid") int uid, @Body GsonMockClasses.LocContainenr u);

    @GET("/party/{pid}/playlist")
    Observable<RootPlaylistModel> getPartyPlaylist(@Path("pid") int pid);

    @DELETE("/user/{uid}/party")
    Observable<SimpleStatusModel> leaveParty(@Path("uid") int uid);

    @PUT("/party/{pid}/permissions")
    Observable<Object> getMusicControl(@Path("pid") int pid, @Body GsonMockClasses.MusicControl m);

    @PUT("/party/{pid}")
    Observable<Party> updateParty(@Path("pid") int pid, @Body GsonMockClasses.UpdateParty  p);

    @POST("/party")
    Observable<Party> createParty(@Body GsonMockClasses.UpdateParty p);

    @POST("/song/{sid}/favorite")
    Observable<Object> addFavorite(@Path("sid") int sid);

    @POST("/song/{sid}/upvote")
    Observable<Object> upVoteSong(@Path("sid") int sid);

    @GET("/party/{pid}/nextsong")
    Observable<Song> getNextSong(@Path("pid") int pid);

    @GET(("/user/name/validate"))
    Observable<GsonMockClasses.usernameCallback> checkUsername(@Query("name") String name);

    @GET("/party/{pid}/members")
    Observable<RootMemberModel> getPartyMembers(@Path("pid") int pid);

    @PUT("/user/{uid}")
    Observable<User> updateUser(@Path("uid") int uid, @Body GsonMockClasses.EditUser u);

    @POST("/user/facebook")
    Observable<User> createUser(@Body GsonMockClasses.EditUser u);
}
