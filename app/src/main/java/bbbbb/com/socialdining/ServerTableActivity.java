package bbbbb.com.socialdining;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.LinearLayout;


import com.google.android.gms.maps.model.LatLng;
import com.ianpanton.serverplease.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class ServerTableActivity extends AppCompatActivity {

    /*private ArrayList<String> numList = new ArrayList<String>();
    private String m_restaurantName = "";
    private LatLng m_restaurantLatLang;
    protected ProgressBar mProgressBar;
    private ArrayList<String> coveredTableList = new ArrayList<String>();
    private GridLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_server_table);

        layout = (GridLayout) findViewById(R.id.st_gridLayout);

        mProgressBar = (ProgressBar)findViewById(R.id.st_progressBar);

        Intent receivedIntent = getIntent();
        m_restaurantName = receivedIntent.getExtras().getString("restaurant_name");
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        m_restaurantLatLang = bundle.getParcelable("position");

        ImageButton m_backBtn = (ImageButton)findViewById(R.id.btnBack);
        m_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get the all restaurant table numbers from the Backend.
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        ParseGeoPoint geoPoint = new ParseGeoPoint(m_restaurantLatLang.latitude, m_restaurantLatLang.longitude);
        query.whereEqualTo("location", geoPoint);
        //mProgressBar.setVisibility(View.VISIBLE);
        final ACProgressFlower dialog = new ACProgressFlower.Builder(ServerTableActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Loading...")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        ArrayList<String> tmpList = new ArrayList<String>();
                        tmpList = (ArrayList<String>) objects.get(i).get("table_numbers");
                        for (int j = 0; j < tmpList.size(); j++) {
                            coveredTableList.add(tmpList.get(j));
                        }
                    }

                    for (int i = 1; i < 101; i++) {
                        Button btn = new Button(ServerTableActivity.this);
                        String numberStr = String.format("%d", i);
                        btn.setText(numberStr);
                        btn.setBackgroundResource(R.drawable.btn_grey);
                        DisplayMetrics dm = new DisplayMetrics();
                        ServerTableActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
                        int width = dm.widthPixels;
                        width = width / 5;
                        int height = width;
                        btn.setLayoutParams(new LinearLayout.LayoutParams(width, height));

                        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                ActionBar.LayoutParams.WRAP_CONTENT
                        );


                        for (int j = 0; j < coveredTableList.size(); j++) {
                            if (coveredTableList.get(j).equals(numberStr)) {
                                // btn.setBackgroundColor(Color.RED);
                                btn.setBackgroundResource(R.drawable.btn_red);
                                break;
                            }
                        }

                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // put code on click operation
                                Button b = (Button) v;
                                String buttonText = b.getText().toString();
                                // Checked if clicked number is in the numberArray
                                for (int i = 0; i < numList.size(); i++) {
                                    String elemStr = numList.get(i);
                                    if (buttonText.equals(elemStr)) {
                                        numList.remove(i);
                                        //b.setBackgroundResource(android.R.drawable.btn_default);
                                        int flag = 0;
                                        for (int j = 0; j < coveredTableList.size(); j++) {
                                            String elemStr1 = coveredTableList.get(j);
                                            if (buttonText.equals(elemStr1)) {
                                                b.setBackgroundResource(R.drawable.btn_red);
                                                flag =1;
                                                break;
                                            }
                                        }

                                        if (flag != 1){
                                            b.setBackgroundResource(R.drawable.btn_grey);
                                        }

                                        return;
                                    }
                                }

//                                for (int i = 0; i < coveredTableList.size(); i++) {
//                                    String elemStr = coveredTableList.get(i);
//                                    if (buttonText.equals(elemStr)) {
//                                        Toast.makeText(ServerTableActivity.this, "This table has already covered by other waiter! Please Choose another!", Toast.LENGTH_LONG).show();
//                                        return;
//                                    }
//                                }

                                numList.add(buttonText);
                                // b.setBackgroundColor(Color.GREEN);
                                b.setBackgroundResource(R.drawable.btn_green);
                            }
                        });

                        layout.addView(btn);
                    }
                    //mProgressBar.setVisibility(View.GONE);
                    dialog.hide();
                } else {
                    //mProgressBar.setVisibility(View.GONE);
                    dialog.hide();
                    Toast.makeText(ServerTableActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void doneBtnClicked(final View v){
        // Save data to parse.com
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put("restaurant_name", m_restaurantName);
        ParseGeoPoint geoPoint = new ParseGeoPoint(m_restaurantLatLang.latitude, m_restaurantLatLang.longitude);
        currentUser.put("location", geoPoint);
        currentUser.put("table_numbers", numList);


        //mProgressBar.setVisibility(v.VISIBLE);
        final ACProgressFlower dialog = new ACProgressFlower.Builder(ServerTableActivity.this)
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
                    Toast.makeText(ServerTableActivity.this, "Saved table numbers successfully!", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(ServerTableActivity.this, ServerProfileActivity.class);
                    ServerTableActivity.this.finish();
                    startActivity(i);
                } else {
                    Toast.makeText(ServerTableActivity.this, "Failed to save", Toast.LENGTH_LONG).show();
                    Log.d(getClass().getSimpleName(), "User Update error:" + e);
                }
            }
        });
    }

    public  void  selectAllBtnClicked(final  View v){
        numList.clear();

        int count = layout.getChildCount();
        for (int i=0 ; i < count; i ++){
            View child = layout.getChildAt(i);
            Button b = (Button) child;
            String buttonText = b.getText().toString();
            numList.add(buttonText);
            b.setBackgroundResource(R.drawable.btn_green);
        }
    }

    public  void  deselectBtnClicked(final  View v){
        numList.clear();

        int count = layout.getChildCount();
        for (int i=0 ; i < count; i ++){
            View child = layout.getChildAt(i);
            Button b = (Button) child;
            String buttonText = b.getText().toString();
            int flag = 0;
            for (int j = 0; j < coveredTableList.size(); j++) {
                String elemStr = coveredTableList.get(j);
                if (buttonText.equals(elemStr)) {
                    b.setBackgroundResource(R.drawable.btn_red);
                    flag =1;
                    break;
                }
            }

            if (flag != 1){
                b.setBackgroundResource(R.drawable.btn_grey);
            }

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ServerTableActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ServerTableActivity.this.finish();
        startActivity(intent);
    }*/
}
