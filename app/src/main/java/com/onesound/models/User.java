/**
 *  User.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 12/16/14.
 *  Copyright (c) 2014 OneSound LLC. All rights reserved.
 *
 *  This class models a user
 *
 */

package com.onesound.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("name")
    private String mName = null;            /// users name

    @SerializedName("color")
    private String mColor = null;            /// color the user picks

    @SerializedName("uid")
    private int mID = 0;                    /// OneSound id

    @SerializedName("guest")
    private boolean mGuest = true;          /// flag for guest account

    @SerializedName("photo")
    private String mPhotoURL = null;        /// url for the profile pic

    @SerializedName("song_count")
    private int mSongCount = 0;             /// number of songs played( displayed in profile)

    @SerializedName("points")
    private int mHotnessPercent = 0;        /// hotness percentage

    @SerializedName("access_token")
    private String mOneSoundToken = null;   /// OneSound access token

    @SerializedName("vote_count")
    private  int mVoteCount = 0;            /// number of song up votes

    @SerializedName("token")
    private String mFacebookToken = null;   /// Facebook access token

    @SerializedName("following")
    private int mFollowing = 0;             /// number of people following

    @SerializedName("party_pid")
    protected int mPID = 0;                 /// id of the party the user is in

    @SerializedName("status")               /// status for a network call
    private String mStatus = null;

    @SerializedName("message")              /// error message for network call
    private String mMessage = null;

    @SerializedName("active")
    private boolean mActive = false;

    @SerializedName("valid")
    private boolean mValid = false;

    private boolean mInParty = false;

    private Color colorSelected = Color.Red;

    public Color getColorSelected() {
        return colorSelected;
    }

    public enum Color{ Yellow, Orange, Purple, Terqouise, Red }

    /*
     *  return the user as a string for debugging
     */
    public String toString() {
        return "UserID: " + mID +", name: " + mName + "Guest: " + mGuest + ", color: " + mColor;
    }

    /**
     *   Getters and setters for all member variables
     */
    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

   public String getColor() {
        return mColor;
    }

    public void setColor(String color) {
        this.mColor = color;
        // Set color code based on the color
        if (color == null) return;
        switch (mColor.charAt(0)) {
            case 'r':
                colorSelected = Color.Red;
                break;
            case 'p':
                colorSelected = Color.Purple;
                break;
            case 't':
                colorSelected = Color.Terqouise;
                break;
            case 'y':
                colorSelected = Color.Yellow;
                break;
            case 'o':
                colorSelected = Color.Orange;
                break;
            case 'g':
                colorSelected = Color.Terqouise;
                break;
            default:
                colorSelected = Color.Red;
                break;
        }
    }

    public int getID() {
        return mID;
    }

    public void setID(int mID) {
        this.mID = mID;
    }

    public boolean isGuest() {
        return mGuest;
    }

    public void setGuest(boolean mGuest) {
        this.mGuest = mGuest;
    }

    public String getPhotoURL() {
        return mPhotoURL;
    }

    public void setPhotoURL(String mPhotoURL) {
        this.mPhotoURL = mPhotoURL;
    }

    public int getSongCount() {
        return mSongCount;
    }

    public void setSongCount(int mSongCount) {
        this.mSongCount = mSongCount;
    }

    public int getVoteCount() {
        return mVoteCount;
    }

    public void setVoteCount(int mUpVoteCount) {
        this.mVoteCount = mUpVoteCount;
    }

    public int getHotnessPercent() {
        return mHotnessPercent;
    }

    public void setHotnessPercent(int mHotnessPercent) {
        this.mHotnessPercent = mHotnessPercent;
    }

    public int getFollowing() {
        return mFollowing;
    }

    public void setFollowing(int mFollowing) {
        this.mFollowing = mFollowing;
    }

    public String getFacebookToken() {
        return mFacebookToken;
    }

    public void setFacebookToken(String mFacebookToken) {
        this.mFacebookToken = mFacebookToken;
    }

    public String getOneSoundToken() {
        return mOneSoundToken;
    }

    public void setOneSoundToken(String mOneSoundToken) {
        this.mOneSoundToken = mOneSoundToken;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getMessage() {
        return mMessage;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean a){mActive = a;}

    public int getPID() { return mPID; }

    public void setPID(int pid) {
        mPID = pid;
        if (pid == 0) setInParty(false);
        else setInParty(true);
    }

    public boolean inParty() { return mInParty; }

    protected void setInParty(boolean party) {
        mInParty = party; }
}
