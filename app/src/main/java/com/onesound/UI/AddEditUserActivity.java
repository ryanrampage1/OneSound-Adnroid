/**
 * AddEditUserActivity.Java
 * OneSound
 * <p/>
 * Created by Ryan Casler on 12/28/14.
 * Copyright (c) 2014 OneSound LLC. All rights reserved.
 * <p/>
 * Add user fragment that displays the
 */

package com.onesound.UI;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.onesound.BaseActivity;
import com.onesound.R;
import com.onesound.managers.UserManager;
import com.onesound.models.GsonMockClasses;
import com.onesound.models.User;
import com.onesound.networking.APIService;
import com.onesound.networking.LocationHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddEditUserActivity extends BaseActivity {

    @Bind(R.id.lettercount) TextView count;
    @Bind(R.id.partyname) EditText djName;
    @Bind(R.id.spinner) Spinner spinner;
    private String[] colors;

    private String mSelectedColor;
    private String mDjName;
    private String action;
    private boolean validName = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_fragment);

        ButterKnife.bind(this);

        action= getIntent().getStringExtra(MainActivity.ACTION);

        colors = getResources().getStringArray(R.array.color_choices);


        if (!action.equals(MainActivity.EDIT)) {
            TextView tv = (TextView) findViewById(R.id.namelable);
            tv.setText("Create Account");
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.color_choices, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // set the selected color in form that is taken by she local user setter
                mSelectedColor = String.valueOf(colors[i].charAt(0)).toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing, must override method
            }
        });

        djName.setText(UserManager.INSTANCE.getName());
        djName.setTextColor(Color.parseColor("#669900"));
        djName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mDjName = s.toString();
                SetCountColor(count, s.toString());
                count.setText(Integer.toString(mDjName.length()) + "/15");
                APIService.INSTANCE.getRestAdapter().checkUsername(mDjName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<GsonMockClasses.usernameCallback>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(GsonMockClasses.usernameCallback usernameCallback) {
                                if (usernameCallback.valid || djName.getText().toString().equals(UserManager.INSTANCE.getName())) {
                                    // username is valid or current users name
                                    djName.setTextColor(getResources().getColor(R.color.green_text));
                                    validName = true;
                                } else if (!usernameCallback.valid) {
                                    // username is taken
                                    djName.setTextColor(getResources().getColor(R.color.red_text));
                                    validName = false;
                                }
                            }
                        });
            }
        });

        // set the dj name with initial content
        mDjName = djName.getText().toString();
        // set the count and color correctly on start
        count.setText(Integer.toString(mDjName.length()) + "/15");
        SetCountColor(count, mDjName);

    }
    @OnClick(R.id.submitbutton)
    public void submitClicked(){
        if (mDjName.length() < 3) {
            Toast.makeText(AddEditUserActivity.this,
                    "DJ name not long enough. Enter longer name.", Toast.LENGTH_LONG).show();
        } else if (!validName) {
            Toast.makeText(AddEditUserActivity.this,
                    "DJ Name is Taken", Toast.LENGTH_LONG).show();
        } else {
            GsonMockClasses.EditUser u = new GsonMockClasses.EditUser();
            u.mName  =mDjName;
            u.mColor = mSelectedColor;
            u.mOneSoundToken = UserManager.INSTANCE.getOneSoundToken();
            u.mFacebookToken = UserManager.INSTANCE.getFacebookToken();
            u.mID = UserManager.INSTANCE.getID();
            LocationHelper locationHelper = new LocationHelper(AddEditUserActivity.this);
            Location loc = locationHelper.getLocation();
            if (loc!= null) {
                u.location.latitude = loc.getLatitude();
                u.location.longitude = loc.getLongitude();

            }

            if (action.equals(MainActivity.EDIT)) {
                updateUser(u);
            } else {
                createUser(u);
            }
        }
    }
    /**
     * Set the color of a text view based on string length
     * @param tv text view to change
     * @param s string to base changes off
     */
    private void SetCountColor(TextView tv, String s) {
        if (s.length() > 12) {
            tv.setTextColor(Color.parseColor("#CC0000"));
        } else if (s.length() > 8) {
            tv.setTextColor(Color.parseColor("#FF8800"));
        } else {
            tv.setTextColor(Color.parseColor("#669900"));
        }
    }

    private void updateUser(GsonMockClasses.EditUser user){
        APIService.INSTANCE.getRestAdapter().updateUser(UserManager.INSTANCE.getID(), user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Update user error", e.toString());
                        Toast.makeText(AddEditUserActivity.this, "Error updating user", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(User user) {
                        handleUserCallback(user, "Profile Updated!");
                    }
                });
    }

    private void createUser(GsonMockClasses.EditUser user){
        APIService.INSTANCE.getRestAdapter().createUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Post New user error", e.toString());
                        APIService.INSTANCE.getGuest();
                        Toast.makeText(AddEditUserActivity.this, "Error Creating User", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }

                    @Override
                    public void onNext(User user) {
                        handleUserCallback(user, "Account Created!");
                    }
                });
    }

    private void handleUserCallback(User user, String toast) {
        UserManager.INSTANCE.SetUser(user);
        APIService.INSTANCE.setSharedPrefs();
        Toast.makeText(AddEditUserActivity.this, toast, Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

}
