/**
 *  RootMemberModel.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */

package com.onesound.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RootMemberModel {

    @SerializedName("paging")
    private Object mPaging;

    @SerializedName("results")
    private List<User> mResults;

    public List<User> getResults() { return mResults; }

    public Object getPaging() { return mPaging; }


}
