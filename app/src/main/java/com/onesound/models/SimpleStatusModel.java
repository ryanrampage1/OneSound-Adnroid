/**
 *  GsonMockClasses.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */

package com.onesound.models;

import com.google.gson.annotations.SerializedName;

public class SimpleStatusModel {

    @SerializedName("status")
    private String mStatus;

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String s) {
        this.mStatus = s;
    }
}
