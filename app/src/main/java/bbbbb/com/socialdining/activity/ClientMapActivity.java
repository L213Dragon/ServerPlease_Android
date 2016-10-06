package bbbbb.com.socialdining.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.ianpanton.serverplease.R;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import bbbbb.com.socialdining.fragment.GoogleMapFragment;
import bbbbb.com.socialdining.service.BackgroundLocationService;
import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class ClientMapActivity extends BaseActivity implements GoogleMapFragment.GoogleMapFragmentListener {
    private String m_tableNumber;
    private Button m_callServiceBtn;

    private String m_restaurantTitle;
    private LatLng m_restaurantLatLang;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        View m_backBtn = findViewById(R.id.llBack);
        m_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        m_callServiceBtn = (Button) findViewById(R.id.cm_nextBtn);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.sm_fragment_cont, GoogleMapFragment.getInstance(this));
        ft.commit();
    }

    @Override
    public boolean onMarkerClick(final Marker marker, final Location m_currentLocation) {

        final LatLng m_restaurantLatLang = marker.getPosition();

        final ACProgressFlower dialog = new ACProgressFlower.Builder(ClientMapActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Loading")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();

        // Check if user is around his previous loggedin restaurant
        ParseQuery<ParseObject> locQuery = ParseQuery.getQuery("_User");
        ParseGeoPoint geoPointL = new ParseGeoPoint(m_restaurantLatLang.latitude, m_restaurantLatLang.longitude);
        locQuery.whereEqualTo("location", geoPointL);
        locQuery.whereEqualTo("loggedIn", 1);
        locQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                dialog.hide();
                if (e == null) {
                    if (objects.size() == 0) {
                        Toast.makeText(ClientMapActivity.this, "No waiters with this app at this location. Please ask waiter to download and use the app!", Toast.LENGTH_LONG).show();
                    } else {
                        a(marker, m_currentLocation);
                    }
                } else {
                    Toast.makeText(ClientMapActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return true;
    }

    private void a(final Marker marker, final Location m_currentLocation){
        m_restaurantLatLang = marker.getPosition();
        String CurrentString = marker.getTitle();
        String[] separated = CurrentString.split(" : ");
        m_restaurantTitle = separated[0];
        // If customer is working now
        Intent receivedIntent = getIntent();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(m_restaurantTitle + "\n Please input the table number");
        final EditText input = new EditText(this);
        builder.setPositiveButton("ok", null);
        builder.setNegativeButton("cancel", null);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        final AlertDialog mAlertDialog = builder.create();

        mAlertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        m_tableNumber = input.getText().toString();

                        // Set the text into the table_nmberLabel textview.
                        TextView tbl_numberTextView = (TextView) findViewById(R.id.cm_table_numberLabel);
                        tbl_numberTextView.setText("You are located at " + m_tableNumber + " in the " + m_restaurantTitle);

                        final ParseGeoPoint geoPoint = new ParseGeoPoint(m_restaurantLatLang.latitude, m_restaurantLatLang.longitude);
                        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("CallLog");
                        query1.whereEqualTo("location", geoPoint);
                        query1.whereEqualTo("table_number", m_tableNumber);

                        Date now = new Date();
                        long miliseconds = now.getTime();
                        long t1 = miliseconds - 3 * 1000 * 60;//количество минут
                        Date min3ago = new Date(t1);
                        query1.whereGreaterThan("createdAt", min3ago);

                        Toast.makeText(ClientMapActivity.this, "Checking if the table is available....", Toast.LENGTH_LONG).show();

                        //mProgressBar.setVisibility(view.VISIBLE);
                        final ACProgressFlower dialog = new ACProgressFlower.Builder(ClientMapActivity.this)
                                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                                .themeColor(Color.WHITE)
                                .text("Loading")
                                .fadeColor(Color.DKGRAY).build();
                        dialog.show();

                        final View view1 = view;

                        ParseObject.createWithoutData("invFriend", "efgh").deleteEventually();

                        query1.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null) {
                                    if (objects.size() > 0) {
//                                        mProgressBar.setVisibility(view1.GONE);
                                        dialog.hide();
                                        Toast.makeText(ClientMapActivity.this, "This table has already been charged by other! Please enter another number!", Toast.LENGTH_LONG).show();
                                    } else {
                                        ParseQuery<ParseObject> delete_query = ParseQuery.getQuery("CallLog");
                                        delete_query.whereEqualTo("devicetoken", getDeviceId());
                                        try {
                                            List<ParseObject> delete_object = delete_query.find();
                                            if(delete_object!=null){
                                                for(ParseObject o : delete_object){
                                                    o.delete();
                                                }
                                            }
                                        } catch (ParseException e1) {
                                            //e1.printStackTrace();
                                        }

                                        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                                        query.whereEqualTo("location", geoPoint);
                                        ArrayList<String> arr = new ArrayList<String>();
                                        arr.add(m_tableNumber);
                                        query.whereContainedIn("table_numbers", arr);
                                        query.whereEqualTo("loggedIn", 1);
                                        query.findInBackground(new FindCallback<ParseObject>() {
                                            @Override
                                            public void done(List<ParseObject> objects, ParseException e) {
                                                if (e == null) {
                                                    if (objects.size() > 0) {
                                                        //mProgressBar.setVisibility(view1.GONE);
                                                        dialog.hide();
                                                        mAlertDialog.dismiss();
                                                    } else {
                                                        //mProgressBar.setVisibility(view1.GONE);
                                                        dialog.hide();

                                                        Toast.makeText(ClientMapActivity.this, "No waiters with this app for this seat at this restaurant." +
                                                                " Please ask waiter to download and use the app!", Toast.LENGTH_LONG).show();
                                                    }
                                                } else {
                                                    //mProgressBar.setVisibility(view1.GONE);
                                                    dialog.hide();
                                                    Toast.makeText(ClientMapActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                        //mAlertDialog.hide();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    public void goToNextScreen(final View view){

        if (m_restaurantTitle == null || m_tableNumber == null || m_restaurantLatLang == null || m_restaurantTitle.equals("") || m_tableNumber.equals("")){
            Toast.makeText(ClientMapActivity.this, "Please select which restaurant you are located at!", Toast.LENGTH_LONG).show();
        } else {
            //mProgressBar.setVisibility(view.VISIBLE);
            ParseQuery<ParseObject> delete_query = ParseQuery.getQuery("CallLog");
            delete_query.whereEqualTo("devicetoken", getDeviceId());
            try {
                List<ParseObject> delete_object = delete_query.find();
                if(delete_object!=null){
                    for(ParseObject o : delete_object){
                        o.delete();
                    }
                }
            } catch (ParseException e1) {
                //e1.printStackTrace();
            }

            final ACProgressFlower dialog = new ACProgressFlower.Builder(ClientMapActivity.this)
                    .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                    .themeColor(Color.WHITE)
                    .text("Calling...")
                    .fadeColor(Color.DKGRAY).build();
            dialog.show();
            Toast.makeText(ClientMapActivity.this, "Calling sevice now...", Toast.LENGTH_LONG).show();
            final ParseGeoPoint geoPoint = new ParseGeoPoint(m_restaurantLatLang.latitude, m_restaurantLatLang.longitude);
            ParseObject obj = ParseObject.create("CallLog");
            obj.put("location", geoPoint);
            obj.put("table_number", m_tableNumber);
            obj.put("devicetoken", getDeviceId());

            obj.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        // Send push notification to waiter charge this number.
                        // Find the waiter to send.

                        // Check the waiter is charged in this table number
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");

                        query.whereEqualTo("location", geoPoint);
                        ArrayList<String> arr = new ArrayList<String>();
                        arr.add(m_tableNumber);
                        query.whereContainedIn("table_numbers", arr);
                        query.whereEqualTo("loggedIn", 1);
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {

                                if (e == null){
                                    if (objects.size() > 0){

                                        for(int i = 0; i < objects.size(); i ++){

                                            ParseObject f_object = objects.get(i);
                                            String f_email = (String)f_object.get("username");
                                            // Send push notification to waiter with this email
                                            JSONObject obj;
                                            try {
                                                // Block button for 10 minutes
                                                m_callServiceBtn.setEnabled(false);
                                                Timer buttonTimer = new Timer();
                                                buttonTimer.schedule(new TimerTask() {
                                                    @Override
                                                    public  void run() {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                m_callServiceBtn.setEnabled(true);
                                                            }
                                                        });
                                                    }
                                                }, 5000);


                                                String location = m_restaurantLatLang.toString();
                                                obj = new JSONObject();
                                                String message = "Number " + m_tableNumber + " is calling service!";
                                                obj.put("alert", message);
                                                obj.put("title", "ServerPlease!");
                                                obj.put("table_number", m_tableNumber);
                                                obj.put("sound", "default");
                                                obj.put("badge", "increment");

                                                String jsonDataString = obj.toString();

                                                final HashMap<String, Object> params = new HashMap<String, Object>();
                                                params.put("data", jsonDataString);
                                                params.put("email", f_email);
                                                ParseCloud.callFunctionInBackground("sendPush", params, new FunctionCallback<String>() {
                                                    @Override
                                                    public void done(String object, ParseException e) {
                                                        if (e == null) {
                                                            Log.d("sendPush", "success");
                                                        } else {
                                                            Log.d("sendPush", e.getMessage());
                                                        }
                                                    }
                                                });
//
//                                                ParsePush push = new ParsePush();
//                                                ParseQuery query = ParseInstallation.getQuery();
//
//                                                // Push the notification to Android users
//                                                query.whereEqualTo("email", f_email);
//                                                query.whereEqualTo("loggedin", 1);
//                                                push.setQuery(query);
//                                                push.setData(obj);
//                                                push.sendInBackground();

                                                dialog.hide();
                                                Toast.makeText(ClientMapActivity.this, "Calling successfully!", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(ClientMapActivity.this, BackgroundLocationService.class);
                                                intent.putExtra("type", "customer");
                                                intent.putExtra("latitude", m_restaurantLatLang.latitude);
                                                intent.putExtra("longitude", m_restaurantLatLang.longitude);
                                                startService(intent);
                                            } catch (JSONException exc) {
                                                exc.printStackTrace();
                                            }
                                        }

                                    } else {
                                        //mProgressBar.setVisibility(view.GONE);
                                        dialog.hide();
                                        Toast.makeText(ClientMapActivity.this, "No waiters with this app for this seat at this restaurant. Please ask waiter to download and use the app!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    //mProgressBar.setVisibility(view.GONE);
                                    dialog.hide();
                                    Toast.makeText(ClientMapActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        //mProgressBar.setVisibility(view.GONE);
                        dialog.hide();
                        Toast.makeText(ClientMapActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
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
