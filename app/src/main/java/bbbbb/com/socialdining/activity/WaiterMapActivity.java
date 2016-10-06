package bbbbb.com.socialdining.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.ianpanton.serverplease.R;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import bbbbb.com.socialdining.ServerProfileActivity;
import bbbbb.com.socialdining.ServerTableActivity;
import bbbbb.com.socialdining.fragment.GoogleMapFragment;
import bbbbb.com.socialdining.fragment.ServerTableFagment;
import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class WaiterMapActivity extends AppCompatActivity implements GoogleMapFragment.GoogleMapFragmentListener{
    private String m_restaurantTitle;
    private TextView m_numberTextView;
    private LatLng m_restaurantLatLang;

    private boolean b = false;

    private ServerTableFagment serverTableFagment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_map);

        View m_backBtn = findViewById(R.id.llBack);
        m_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        m_numberTextView = (TextView) findViewById(R.id.sm_restaurantText);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.sm_fragment_cont, GoogleMapFragment.getInstance(this));
        ft.commit();
    }

    @Override
    public boolean onMarkerClick(Marker marker, Location curLocation) {
        m_restaurantTitle = marker.getTitle();
        m_numberTextView.setText("You are serving at " +  m_restaurantTitle + ".");

        // Get the geoLocation selected restaurant
        m_restaurantLatLang = marker.getPosition();

        return true;
    }

    public void goToNextScreen(View view){

        if (m_restaurantTitle.equals("")){
            Toast.makeText(WaiterMapActivity.this, "Please select the restaurant!", Toast.LENGTH_LONG).show();
        } else {
            if(b){
                doneBtnClicked();
                /*Intent i = new Intent(WaiterMapActivity.this, ServerProfileActivity.class);
                i.putExtra("restaurant_name", m_restaurantTitle);
                Bundle args = new Bundle();
                args.putParcelable("position", m_restaurantLatLang);
                i.putExtra("bundle", args);
                WaiterMapActivity.this.finish();
                startActivity(i);*/
            }else {
                serverTableFagment = ServerTableFagment.newInstance(m_restaurantLatLang, m_restaurantTitle);

                b = true;
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.sm_fragment_cont, serverTableFagment);
                ft.commit();
            }

            //ServerProfileActivity*/



        }
    }

    //public void doneBtnClicked(final View v)
     public void doneBtnClicked(){
        // Save data to parse.com
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put("restaurant_name", m_restaurantTitle);
        ParseGeoPoint geoPoint = new ParseGeoPoint(m_restaurantLatLang.latitude, m_restaurantLatLang.longitude);
        currentUser.put("location", geoPoint);
        currentUser.put("table_numbers", serverTableFagment.getNumList());


        //mProgressBar.setVisibility(v.VISIBLE);
        final ACProgressFlower dialog = new ACProgressFlower.Builder(WaiterMapActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Saving...")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();

        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //mProgressBar.setVisibility(v.GONE);
                dialog.hide();
                if (e == null) {
                    //Saved successfully
                    Toast.makeText(WaiterMapActivity.this, "Saved table numbers successfully!", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(WaiterMapActivity.this, ServerProfileActivity.class);
                    WaiterMapActivity.this.finish();
                    startActivity(i);
                } else {
                    Toast.makeText(WaiterMapActivity.this, "Failed to save", Toast.LENGTH_LONG).show();
                    Log.d(getClass().getSimpleName(), "User Update error:" + e);
                }
            }
        });
    }
}
