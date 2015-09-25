/**
 *  NearPartiesActivity.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.UI;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.onesound.BaseActivity;
import com.onesound.PartySettingsActivity;
import com.onesound.R;
import com.onesound.SettingsFragmentActivity;
import com.onesound.UI.adapters.NearbyResultAdapter;
import com.onesound.Utility;
import com.onesound.managers.PartyManager;
import com.onesound.managers.UserManager;
import com.onesound.models.GsonMockClasses;
import com.onesound.models.Party;
import com.onesound.networking.APIService;
import com.onesound.networking.LocationHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class NearPartiesActivity extends BaseActivity {

    /**
     * Option added to bundle when leaving activity to tell the main activity that the user
     * selected to not join a party from this page
     */
    @SuppressWarnings("WeakerAccess")
    public static final String NONE = "NONE";

    /**
     * Option added to bundle when leaving activity to tell the main activity that the user
     * selected to create a party
     */
    @SuppressWarnings("WeakerAccess")
    public static final String CREATE = "CREATE";

    /**
     * Option added to bundle when leaving activity to tell the main activity that the user
     * selected to join an existing party
     */
    @SuppressWarnings("WeakerAccess")
    public static final String EXISTING = "EXISTING";

    /**
     * Tag for the option added to bundle when leaving activity to tell the main activity
     * what the user selected
     */
    @SuppressWarnings("WeakerAccess")
    public static final String TYPE = "TYPE";

    /**
     * Listview that displays the nearby parties
     */
    @Bind(R.id.parties) ListView nearbyPartiesListView;

    /**
     * Textview that shows there are no nearby parties
     */
    @Bind(R.id.noPartiesText) TextView noPartiesTextView;

    /**
     * Imageview that holds the logo
     */
    @Bind(R.id.logo) ImageView logo;

    /**
      * Textview that holds the name that a user is searching for
     */
    @Bind(R.id.partyName) EditText partyName;

    /**
     * Profilepic of the user. If clicked on it will go to their profile
     */
    @Bind(R.id.profilePic) ImageView pic;

    /**
     *  Holds all search view elements, animates up when selected to search for party
     */
    @Bind(R.id.search_box ) LinearLayout searchBox;

    /**
     * Root layout used to set padding so views go into status bar at top
     */
    @Bind(R.id.rootLayout) View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_parties);

        ButterKnife.bind(this);

        // check the users android version, if it lollipop or greter, color the statusbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // Enable status bar translucency
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            rootView.setPadding(0, Utility.getStatusBarHeight(this), 0, 0);
        }

        partyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // while searching keep updating the list of results
                compositeSubscription.add(APIService.INSTANCE.getRestAdapter().searchForParty(s.toString())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<Party>>() {
                            @Override
                            public void call(List<Party> parties) {
                                NearbyResultAdapter adapter = new NearbyResultAdapter(NearPartiesActivity.this, R.layout.party_search_item, parties);
                                nearbyPartiesListView.setAdapter(adapter);
                                ToggleListOn(true);
                            }
                        }));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // onfocus listener to make the keyboard appear when the user clicks on the search for
        // party edit text box
        partyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v == partyName) {
                    if (hasFocus) {
                        // hide the logo and profile pic
                        logo.setVisibility(View.GONE);
                        pic.setVisibility(View.GONE);

                        // change the margin top to 10dp on the search box
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(Utility.DIPtoPIXLE(getBaseContext(), 50), Utility.DIPtoPIXLE(getBaseContext(), 20), Utility.DIPtoPIXLE(getBaseContext(), 50), 0);
                        searchBox.setLayoutParams(params);

                        // Open keyboard
                        ((InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(partyName, InputMethodManager.SHOW_FORCED);
                    } else {
                        logo.setVisibility(View.VISIBLE);
                        pic.setVisibility(View.VISIBLE);

                        // Close keyboard
                        ((InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(partyName.getWindowToken(), 0);
                    }
                }
            }
        });

        partyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditTextFocus(true);
            }
        });




        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user wants to go to profile
                ExitNearParties(MainActivity.PROFILE);
            }
        });

        // load the profile picture into the imageview
        if (UserManager.INSTANCE.isGuest()) {
            try {
                Glide.with(this)
                        .load(R.drawable.defaultuserimageformainparty)
                        .transform(new Utility.RoundedCorner(this, 90, 0))
                        .into(pic);
            } catch (Exception e) {
                pic.setImageDrawable(this.getResources().getDrawable(R.drawable.defaultuserimageformainparty));
            }
        } else {
            try {
                Glide.with(this)
                        .load(UserManager.INSTANCE.getPhotoURL())
                        .transform(new Utility.RoundedCorner(this, 90, 0))
                        .into(pic);
            } catch (Exception e) {
                pic.setImageDrawable(this.getResources().getDrawable(R.drawable.defaultuserimageformainparty));
            }
        }

        // location helper for getting the location
        LocationHelper locationHelper = new LocationHelper(this);
        // get the users last known location
        Location loc = locationHelper.getLocation();
        if (loc != null) {
            final Double lon = loc.getLongitude();
            final Double lat = loc.getLatitude();

            // create a location object to pass to the update user location call
            GsonMockClasses.LocContainenr location = new GsonMockClasses.LocContainenr();
            location.loc.latitude = lat;
            location.loc.longitude = lon;

            // update the users location and get nearby parties
            compositeSubscription.add(APIService.INSTANCE.getRestAdapter().updateUser(UserManager.INSTANCE.getID(), location)
                    .flatMap(new Func1<Object, Observable<List<Party>>>() {
                        @Override
                        public Observable<List<Party>> call(Object o) {
                            return APIService.INSTANCE.getRestAdapter().getNearbyParties(lat, lon);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Party>>() {
                        @Override
                        public void call(List<Party> parties) {
                            if (parties.size() < 1) {
                                // no parties returned, show no parties found text
                                ToggleListOn(false);
                            } else {
                                // parties returned, create a new adapter for the results so the can
                                NearbyResultAdapter adapter = new NearbyResultAdapter(NearPartiesActivity.this, R.layout.nearby_item, parties);
                                nearbyPartiesListView.setAdapter(adapter);

                                // show the list and hide the no parites text
                                ToggleListOn(true);
                            }
                        }
                    }));

        } else {
            Toast.makeText(this, "Error getting locaiton", Toast.LENGTH_SHORT).show();
            ToggleListOn(false);
        }

        locationHelper.unregisterListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    /**
     * Exits the activity with an action for the main activity to complete
     *
     * @param type type of action the main activity must do
     */
    private void ExitNearParties(String type) {
        Intent start = new Intent(this, MainActivity.class);
        // add the type to the bundle
        start.putExtra(TYPE, type);

        // hide the keyboard
        ((InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(partyName.getWindowToken(), 0);

        // star the activity
        this.startActivity(start);
    }

    /**
     * Sets if the textbox has focus or not
     *
     * @param isFocused flag for if focused or not
     */
    private void setEditTextFocus(boolean isFocused) {
        partyName.setCursorVisible(isFocused);
        partyName.setFocusable(isFocused);
        partyName.setFocusableInTouchMode(isFocused);

        if (isFocused) {
            partyName.requestFocus();
        }
    }

    /**
     * Toggles if the nearby party listview is shown or not
     *
     * @param b flag for is the listview is shown or not
     */
    private void ToggleListOn(boolean b) {
        if (b) {
            noPartiesTextView.setVisibility(View.GONE);
            nearbyPartiesListView.setVisibility(View.VISIBLE);
        } else {
            noPartiesTextView.setVisibility(View.VISIBLE);
            nearbyPartiesListView.setVisibility(View.GONE);
        }
    }

    /**
     * onClick function for the skip text
     */
    @OnClick(R.id.skip)
    public void skip(){ ExitNearParties(NONE); }

    /**
     * Onclick function for the start party button
     */
    @OnClick(R.id.start_party)
    public void startParty() {
        // person wants to create a party
        if (!UserManager.INSTANCE.isGuest()) {
            Intent intent = new Intent(NearPartiesActivity.this, PartySettingsActivity.class);
            intent.putExtra(MainActivity.ACTION, MainActivity.CREATE);
            startActivity(intent);
        } else {
            Intent intent = new Intent(NearPartiesActivity.this, SettingsFragmentActivity.class);
            startActivity(intent);
            Toast.makeText(NearPartiesActivity.this, "Must login to create a party!", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Eventbus method for clicks on party items in the nearby party listview adapter. this will
     * join the party and enter the main activity
     *
     * @param partyID id of the party the user wants to join
     */
    public void onEvent(Integer partyID) {
        compositeSubscription.add(APIService.INSTANCE.getRestAdapter().getParty(partyID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Subscriber<Party>() {
                            @Override
                            public void onCompleted() {
                                // exit the activity with status existing
                                ExitNearParties(EXISTING);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getBaseContext(), "Error Joining Party, Please Try again", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNext(Party p) {
                                if (p != null) {
                                    // set the party manager
                                    PartyManager.INSTANCE.SetParty(p);
                                }
                            }
                        }));
    }

}
