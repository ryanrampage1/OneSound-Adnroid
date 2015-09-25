/**
 *  GsonMockClasses.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.models;

import com.google.gson.annotations.SerializedName;

public class GsonMockClasses {

    public static class EditUser {
        @SerializedName("location")
        public Loc location = new Loc();

        @SerializedName("name")
        public String mName = null;            /// users name

        @SerializedName("color")
        public String mColor = null;            /// color the user picks

        @SerializedName("uid")
        public int mID = 0;                    /// OneSound id

        @SerializedName("email")
        public String email = null;

        @SerializedName("access_token")
        public String mOneSoundToken = null;   /// OneSound access token

        @SerializedName("token")
        public String mFacebookToken = null;   /// Facebook access token

    }

    public static class Loc {
        @SerializedName("latitude")
        public double latitude;

        @SerializedName("longitude")
        public double longitude;
    }

    public static class LocContainenr{
            @SerializedName("location")
            public Loc loc = new Loc();
    }

    public static class CheckVersion{
        @SerializedName("version_status")
        public int version;

        @SerializedName("party_count")
        public int count;
    }

    public static class UpdateParty {
        @SerializedName("name")
        public String mName;

        @SerializedName("privacy")
        public boolean mPrivate;

        @SerializedName("strictness")
        public int mStrictness;

        @SerializedName("location")
        public Loc location = new Loc();
    }

    public class VersionCallback {
        private int version_status;
        private int party_count;

        public int getVersion_status() {
            return version_status;
        }

        public int getParty_count() {
            return party_count;
        }
    }

    /**
     * Mock class for get musiccontrol callback with gson
     */
    public static class MusicControl {
        @SuppressWarnings("unused")
        @SerializedName("music_control")
        boolean aBoolean = true;
    }

    /**
     * class for callback on boolean value because callback cannot be a primitive type
     */
    public static final class usernameCallback {
        @SuppressWarnings("unused")
        @SerializedName("valid")
        public boolean valid;
    }

}
