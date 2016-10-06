package bbbbb.com.socialdining;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ianpanton.serverplease.R;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bbbbb.com.socialdining.activity.BaseActivity;
import bbbbb.com.socialdining.activity.ClientMapActivity;
import bbbbb.com.socialdining.activity.LogInActivity;
import bbbbb.com.socialdining.activity.WaiterMapActivity;
import me.leolin.shortcutbadger.ShortcutBadger;
import android.Manifest;

public class MainActivity extends BaseActivity implements LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected double latitude;
    protected double longitude;
    protected Location currentLocation;
    protected boolean gps_enabled, network_enabled;
    private boolean canGetLocation;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ParseUser.logOut();
        ShortcutBadger.with(getApplicationContext()).remove();
        MyCustomReceiver.badgeCount = 0;

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.ianpanton.serverplease",
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        getLocation();

    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    public void goToCustomerSelectTableActivity(View view) {
        Intent i = new Intent(MainActivity.this, ClientMapActivity.class);
        i.putExtra("target", "customer");
        startActivity(i);
    }

    public void goToWaiterSelectTableActivity(View view) {
        /*ParseQuery<ParseObject> query = ParseQuery.getQuery("CallLog");
        //query.whereEqualTo("username", currentUser.getEmail());
        try {
            List<ParseObject> object = query.find();

        } catch (ParseException e1) {
            //e1.printStackTrace();
        }*/


        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            loadLoginView();
        } else {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", currentUser.getUsername());
            try {
                List<ParseUser> objects = query.find();
                if(objects!=null){
                    for(ParseObject o : objects){
                        if(o.getInt("loggedIn") == 0){
                            currentUser.logOutInBackground();
                            loadLoginView();
                            return;
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(currentUser.getInt("loggedIn") == 0){
                currentUser.logOutInBackground();
                loadLoginView();
                return;
            }
            String restaurant_name = currentUser.getString("restaurant_name");
            if (restaurant_name == null || restaurant_name.equals("")) {
                Intent intent = new Intent(MainActivity.this, WaiterMapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // Check if user is in the selected restaurant
                ParseGeoPoint geoPoint = currentUser.getParseGeoPoint("location");
                Location loc1 = new Location("");
                loc1.setLatitude(geoPoint.getLatitude());
                loc1.setLongitude(geoPoint.getLongitude());

                float distanceInMeters = loc1.distanceTo(currentLocation);

                Log.d("MainActivity", "Distance : " + Float.toString(distanceInMeters));

                if (distanceInMeters > AppConstants.RANGE_OF_RESTAURANT) {

                    Toast.makeText(MainActivity.this, "You are not in your restaurant! Logging out now...", Toast.LENGTH_LONG).show();

                    Log.d("MainActivity", "log out");

                    // logout
                    currentUser.put("loggedIn", 0);

                    ArrayList<String> numList = new ArrayList<String>();

                    for (int i = 1; i < 101; i++) {
                        numList.add("0");
                    }

                    currentUser.put("table_numbers", numList);
                    currentUser.put("restaurant_name", "");

                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                installation.put("loggedin", 0);
                                // installation.saveInBackground();
                                installation.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            ParseUser user = ParseUser.getCurrentUser();
                                            user.logOutInBackground();
                                            cleanNotification();
                                            Toast.makeText(MainActivity.this, "Logged Out!", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Failed to logout", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to logout", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    return;
                }

                Intent intent = new Intent(MainActivity.this, ServerProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    public void exitButton(View view){
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
            //finish();
        }
        finish();
    }

    private void loadLoginView() {
        Intent intent = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("MainActivity:", "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    protected void cleanNotification() {
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }

    public boolean getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            Boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            Boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            5, 5, this);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        currentLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (currentLocation != null) {
                            latitude = currentLocation.getLatitude();
                            longitude = currentLocation.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (currentLocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                5,
                                20, this);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            currentLocation = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (currentLocation != null) {
                                latitude = currentLocation.getLatitude();
                                longitude = currentLocation.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
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



