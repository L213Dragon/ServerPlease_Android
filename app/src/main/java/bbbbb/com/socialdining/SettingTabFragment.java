package bbbbb.com.socialdining;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.EditText;

import com.ianpanton.serverplease.R;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import me.leolin.shortcutbadger.ShortcutBadger;


/**

 */
public class SettingTabFragment extends Fragment {

        protected Button m_changeBtn;
        protected ProgressBar m_ProgressBar;
        protected Button m_logoutBtn;
        protected Button m_delBtn;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            final View rootView = inflater.inflate(R.layout.fragment_setting_tab, container, false);
            m_changeBtn = (Button)rootView.findViewById(R.id.fs_changeBtn);
            m_ProgressBar = (ProgressBar)rootView.findViewById(R.id.fs_progressBar);
            m_logoutBtn = (Button) rootView.findViewById(R.id.fs_logoutBtn);
            m_delBtn = (Button) rootView.findViewById(R.id.fs_deleteBtn);

            ShortcutBadger.with(getContext()).remove();
            MyCustomReceiver.badgeCount = 0;



            m_changeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    // Save data to parse.com
                    EditText newPassText = (EditText) rootView.findViewById(R.id.fs_newPassword);
                    String newPassword = newPassText.getText().toString();
                    EditText confirmPassText = (EditText) rootView.findViewById(R.id.fs_confirmPassword);
                    String confirmPassword = confirmPassText.getText().toString();

                    ParseUser currentUser = ParseUser.getCurrentUser();

                    if (newPassText.toString() == "") {
                        Toast.makeText(getActivity().getApplicationContext(), "Password can not be empty!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        //Alert
                        Toast.makeText(getActivity().getApplicationContext(), "New password does not match with confirm password", Toast.LENGTH_LONG).show();
                    } else {
                        //currentUser.setPassword();
                        //m_ProgressBar.setVisibility(v.VISIBLE);
                        final ACProgressFlower dialog = new ACProgressFlower.Builder(getActivity())
                                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                                .themeColor(Color.WHITE)
                                .text("saving...")
                                .fadeColor(Color.DKGRAY).build();
                        dialog.show();
                        currentUser.setPassword(newPassword);

                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    //Saved successfully
                                    Toast.makeText(getActivity().getApplicationContext(), "Saved successfully", Toast.LENGTH_LONG).show();
                                    ParseUser user = ParseUser.getCurrentUser();
                                    user.put("loggedIn", 0);
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            if (e == null) {
                                                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                                installation.put("loggedin", 0);
                                                installation.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            //m_ProgressBar.setVisibility(v.GONE);
                                                            dialog.hide();
                                                            ParseUser user = ParseUser.getCurrentUser();
                                                            user.logOutInBackground();
                                                            cleanNotification();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Logged Out!", Toast.LENGTH_LONG).show();
                                                            getActivity().finish();
                                                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                                                        } else {
                                                            //m_ProgressBar.setVisibility(v.GONE);
                                                            dialog.hide();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Failed to logout", Toast.LENGTH_LONG).show();
                                                            getActivity().finish();
                                                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                                                        }
                                                    }
                                                });
                                            } else {
                                                //m_ProgressBar.setVisibility(v.GONE);
                                                dialog.hide();
                                                Toast.makeText(getActivity().getApplicationContext(), "Failed to logout", Toast.LENGTH_LONG).show();
                                                getActivity().finish();
                                                startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                                            }
                                        }
                                    });
                                } else {
                                    //m_ProgressBar.setVisibility(v.GONE);
                                    dialog.hide();
                                    Toast.makeText(getActivity().getApplicationContext(), "Failed to save", Toast.LENGTH_LONG).show();
                                    Log.d(getClass().getSimpleName(), "User Update error:" + e);
                                }
                            }
                        });
                    }


                }
            });

            m_logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    //m_ProgressBar.setVisibility(v.VISIBLE);
                    final ACProgressFlower dialog = new ACProgressFlower.Builder(getActivity())
                            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                            .themeColor(Color.WHITE)
                            .text("Logging out...")
                            .fadeColor(Color.DKGRAY).build();
                    dialog.show();
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("loggedIn", 0);

                    ArrayList<String> numList = new ArrayList<String>();

                    for (int i =1; i < 101; i ++){
                        numList.add("0");
                    }

                    user.put("table_numbers",numList);
                    user.put("restaurant_name", "");

                    user.saveInBackground(new SaveCallback() {
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
                                            //m_ProgressBar.setVisibility(v.GONE);
                                            dialog.hide();
                                            ParseUser user = ParseUser.getCurrentUser();
                                            user.logOutInBackground();
                                            cleanNotification();
                                            Toast.makeText(getActivity(), "Logged Out!", Toast.LENGTH_LONG).show();
                                            getActivity().finish();
                                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                                        } else {
                                            //m_ProgressBar.setVisibility(v.GONE);
                                            dialog.hide();
                                            Toast.makeText(getActivity().getApplicationContext(), "Failed to logout", Toast.LENGTH_LONG).show();
                                            getActivity().finish();
                                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                                        }
                                    }
                                });
                            } else {
                                //m_ProgressBar.setVisibility(v.GONE);
                                dialog.hide();
                                Toast.makeText(getActivity().getApplicationContext(), "Failed to logout", Toast.LENGTH_LONG).show();
                                getActivity().finish();
                                startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                            }
                        }
                    });
                }
            });

            m_delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ParseUser user = ParseUser.getCurrentUser();
                    user.deleteInBackground();
                    user.logOutInBackground();
                    Toast.makeText(getActivity().getApplicationContext(), "Deleted successfully!", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                    startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                }
            });


            return rootView;
        }

    protected void cleanNotification(){
        SharedPreferences preferences = getContext().getSharedPreferences("MyPreferences", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }
}


