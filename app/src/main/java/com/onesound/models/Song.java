/**
 *  Song.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 12/22/14.
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 *  This class models a song
 *
 */

package com.onesound.models;

import com.google.gson.annotations.SerializedName;

public class Song {

    public Song() { }

    @SerializedName("sid")
    private int mSID;               /// Song ID

    @SerializedName("title")
    private String mTitle;           /// Song name

    @SerializedName("external_id")
    private String mExternalID;     /// song External id

    @SerializedName("party_pid")
    private int mPID;               /// Id of the party the song is in

    @SerializedName("pid")
    private int mSongPID;

    @SerializedName("user_uid")
    private int mUID;               /// ID of the user who added the song

    @SerializedName("source")
    private String mSource;

    @SerializedName("length")
    private int mLength;

    @SerializedName("artist")
    private String mArtist;

    @SerializedName("album")
    private String mAlbum;

    @SerializedName("location")
    private String mLocation;

    @SerializedName("status")
    private String mStatus;

    @SerializedName("created_at")
    private String mCreatedAt;

    @SerializedName("user_vote")
    private int mUserVote;              //

    @SerializedName("vote_count")
    private int mVoteCount;             // vote count on the song

    @SerializedName("user")
    private User mUserPlaying;          // user playing the song

    private boolean favorited;

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }
    /*
     *  Getters and setters
     */

    public User getUserPlaying() {
        return mUserPlaying;
    }

    public void setUserPlaying(User mUserPlaying) {
        this.mUserPlaying = mUserPlaying;
    }

    public int getSID() {
        return mSID;
    }

    public void setSID(int mSID) {
        this.mSID = mSID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getExternalID() {
        return mExternalID;
    }

    public void setExternalID(String id) {
        this.mExternalID = id;
    }

    public int getPID() {
        return mPID;
    }

    public void setPID(int pid) {
        this.mPID = pid;
        this.mSongPID = pid;
    }

    public int getUID() {
        return mUID;
    }

    public void setUID(int uid) {
        this.mUID = uid;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int mLength) {
        this.mLength = mLength;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String mSource) { this.mSource = mSource; }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String mAlbum) {
        this.mAlbum = mAlbum;
    }

    public void setUserVote(int v) { mUserVote = v; }

    public int getUserVote() { return mUserVote; }

    public void setVoteCount(int v) { mVoteCount = v; }

    public int getVoteCount() { return mVoteCount; }

}
