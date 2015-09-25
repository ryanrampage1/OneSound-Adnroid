package com.onesound.networking;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by ryan on 5/11/15.
 */
public class LocationHelper {

    private final ActiveListener activeListener = new ActiveListener();
    private LocationManager locationManager = null;
    private Location location;

    public LocationHelper(Context c){
        // Get the location manager
        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
    }

    private class ActiveListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            onLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            registerListeners();
        }
    }

    private void onLocation(Location loc) {
        if(location == null) {
            return;
        }
        location = loc;
    }

    public Location getLocation() {
//        registerListeners();
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        unregisterListeners();
    }

    private void registerListeners() {
        unregisterListeners();

        // Create a Criteria object
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String bestAvailable = locationManager.getBestProvider(criteria, true);

        if(bestAvailable != null) {
            locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
            Location location = locationManager.getLastKnownLocation(bestAvailable);
            onLocation(location);
        }
    }

    public void unregisterListeners() {
        locationManager.removeUpdates(activeListener);
    }
}
