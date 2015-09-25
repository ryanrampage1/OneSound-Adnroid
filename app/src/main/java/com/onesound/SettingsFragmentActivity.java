package com.onesound;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by ryan on 8/19/15.
 *
 * This is an activity that holds and displays the settings fragment
 */
public class SettingsFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_fragment_activity);
    }
}
