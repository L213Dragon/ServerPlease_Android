package bbbbb.com.socialdining;

import android.app.IntentService;
import android.content.Intent;

import android.app.NotificationManager;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.ianpanton.serverplease.R;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;


/**
 * Created by HP-HP on 26-11-2015.
 */
public class LocationUpdates extends IntentService {

    private String TAG = this.getClass().getSimpleName();

    public LocationUpdates() {
        super("Fused Location");
    }

    public LocationUpdates(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "onHandleIntent");

        Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        if(location !=null)
        {
            Log.i(TAG, "onHandleIntent " + location.getLatitude() + "," + location.getLongitude());
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(this);
            noti.setContentTitle("Fused Location");
            noti.setContentText(location.getLatitude() + "," + location.getLongitude());
            noti.setSmallIcon(R.mipmap.ic_launcher);

            //notificationManager.notify(1234, noti.build());

            ParseUser user = ParseUser.getCurrentUser();

            if(user == null){
                return;
            }

            if (user.getParseGeoPoint("location") == null){
                return;
            }

            // Check if user is in the selected restaurant
            ParseGeoPoint geoPoint = user.getParseGeoPoint("location");
            Location loc1 = new Location("");
            loc1.setLatitude(geoPoint.getLatitude());
            loc1.setLongitude(geoPoint.getLongitude());

            float distanceInMeters = loc1.distanceTo(location);

            Log.d("LocationUpdate", "Value: " + Float.toString(distanceInMeters));

            if(distanceInMeters > AppConstants.RANGE_OF_RESTAURANT){

                Log.d("LocationUpdate", "please log out");

                // send notification
                this.sendLocalNotification();
            }
        }
    }

    private void sendLocalNotification(){
        final Intent intent = new Intent("logout");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
