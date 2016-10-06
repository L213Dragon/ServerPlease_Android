package bbbbb.com.socialdining.service;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ianpanton.serverplease.R;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.UUID;

import bbbbb.com.socialdining.LocationUpdates;
import bbbbb.com.socialdining.activity.BaseActivity;

/**
 * Created by HP-HP on 26-11-2015.
 */
public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
                                                                    android.location.LocationListener{

    protected static final String DEBUG_TAG = "BackgroundLocation";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    private Intent mIntentService;
    private PendingIntent mPendingIntent;

    private LocationManager locationManager;

    private String type = "";

    private double latitude;
    private double longitude;

    private boolean timerEnable = false;

    IBinder mBinder = new LocalBinder();

    Thread t = new Thread(new Ti());

    AlertDialog alert;

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DEBUG_TAG, "onCreate");

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher);
        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = builder.getNotification();
        else
            notification = builder.build();

        startForeground(341, notification);

        mIntentService = new Intent(this,LocationUpdates.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, BackgroundLocationService.this);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                BackgroundLocationService.this);

        buildGoogleApiClient();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DEBUG_TAG, "onStartCommand");

        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()){
            mGoogleApiClient.connect();
        }

        Integer pi = intent.getIntExtra("code", -1);
        type = intent.getStringExtra("type");

        if(type != null && type.equals("customer")){
            latitude = intent.getDoubleExtra("latitude", 0);
            longitude = intent.getDoubleExtra("longitude", 0);
        }

        if(pi == 1){
            Log.d("123", "logOut()");
            Thread t = new Thread(new AppThread());
            t.start();
        }

        //super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }



    protected synchronized void buildGoogleApiClient() {
        Log.i(DEBUG_TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        Log.i(DEBUG_TAG, "createLocationRequest()");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.i(DEBUG_TAG, "Started Location Updates");

        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mPendingIntent);
    }

    protected void stopLocationUpdates() {
        Log.i("123","Stopped Location Updates");

        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mPendingIntent);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("123", "Connected to GoogleApiClient");

        startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(DEBUG_TAG, "onLocationChanged");
        Location l = new Location(location);

        if(type != null && type.equals("waiter")) {
            ParseUser user = ParseUser.getCurrentUser();
            ParseGeoPoint geoPoint = user.getParseGeoPoint("location");

            double latitude = geoPoint.getLatitude();
            double longitude = geoPoint.getLongitude();

            l.setLatitude(latitude);
            l.setLongitude(longitude);



            if(location.distanceTo(l) > 1524){
                if(!timerEnable) {
                    showDialog();
                    t.start();
                }
                timerEnable = true;
            }else {
                if(timerEnable){
                    hideDialog();
                    timerEnable = false;
                }
            }

        }else if(type != null && type.equals("customer")){
            l.setLatitude(latitude);
            l.setLongitude(longitude);

            //if(location.distanceTo(l) > 1524){
            if(location.distanceTo(l) > 1524){
                if(!timerEnable) {
                    showDialog();
                    t.start();
                }
                timerEnable = true;
            }else {
                if(timerEnable){
                    hideDialog();
                    timerEnable = false;
                }
            }
        }

        Log.d(DEBUG_TAG, "onLocationChanged:" + String.valueOf(location.distanceTo(l)));
    }

    private void showDialog(){
        Intent intent = new Intent(BaseActivity.BROADCAST_ACTION);
        intent.putExtra("show", true);
        sendBroadcast(intent);
    }

    private void hideDialog(){
        Intent intent = new Intent(BaseActivity.BROADCAST_ACTION);
        intent.putExtra("show", false);
        sendBroadcast(intent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(DEBUG_TAG, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(DEBUG_TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(DEBUG_TAG, "onProviderDisabled");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(DEBUG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("123", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroyService");
        super.onDestroy();
        stopLocationUpdates();
    }

    private void logOut(){


        if(type != null && type.equals("waiter")) {
            Log.d(DEBUG_TAG, "waiter_logOut()");

            ParseUser user = ParseUser.getCurrentUser();

            if (user == null) {
                Log.d(DEBUG_TAG, "user == null");
                return;
            }

            String userName = user.getUsername();

            user.put("loggedIn", 0);
            user.saveEventually();
            Log.d(DEBUG_TAG, "saveEventually");

            user.logOut();
            Log.d(DEBUG_TAG, "logOut()");

            try {
                Log.d(DEBUG_TAG, "try");
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", userName);
                try {
                    Log.d(DEBUG_TAG, "try");
                    List<ParseUser> objects = query.find();
                    if(objects!=null){
                        Log.d(DEBUG_TAG, "objects!=null");
                        for(ParseObject o : objects){
                            Log.d(DEBUG_TAG, "o");
                            o.put("loggedIn", 0);
                            o.save();
                            Log.d(DEBUG_TAG, "save");
                        }
                    }else{
                        Log.d(DEBUG_TAG, "objects==null");
                    }
                } catch (ParseException e) {
                    //e.printStackTrace();
                }
            } catch (Exception e1) {
                Log.d(DEBUG_TAG, "Exception");
            }
            Log.d(DEBUG_TAG, "save_finish");

            //user.saveInBackground();
            /*try {
                user.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }*/


            //user = ParseUser.getCurrentUser();
            //user.logOutInBackground();

        /*user.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("123", "e");
                if (e == null) {
                    Log.d("123", "e == null");
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("loggedin", 0);
                    // installation.saveInBackground();
                    installation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseUser user = ParseUser.getCurrentUser();
                                user.logOutInBackground();
                                //Toast.makeText(ServerProfileActivity.this, "Logged Out!", Toast.LENGTH_LONG).show();
                                //ServerProfileActivity.this.finish();
                                //startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                            } else {

                                //Toast.makeText(ServerProfileActivity.this, "Failed to logout", Toast.LENGTH_LONG).show();
                                //ServerProfileActivity.this.finish();
                                //startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                            }
                        }
                    });
                } else {
                    //Toast.makeText(ServerProfileActivity.this, "Failed to logout", Toast.LENGTH_LONG).show();
                    //ServerProfileActivity.this.finish();
                    //startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                }
            }
        });*/
        }else if(type != null && type.equals("customer")) {
            Log.d(DEBUG_TAG, "customer_logOut()");

            ParseQuery<ParseObject> delete_query = ParseQuery.getQuery("CallLog");
            delete_query.whereEqualTo("devicetoken", getDeviceId());
            try {
                List<ParseObject> delete_object = delete_query.find();
                if(delete_object!=null){
                    for(ParseObject o : delete_object){
                        o.deleteEventually();
                    }
                }
            } catch (ParseException e1) {
                //e1.printStackTrace();
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        logOut();
        Log.d(DEBUG_TAG, "onTaskRemoved");
        stopSelf();
    }

    class AppThread extends Thread {

        public AppThread() {

        }

        public void run() {
            Log.d(DEBUG_TAG, "t_logOut()");
            logOut();
            /*for(int i = 1; i <= 1000; i++) {
                Log.d("123", "Thread");
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
        }
    }

    class Ti extends Thread {

        public Ti() {

        }

        public void run() {
            for(int i = 1; i <= 15; i++) {
                if(!timerEnable){
                    return;
                }

                Log.d(DEBUG_TAG, "Thread");
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logOut();
            stopSelf();
            timerEnable = false;

            Intent intent = new Intent(BaseActivity.BROADCAST_ACTION);
            intent.putExtra("logout", true);
            sendBroadcast(intent);
        }

    }

    private String getDeviceId() {

        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();

        return deviceId;
    }
}
