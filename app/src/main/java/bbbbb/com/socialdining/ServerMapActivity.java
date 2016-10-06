package bbbbb.com.socialdining;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ianpanton.serverplease.R;
import com.parse.ParseUser;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.widget.Spinner;

import me.leolin.shortcutbadger.ShortcutBadger;

public class ServerMapActivity extends AppCompatActivity {

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private Spinner mSpriPlaceType;

    private double mLatitude = 0;
    private double mLongitude = 0;

    private String m_restaurantTitle = "";
    private LatLng m_restaurantLatLang;
    private TextView m_numberTextView;

//    protected Button m_findBtn;

    private double m_cameraLat = 0;
    private double m_cameraLong = 0;

    protected ProgressBar mProgressBar;

    String mTarget;

    /*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_map);

        ParseUser currUser = ParseUser.getCurrentUser();

        ShortcutBadger.with(getApplicationContext()).remove();
        MyCustomReceiver.badgeCount = 0;

        m_numberTextView = (TextView) findViewById(R.id.sm_restaurantText);
        //m_findBtn = (Button)findViewById(R.id.sm_findBtn);
        mProgressBar = (ProgressBar) findViewById(R.id.sm_progressbar);

//        m_findBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                mProgressBar.setVisibility(View.VISIBLE);
//                showNearbyRestaurant(m_cameraLat, m_cameraLong);
//            }
//        });

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.sm_map));

        ImageButton m_backBtn = (ImageButton) findViewById(R.id.btnBack);
        m_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    mGoogleMap = map;
                    mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_LONG).show();
        }


    }


    @TargetApi(Build.VERSION_CODES.M)
    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready

            // Creating a new non-ui thread task to download Google place json data

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);

            map.getUiSettings().setZoomControlsEnabled(true);

            mGoogleMap.setOnMarkerClickListener(this);

            // Now that map has loaded, let's get our location!
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    String msg = "Updated Location: " +
                            Double.toString(cameraPosition.target.latitude) + "," +
                            Double.toString(cameraPosition.target.longitude);
                    m_cameraLat = cameraPosition.target.latitude;
                    m_cameraLong = cameraPosition.target.longitude;
                }
            });

            connectClient();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_LONG).show();
        }
    }

    protected void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Called when the Activity becomes visible.
    */
   /* @Override
    protected void onStart() {
        super.onStart();
        connectClient();
    }

    /*
	 * Called when the Activity is no longer visible.
	 */
    /*@Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /*
     * Handle results returned to the FragmentActivity by Google Play services
     */
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
               /* switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.connect();
                        break;
                }
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    /*@TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            // Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17.0f);
            map.animateCamera(cameraUpdate, 1000, null);

            startLocationUpdates();

            showNearbyRestaurant(location.getLatitude(), location.getLongitude());


        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_LONG).show();
        }
    }

    private void showNearbyRestaurant(double latitude, double longitude) {
        String type = "restaurant";

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        mLatitude = latitude;
        mLongitude = longitude;
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius=" + AppConstants.SEARCH_RADIUS);
        sb.append("&types=" + type);
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyD8oay_EkrzLaifDCCrNZ4cL6KGqwwojhQ");

        PlacesTask placesTask = new PlacesTask();

        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
    }


    @TargetApi(Build.VERSION_CODES.M)
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
    }

    /** A method to download json data from url */
    /*private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);


            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Toast.makeText(ServerMapActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    /*@Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_LONG).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    /*@Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * fksajkfljdksaljfkaj
		 * services activity that can resolve error.
		 */
        /*if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
          /*  } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /** A class, to download Google Places */
   /* private class PlacesTask extends AsyncTask<String, Integer, String>{

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
                Log.d("Do in backgroud ", "asdfaf");
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        //d after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }
    }





    /** A class to parse the Google Places in JSON format */
    /*private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
               /* places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){

            // Clears all the existing markers
            mGoogleMap.clear();

            for(int i=0;i<list.size();i++){

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("place_name");

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                // markerOptions.title(name + " : " + vicinity);
                markerOptions.title(name);

                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

                // Placing a marker on the touched position
                Marker restaurantMarker = mGoogleMap.addMarker(markerOptions);
            }

            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Toast.makeText(this, marker.getTitle(),Toast.LENGTH_LONG).show();
        marker.showInfoWindow();

        // If customer is working now
        Intent receivedIntent = getIntent();

        m_restaurantTitle = marker.getTitle();
        m_numberTextView.setText("You are serving at " +  m_restaurantTitle + ".");

        // Get the geoLocation selected restaurant
        m_restaurantLatLang = marker.getPosition();

        return true;
    }

    public void goToNextScreen(View view){

        if (m_restaurantTitle.equals("")){
            Toast.makeText(ServerMapActivity.this, "Please select the restaurant!", Toast.LENGTH_LONG).show();
        } else {
            Intent i = new Intent(ServerMapActivity.this, ServerTableActivity.class);
            i.putExtra("restaurant_name", m_restaurantTitle);
            Bundle args = new Bundle();
            args.putParcelable("position", m_restaurantLatLang);
            i.putExtra("bundle", args);
            ServerMapActivity.this.finish();
            startActivity(i);
        }
    }

//    @Override
//     public void onBackPressed() {
//
//    }
*/
}
