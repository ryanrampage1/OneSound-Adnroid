/**
 *  PartyManager.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 1/1/15.
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 *  This class represents the singelton of the
 *  local party
 *
 *  Extends the party class so has all avalible fields
 *  and functions
 *
 */

package com.onesound.managers;

import com.onesound.models.Party;
import com.onesound.models.Song;
import com.onesound.models.SoundCloudSong;
import com.onesound.models.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PartyManager extends Party {

    /** The singelton local partymanager object  */
    public static final PartyManager INSTANCE = new PartyManager();
    private List<Song> mPlaylist;
    private List<User> mMembers;
    private List<SoundCloudSong> mSongSearchResults;
    private  boolean mSet;
    private Song mNextSong;

    // Getters and setters
    public Song getNextSong() { return mNextSong; }

    public void setNextSong(Song s) { this.mNextSong = s; }

    public boolean isSet() { return mSet; }

    public void setSet(boolean mSet) { this.mSet = mSet; }

    /**
     * Private constructor so it cannot be accessed outside the class
     */
    private PartyManager() {}

    /**
     * set the party manager based on a party
     * @param p party to set the manager to
     */
    public void SetParty(Party p) {
        setPID(p.getPID());
        setName(p.getName());
        setHost(p.getHost());
        setPrivate(p.isPrivate());
        setStrictness(p.getStrictness());

        if (p.getCurrentSong() == null || p.getCurrentSong().getExternalID() == null) setCurrentSong(null); // if the external id is null the song is null
        else setCurrentSong(p.getCurrentSong());                              // else set the current song

        setHost(p.isHost());                                                  // flag for is host
        setMusicControl(p.isMusicControl());
        UserManager.INSTANCE.SetUserState(p.isHost(), p.isMusicControl());
        UserManager.INSTANCE.setPID(p.getPID());
    }

    /**
     * Sets the playlist for the party manager
     * @param playlist to set the manager playlist to
     */
    public void setPlaylist(List<Song> playlist) { mPlaylist = playlist; }

    /**
     *
     * @return the party playlist
     */
    public List<Song> getPlaylist() { return mPlaylist; }

    /**
     * Set the color of each member and assign the members to the user manager members list
     * @param members list of members to set the managers list to
     */
    public void setMembers(List<User> members) {
        if (members != null) {
            for (User u : members) {
                u.setColor(u.getColor());
            }

            // sort members by score
            Collections.sort(members, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    return rhs.getHotnessPercent() - lhs.getHotnessPercent();
                }
            });
        }

        mMembers = members;
    }

    /**
     *
     * @return party member list
     */
    public List<User> getMembers() { return mMembers; }

    /**
     * Sets the song search results from a list
     * @param results result list to set the manager results to
     */
    public void setSongSearchResults(List<SoundCloudSong> results) {
        this.mSongSearchResults = results;
    }

    /**
     * Reset all fields of the party manager
     */
    public void clear() {
        setPID(0);
        setName("");
        setHost(false);
        setPrivate(false);
        setStrictness(0);
        setCurrentSong(null);
        setHost(false); // flag for is host
        setMusicControl(false);
        setMembers(null);
        setPlaylist(null);
        UserManager.INSTANCE.setPID(0);
    }

}
