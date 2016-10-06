package bbbbb.com.socialdining.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import bbbbb.com.socialdining.AppConstants;
import bbbbb.com.socialdining.PlaceJSONParser;


public class GoogleMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public interface GoogleMapFragmentListener{
        boolean onMarkerClick(Marker marker, Location curLocation);
    }

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private Location m_currentLocation;

    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private LocationRequest mLocationRequest;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;

    private double m_cameraLat = 0;
    private double m_cameraLong = 0;

    private double mLatitude = 0;
    private double mLongitude = 0;

    private GoogleMapFragmentListener googleMapFragmentListener;


    public static GoogleMapFragment getInstance(GoogleMapFragmentListener googleMapFragmentListener){
        GoogleMapFragment fragment = new GoogleMapFragment();
        fragment.setGoogleMapFragmentListener(googleMapFragmentListener);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_google_map, container, false);

        //mapFragment = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.sm_map));

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
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_LONG).show();
        }



        return rootView;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);

        mapFragment = (SupportMapFragment)childFragment;
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready

            // Creating a new non-ui thread task to download Google place json data

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);

            map.getUiSettings().setZoomControlsEnabled(true);

            mGoogleMap.setOnMarkerClickListener(this);

            // Now that map has loaded, let's get our location!
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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
            Toast.makeText(getActivity(), "Error - Map was null!!", Toast.LENGTH_LONG).show();
        }
    }

    protected void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getActivity().getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        m_currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (m_currentLocation == null) {
            Toast.makeText(getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_LONG).show();
            return false;
        }

        Location loc1 = new Location("");
        loc1.setLatitude(m_currentLocation.getLatitude());
        loc1.setLongitude(m_currentLocation.getLongitude());

        LatLng m_restaurantLatLang = marker.getPosition();

        Location loc2 = new Location("");
        loc2.setLatitude(m_restaurantLatLang.latitude);
        loc2.setLongitude(m_restaurantLatLang.longitude);

        float distanceInMeters = loc1.distanceTo(loc2);

        if(distanceInMeters > AppConstants.RANGE_OF_RESTAURANT){
            Toast.makeText(getContext(), "You are not in this restaurant", Toast.LENGTH_LONG).show();
            return false;
        }

        if(googleMapFragmentListener != null)
            return googleMapFragmentListener.onMarkerClick(marker, m_currentLocation);

        return false;
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
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Display the connection status
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        m_currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (m_currentLocation != null) {
            // Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(m_currentLocation.getLatitude(), m_currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17.0f);
            map.animateCamera(cameraUpdate, 1000, null);

            startLocationUpdates();

            showNearbyRestaurant(m_currentLocation.getLatitude(), m_currentLocation.getLongitude());


        } else {
            Toast.makeText(getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(getContext(), "Disconnected. Please re-connect.", Toast.LENGTH_LONG).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(getContext(), "Network lost. Please re-connect.", Toast.LENGTH_LONG).show();
        }
    }

    public void setGoogleMapFragmentListener(GoogleMapFragmentListener googleMapFragmentListener) {
        this.googleMapFragmentListener = googleMapFragmentListener;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, (LocationListener) this);
    }

    private String downloadUrl(String strUrl) throws IOException {
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
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
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

    private class PlacesTask extends AsyncTask<String, Integer, String> {

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

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

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
        }
    }
}
