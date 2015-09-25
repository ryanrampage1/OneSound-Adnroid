package com.onesound;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.onesound.UI.MainActivity;
import com.onesound.managers.PartyManager;
import com.onesound.models.GsonMockClasses;
import com.onesound.models.Party;
import com.onesound.networking.APIService;
import com.onesound.networking.LocationHelper;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Activity that handles both creating and editing parties. Which one is decided by an intent extra
 * passed to the activity.
 */
public class PartySettingsActivity extends AppCompatActivity {

    /**
     * Textview that shows where the entered username is with respect to the limit
     */
    private TextView count;

    /**
     * place to store the entered party name
     */
    private String mPartyName;

    /**
     * Party strictness object, default value of 0
     */
    private int mStrictness = 0;

    /**
     * Party privacy flag, default value of false
     */
    private boolean mPrivate = false;

    /**
     * The action the user was doing, either editing or creating
     */
    private String action = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_create_party);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            action = extras.getString(MainActivity.ACTION);
        }
        count = (TextView) findViewById(R.id.lettercount);
        final EditText mPartyNameTextView = (EditText) findViewById(R.id.partyname);
        Button mSubmit = (Button) findViewById(R.id.submitbutton);
        Spinner spinner = (Spinner) findViewById(R.id.strictness_spinner);
        Switch mSwitch = (Switch) findViewById(R.id.privacySwitch);
        TextView screenName = (TextView) findViewById(R.id.namelable);
        if (action != null && action.equals(MainActivity.EDIT)) {
            screenName.setText("Edit Party");
            mPartyNameTextView.setText(PartyManager.INSTANCE.getName());
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Enable status bar translucency (requires API 19)
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        // set switch text pre lollipop
        mSwitch.setTextOn("Private");
        mSwitch.setTextOff("Not Private");

        // show help dialog when it is clicked
        Button b = (Button) findViewById(R.id.help);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(PartySettingsActivity.this)
                        .setTitle("Help")
                        .setMessage("Strictness: Determines how hard it is for a song to " +
                                "be skipped in the playlist.\n\nPrivate: Sets if the party is private or not.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });

        // toggle privacy based on the switch
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrivate = isChecked;
            }
        });

        // set the outpuut string
        mPartyName = mPartyNameTextView.getText().toString();

        // set the count correctly on start
        count.setText(Integer.toString(mPartyName.length()) + "/15");

        // change listener to update the count text box and the color
        mPartyNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable s) {
                count.setText(String.valueOf((s.length())) + "/15");
                if (s.length() > 12) {
                    count.setTextColor(getResources().getColor(R.color.red_text));
                } else if (s.length() > 8) {
                    count.setTextColor(getResources().getColor(R.color.orange_text));
                } else {
                    count.setTextColor(getResources().getColor(R.color.green_text));
                }
                mPartyName = s.toString();
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // when mSubmit button is hit
                if (mPartyName.length() < 3) {
                    Toast.makeText(PartySettingsActivity.this,
                            "Party name not long enough. Enter longer name.", Toast.LENGTH_LONG).show();
                } else {
                    // create gson object for network call
                    GsonMockClasses.UpdateParty party = new GsonMockClasses.UpdateParty();
                    party.mName = mPartyName;
                    party.mPrivate = mPrivate;
                    party.mStrictness = mStrictness;

                    // get the locaiton of the device and add it to the gson object
                    LocationHelper locationHelper = new LocationHelper(PartySettingsActivity.this);
                    android.location.Location loc = locationHelper.getLocation();
                    if (loc != null) {
                        party.location.latitude = loc.getLatitude();
                        party.location.longitude = loc.getLongitude();
                    }

                    if(action.equals(MainActivity.CREATE))
                        createParty(party);
                    else
                        updateParty(party);
                }
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(PartySettingsActivity.this,
                R.array.strictness_choices, R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // set the selected strictness in form that is taken by she local user setter
                mStrictness = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing, must override method
            }
        });
    }

    /**
     * Exit the settings menu and go to the main activity, clearing the back stack so the back button will leave the party
     */
    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void createParty(final GsonMockClasses.UpdateParty p) {
        APIService.INSTANCE.getRestAdapter().createParty(p)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Party>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(PartySettingsActivity.this, "Error Creating Party", Toast.LENGTH_SHORT).show();
                        Log.e("Party settings", e.toString());
                    }

                    @Override
                    public void onNext(Party party) {
                        if (party != null) {
                            PartyManager.INSTANCE.SetParty(party);
                            Toast.makeText(PartySettingsActivity.this, "Party Created!", Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        }
                    }
                });
    }

    private void updateParty(final GsonMockClasses.UpdateParty p) {
        APIService.INSTANCE.getRestAdapter().updateParty(PartyManager.INSTANCE.getPID(), p)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Party>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(PartySettingsActivity.this, "Error Updating Party", Toast.LENGTH_SHORT).show();
                        Log.e("Party settings", e.toString());
                    }

                    @Override
                    public void onNext(Party party) {
                        if (!party.getStatus().equals("error")) {
                            PartyManager.INSTANCE.setName(p.mName);
                            Toast.makeText(PartySettingsActivity.this, "Party Updated!", Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        }
                    }
                });
    }


}