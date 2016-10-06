package bbbbb.com.socialdining.fragment;


import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ianpanton.serverplease.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class ServerTableFagment extends Fragment {

    private ArrayList<String> numList = new ArrayList<String>();

    private String m_restaurantName = "";
    private LatLng m_restaurantLatLang;
    protected ProgressBar mProgressBar;
    private ArrayList<String> coveredTableList = new ArrayList<String>();
    private GridLayout layout;

    public static ServerTableFagment newInstance(LatLng m_restaurantLatLang, String m_restaurantName){
        ServerTableFagment fragment = new ServerTableFagment();
        fragment.setM_restaurantLatLang(m_restaurantLatLang);
        fragment.setM_restaurantName(m_restaurantName);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_server_table, container, false);

        layout = (GridLayout) rootView.findViewById(R.id.st_gridLayout);


        /*Intent receivedIntent = getIntent();
        m_restaurantName = receivedIntent.getExtras().getString("restaurant_name");
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        m_restaurantLatLang = bundle.getParcelable("position");*/

        // Get the all restaurant table numbers from the Backend.
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        ParseGeoPoint geoPoint = new ParseGeoPoint(m_restaurantLatLang.latitude, m_restaurantLatLang.longitude);
        query.whereEqualTo("location", geoPoint);
        //mProgressBar.setVisibility(View.VISIBLE);
        final ACProgressFlower dialog = new ACProgressFlower.Builder(ServerTableFagment.this.getContext())
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
                        Button btn = new Button(ServerTableFagment.this.getContext());
                        String numberStr = String.format("%d", i);
                        btn.setText(numberStr);
                        btn.setBackgroundResource(R.drawable.btn_grey);
                        DisplayMetrics dm = new DisplayMetrics();
                        ServerTableFagment.this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
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
                    Toast.makeText(ServerTableFagment.this.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
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

    public void setM_restaurantLatLang(LatLng m_restaurantLatLang) {
        this.m_restaurantLatLang = m_restaurantLatLang;
    }

    public void setM_restaurantName(String m_restaurantName) {
        this.m_restaurantName = m_restaurantName;
    }

    public ArrayList<String> getNumList() {
        return numList;
    }
}
