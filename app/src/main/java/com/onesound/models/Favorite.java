/**
 *  Favorite.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */

package com.onesound.models;

import com.google.gson.annotations.SerializedName;

public class Favorite {

    public Favorite() { }

    public Favorite(Favorite f){
        source = f.getSource();
        externalID = f.getExternalID();
        PID = f.getPID();
        artist = f.getArtist();
        albumArtURL = f.getAlbumArtURL();
        title = f.getTitle();
    }

    @SerializedName("source")
    private String source;

    @SerializedName("external_id")
    private String externalID;

    @SerializedName("party_pid")
    private int PID;

    @SerializedName("artist")
    private String artist;

    @SerializedName("album")
    private String albumArtURL;

    @SerializedName("title")
    private String title;


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumArtURL() {
        return albumArtURL;
    }

    public void setAlbumArtURL(String albumArtURL) {
        this.albumArtURL = albumArtURL;
    }
}
