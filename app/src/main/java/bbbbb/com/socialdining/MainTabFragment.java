package bbbbb.com.socialdining;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

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
import me.leolin.shortcutbadger.ShortcutBadger;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MainTabFragment extends Fragment {

    private ArrayList<String> numList = new ArrayList<String>();
    private ArrayList<String> coveredList = new ArrayList<String>();

    protected Button m_saveBtn;
    protected  Button m_changeRestaurantBtn;
    protected ProgressBar m_ProgressBar;
    protected GridLayout layout;
    protected TextView m_text;
    Button m_selectAllBtn;
    Button m_deselectBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_main_tab, container, false);
        m_saveBtn = (Button)rootView.findViewById(R.id.fm_saveBtn);
        m_ProgressBar = (ProgressBar)rootView.findViewById(R.id.fm_progressBar);
        m_changeRestaurantBtn = (Button)rootView.findViewById(R.id.fm_changeRestaurant);
        m_text = (TextView)rootView.findViewById(R.id.mt_textView);
        m_selectAllBtn = (Button)rootView.findViewById(R.id.fm_selectAllButton);
        m_deselectBtn = (Button)rootView.findViewById(R.id.fm_deselectButton);

        ShortcutBadger.with(getContext()).remove();
        MyCustomReceiver.badgeCount = 0;

        numList.clear();
        coveredList.clear();



        m_saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Save data to parse.com
                ParseUser currentUser = ParseUser.getCurrentUser();
                currentUser.put("table_numbers", numList);


                //m_ProgressBar.setVisibility(v.VISIBLE);
                final ACProgressFlower dialog = new ACProgressFlower.Builder(getActivity())
                        .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                        .themeColor(Color.WHITE)
                        .text("Saving...")
                        .fadeColor(Color.DKGRAY).build();
                dialog.show();
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        //m_ProgressBar.setVisibility(v.GONE);
                        dialog.hide();
                        if (e == null) {
                            //Saved successfully
                            Toast.makeText(getActivity().getApplicationContext(), "Saved table numbers successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                            Log.d(getClass().getSimpleName(), "User Update error:" + e);
                        }
                    }
                });
            }
        });

        m_selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
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
        });

        m_deselectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                numList.clear();

                int count = layout.getChildCount();
                for (int i=0 ; i < count; i ++){
                    View child = layout.getChildAt(i);
                    Button b = (Button) child;
                    String buttonText = b.getText().toString();
                    int flag = 0;
                    for (int j = 0; j < coveredList.size(); j++) {
                        String elemStr = coveredList.get(j);
                        if (buttonText.equals(elemStr)) {
                            b.setBackgroundResource(R.drawable.btn_red);
                            flag =1;
                            break;
                        }
                    }

                    if (flag == 0){
                        b.setBackgroundResource(R.drawable.btn_grey);
                    }

                }
            }
        });


        m_changeRestaurantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Save data to parse.com
                Intent intent = new Intent(getActivity(), ServerRestaurantActivity.class);
                //getActivity().finish();
                startActivity(intent);
            }
        });

        ArrayList<String> tmpList = new ArrayList<String>();
        final ParseUser currUser = ParseUser.getCurrentUser();
        String restaurant_name = currUser.getString("restaurant_name");
        tmpList = (ArrayList<String>)currUser.get("table_numbers");

        m_text.setText("You are serving at the " + restaurant_name + ".");

        for(int i = 0; i < tmpList.size(); i ++){
            String tmpStr = tmpList.get(i);
            numList.add(tmpStr);
        }

        layout = (GridLayout) rootView.findViewById(R.id.fm_gridLayout);

        // Get the all restaurant table numbers from the Backend.
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        ParseGeoPoint geoPoint = currUser.getParseGeoPoint("location");
        query.whereEqualTo("location", geoPoint);
        //m_ProgressBar.setVisibility(View.VISIBLE);
        final ACProgressFlower dialog = new ACProgressFlower.Builder(getActivity())
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Loading")
                .fadeColor(Color.DKGRAY).build();
        //dialog.show();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        ArrayList<String> tmpList = new ArrayList<String>();
                        ParseObject object = objects.get(i);
                        tmpList = (ArrayList<String>) object.get("table_numbers");
                        String email = (String) object.get("email");
                        String emailOfCurrentUser = (String) currUser.get("email");
                        if (email.equals(emailOfCurrentUser)) {
                            continue;
                        }
                        int flag = 0;
                        for (int j = 0; j < tmpList.size(); j++) {
                            for (int k = 0; k < coveredList.size(); k++) {
                                if (tmpList.get(j) == coveredList.get(k)) {
                                    flag = 1;
                                    break;
                                } else {
                                    flag = 0;
                                }
                            }

                            if (flag == 0) {
                                coveredList.add(tmpList.get(j));
                            } else {
                                flag = 0;
                            }
                        }
                    }

                    for (int i = 1; i < 101; i++) {
                        Button btn = new Button(getActivity());
                        String numberStr = String.format("%d", i);
                        btn.setText(numberStr);
                        btn.setBackgroundResource(R.drawable.btn_grey);
                        DisplayMetrics dm = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                        int width = dm.widthPixels;
                        width = width / 5;
                        int height = width;
                        btn.setLayoutParams(new LinearLayout.LayoutParams(width, height));

                        for (int k = 0; k < coveredList.size(); k++) {
                            String elemStr1 = coveredList.get(k);
                            if (numberStr.equals(elemStr1)) {
                                btn.setBackgroundResource(R.drawable.btn_red);
                                break;
                            }
                        }

                        for (int j = 0; j < numList.size(); j++) {
                            String elemStr = numList.get(j);
                            if (numberStr.equals(elemStr)) {
                                btn.setBackgroundResource(R.drawable.btn_green);
                                continue;
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

                                        int flag = 0;
                                        for (int j = 0; j < coveredList.size(); j++) {
                                            String elemStr1 = coveredList.get(j);
                                            if (buttonText.equals(elemStr1)) {
                                                b.setBackgroundResource(R.drawable.btn_red);
                                                flag = 1;
                                                break;
                                            }
                                        }

                                        if (flag != 1) {
                                            b.setBackgroundResource(R.drawable.btn_grey);
                                        }

                                        return;
                                    }
                                }

//                                for (int i = 0; i < coveredList.size(); i++) {
//                                    String elemStr = coveredList.get(i);
//                                    if (buttonText.equals(elemStr)) {
//                                        Toast.makeText(getActivity(), "This table has already covered by other waiter! Please Choose another!", Toast.LENGTH_SHORT).show();
//                                        return;
//                                    }
//                                }

                                numList.add(buttonText);
                                //b.setBackgroundColor(Color.GREEN);
                                b.setBackgroundResource(R.drawable.btn_green);
                            }
                        });

                        layout.addView(btn);
                    }
                    //m_ProgressBar.setVisibility(View.GONE);
                    //dialog.hide();
                } else {
                    //m_ProgressBar.setVisibility(View.GONE);
                    //dialog.hide();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }
}



