/**
 *  BaseFragment.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound;

import android.support.v4.widget.SwipeRefreshLayout;

import rx.subscriptions.CompositeSubscription;

public class BaseFragment extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @Override
    public void onRefresh() {

    }

    /**
     * Add all observables and unsubscribe on destroy to prevent memory leaks
     */
    protected CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeSubscription.unsubscribe();
    }
}
