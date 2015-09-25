package com.onesound;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by ryan on 4/8/15.
 *
 * Activity for setting up any application wide services such as Google analytics, facebook stetho
 * and OKHttp as well as others
 *
 */
public class MyApplication extends Application {

    //Logging TAG
    private static final String TAG = "MyApp";

    public static int GENERAL_TRACKER = 0;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    private final HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    public MyApplication() {
        super();
    }




    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.setApplicationId(KEYS.FACEBOOK_APP_ID);
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(KEYS.GOOGLE_ANALYTICS_PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}

