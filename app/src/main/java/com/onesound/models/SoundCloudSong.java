/**
 *  SoundCloudSong.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.models;

import com.google.gson.annotations.SerializedName;
import com.onesound.managers.PartyManager;

public class SoundCloudSong {

    @SerializedName("id")
    private int mExternalID;

    @SerializedName("duration")
    private int mLength;

    @SerializedName("user")
    private SoundCloudUser mArtist;

    @SerializedName("artwork_url")
    private String mAlbum;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("streamable")
    private boolean mStreamable;

    public int getExternalID() {
        return mExternalID;
    }

    public void setExternalID(int id) {
        this.mExternalID = id;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        this.mLength = length;
    }

    public SoundCloudUser getArtist() {
        return mArtist;
    }

    public void setArtist(SoundCloudUser artist) {
        this.mArtist = artist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String mAlbum) {
        this.mAlbum = mAlbum;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public boolean isStreamable() {
        return mStreamable;
    }

    public void setStreamable(boolean s) {
        this.mStreamable = s;
    }

    public class SoundCloudUser {

        @SerializedName("username")
        private String mUserName;

        public String getUserName() {
            return mUserName;
        }

        public void setUserName(String username) {
            this.mUserName = username;
        }
    }

    public Song convertToSong() {
        Song song = new Song();

        // if the string has the soundcloud default value of large, we need to replace it
        // with t500x500 which is the api url paramater for large
        if(this.mAlbum != null) song.setAlbum(this.mAlbum.replace("large", "t500x500"));

        // the source is soundcloud
        song.setSource("sc");

        // songs on sc stored in milliseconds, convert to seconds
        song.setLength(this.mLength / 1000);

        song.setTitle(this.mTitle);
        song.setPID(PartyManager.INSTANCE.getPID());
        song.setArtist(this.getArtist().getUserName());
        song.setExternalID(Integer.toString(this.mExternalID));

        return song;
    }
}
