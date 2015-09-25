/**
 *  Party.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 *  This class models a party
 *
 */

package com.onesound.models;

import com.google.gson.annotations.SerializedName;

public class Party {

    @SerializedName("pid")
    private int mPID;

    @SerializedName("name")
    private String mName;

    @SerializedName("privacy")
    private boolean mPrivate;

    @SerializedName("strictness")
    private int mStrictness;

    @SerializedName("host_name")
    private String mHost;

    @SerializedName("host")
    private boolean host;

    @SerializedName("status")
    private String mStatus;

    @SerializedName("member_count")
    private int mMemberCount;

    @SerializedName("current_song")
    private Song mCurrentSong;

    @SerializedName("distance")
    private double distance;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public class HostInfo{
        @SerializedName("music_control")
        boolean musicControl;

        @SerializedName("skip_song")
        boolean skipSong;

        public boolean isMusicControl() {
            return musicControl;
        }

        public void setMusicControl(boolean musicControl) {
            this.musicControl = musicControl;
        }

        public boolean isSkipSong() {
            return skipSong;
        }

        public void setSkipSong(boolean skipSong) {
            this.skipSong = skipSong;
        }
    }

    @SerializedName("host_info")
    HostInfo hostInfo = new HostInfo();

    public boolean isSkip(){return hostInfo.isSkipSong();}
    public boolean isMusicControl(){return hostInfo.isMusicControl();}
    public void setMusicControl(boolean b){
//        if(hostInfo == null)
//            hostInfo =
        hostInfo.setMusicControl(b);
    }



    public Song getCurrentSong() {
        return mCurrentSong;
    }

    public void setCurrentSong(Song mCurrentSong) {
        this.mCurrentSong = mCurrentSong;
    }

    public int getPID() {
        return mPID;
    }

    public void setPID(int mPID) {
        this.mPID = mPID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public boolean isPrivate() {
        return mPrivate;
    }

    public void setPrivate(boolean mPrivate) {
        this.mPrivate = mPrivate;
    }

    public int getStrictness() {
        return mStrictness;
    }

    public void setStrictness(int mStrictness) {
        this.mStrictness = mStrictness;
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String host) {
        this.mHost = host;
    }


    public String getStatus() {
        return mStatus;
    }

    public int getMemberCount() {
        return mMemberCount;
    }

    public boolean isHost() {
        return host;
    }

    public void setHost(boolean host) {
        this.host = host;
    }
}


