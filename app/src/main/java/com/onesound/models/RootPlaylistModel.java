/**
 *  RootPlaylistModel.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RootPlaylistModel {

    @SerializedName("paging")
    private Object mPaging;

    @SerializedName("results")
    private List<Song> mResults;

    public List<Song> getResults() { return mResults; }

    public Object getPaging() { return mPaging; }


}
