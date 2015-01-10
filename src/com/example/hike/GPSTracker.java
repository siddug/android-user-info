package com.example.hike;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

class entry{
	public Double longitude;
	public Double latitude;
	public Long   time;
	
	@Override
	public String toString(){
		return longitude.toString() + " - " + latitude.toString() + " - " + time.toString();
	}
	
	entry(Double la, Double lo, Long time){
		longitude = lo;
		latitude  = la;
		this.time	  = time;
	}
	entry(entry e){
		longitude = e.longitude;
		latitude = e.latitude;
		time = e.time;
	}
}

public class GPSTracker extends Service implements LocationListener {
	 
    private final Context mContext;
    public List loc			= new ArrayList<entry> ();
    
    // flag for GPS status
    boolean isGPSEnabled = false;
 
    // flag for network status
    boolean isNetworkEnabled = false;
 
    boolean canGetLocation = false;
 
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
 
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 10 meters
 
    private static final long MIN_TIME_BW_UPDATES = 2000; // 2 sec
 
    protected LocationManager locationManager;
 
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
 
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        entry e = new entry(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
        loc.add(e);
        return location;
    }
    @Override
    public void onLocationChanged(Location location) {
    	entry e = new entry(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
        loc.add(e);
    }
 
    @Override
    public void onProviderDisabled(String provider) {
    }
 
    @Override
    public void onProviderEnabled(String provider) {
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
 
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
         
        // return latitude
        return latitude;
    }
     
    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
         
        // return longitude
        return longitude;
    }
    
    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }
    
    public List<entry> getList(){
    	List loc2			= new ArrayList<entry> (loc);
    	entry e = new entry((entry)loc.get(loc.size()-1));
    	loc.clear();
    	loc.add(e);
    	return loc2;
    }
    
    
}