/**
 *  RootFavoriteModel.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
public class RootFavoriteModel {

    @SerializedName("paging")
    private Object mPaging;

    @SerializedName("results")
    private List<Favorite> mResults;

    public List<Favorite> getResults() { return mResults; }

    public Object getPaging() { return mPaging; }
}
