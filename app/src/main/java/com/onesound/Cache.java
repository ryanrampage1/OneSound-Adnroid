package com.onesound;

import com.onesound.models.Favorite;
import com.onesound.models.Song;

import java.util.ArrayList;

/**
 * Created by ryan on 9/18/15.
 *
 * This class is created to cache objects while the app is running. This cuts down the number
 * of network calls needed
 *
 */
public class Cache {

    /**
     * Singleton instance of the cache
     */
    public static final Cache INSTANCE = new Cache();

    /**
     * Arraylist to cache favorites to prevent network calls while app runs
     */
    private ArrayList<Favorite> favorites;

    /**
     * Reference to the current song playing
     */
    private Song nowPlaying;

    private boolean refreshFavorites = false;

    public boolean isRefreshFavorites() {
        return refreshFavorites;
    }

    public Cache setRefreshFavorites(boolean refreshFavorites) {
        this.refreshFavorites = refreshFavorites;
        return this;
    }

    /**
     * Constructor is private for singleton pattern
     */
    private Cache() {
    }

    public ArrayList<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(ArrayList<Favorite> favorites) {
        this.favorites = favorites;
    }

    public Song getNowPlaying() {
        return nowPlaying;
    }

    public void setNowPlaying(Song nowPlaying) {
        this.nowPlaying = nowPlaying;
    }

    /**
     * Clear all cached objects
     */
    public void clear() {
        nowPlaying = null;
        favorites = null;
    }
}
