/**
 *  UserManager.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 12/22/14.
 *  Copyright (c) 2014 OneSound LLC. All rights reserved.
 *
 *  This class represents the singelton of the
 *  local user
 *
 *  Extends the user class so has all avalible fields
 *  and functions
 *
 */

package com.onesound.managers;

import com.onesound.models.User;

public class UserManager extends User {

    /** The singelton local user object  */
    public static final UserManager INSTANCE = new UserManager();

    /** enum for the user state */
    public enum USER_STATE {STREAMABLE, NOSTREAM, MEMBER}

    /** the current state of the user
     *  set to member as default */
    private USER_STATE mUSERState = USER_STATE.MEMBER;

    /**
     *
     * @return if the user has stream control based on user state
     */
    public boolean isStreamable() {
        return mUSERState == USER_STATE.STREAMABLE;
    }

    public boolean isHost() {
        return mUSERState != USER_STATE.MEMBER;
    }


    /**
     *
     * @return if the user is logged in or not
     */
     public boolean isLogginComplete() {
        return mLogginComplete;
    }

    /**
     * Flag for showing the gain music stream dialog in party fragment
     */
    private boolean showGainStreamDialog = true;

    public void setShowGainStreamDialog(boolean b){showGainStreamDialog = b;}
    public boolean isShowGainStreamDialog(){return showGainStreamDialog;}

    /** user login status */
    private boolean mLogginComplete = false;

    /** private constructor so it cant be created anywhere else */
    private UserManager() {}

    /**
     *  Set the user manager based on a user object
     * @param user to set the manager to
     */
    public void SetUser(User user){
        setVoteCount(user.getVoteCount());
        setName(user.getName());
        setColor(user.getColor());
        setPhotoURL(user.getPhotoURL());
        setGuest(user.isGuest());
        setSongCount(user.getSongCount());
        setOneSoundToken(user.getOneSoundToken());
        setHotnessPercent(user.getHotnessPercent());
        setID(user.getID());
        setActive(user.isActive());
        setInParty(user.getPID() > 0);
        if (user.getPID() > 0) PartyManager.INSTANCE.setPID(user.getPID());
        mLogginComplete = true;
    }

    /**
     * Set the state of the user
     * @param host if the user is the host of the party
     * @param musicControl if the user has music control
     */
    public void SetUserState(boolean host, boolean musicControl){
         if(host){
            if (musicControl)
                SetState(USER_STATE.STREAMABLE);
            else
                SetState(USER_STATE.NOSTREAM);
        }
        else
             SetState(USER_STATE.MEMBER);
    }

    /**
     *  Set the state of the user
     * @param state to set the user to
     */
    public void SetState(USER_STATE state){
        mUSERState = state;
    }

    /**
     * Getter for the user state
     * @return user state
     */
    public USER_STATE getState(){return mUSERState;}

}
