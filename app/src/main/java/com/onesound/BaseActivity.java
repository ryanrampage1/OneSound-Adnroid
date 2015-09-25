/**
 *  BaseActivity.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound;

import android.support.v7.app.AppCompatActivity;

import rx.subscriptions.CompositeSubscription;

public class BaseActivity extends AppCompatActivity {

    /**
     * Add all observables and unsubscribe on destroy to prevent memory leaks
     */
    protected CompositeSubscription compositeSubscription = new CompositeSubscription();
    protected boolean registerEventBus = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (registerEventBus) EventBus.getDefault().register(this);
//    }
}
