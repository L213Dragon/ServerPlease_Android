package bbbbb.com.socialdining.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ianpanton.serverplease.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import bbbbb.com.socialdining.fragment.ForgotPasswordFragment;
import bbbbb.com.socialdining.fragment.LoginFragment;
import bbbbb.com.socialdining.fragment.SignUpFragment;
import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;


public class LogInActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener,
                                                                SignUpFragment.SignUpFragmentListener{

    //private FragmentTransaction ft;
    private Fragment fragmentLogin, fragmentSignUp, fragmentForgotPassword;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        title = (TextView) findViewById(R.id.txtTitle);

        View btnBack = findViewById(R.id.llBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(title.getText().equals("Log in")) {
                    finish();
                }else if(title.getText().equals("Forgot Password") || title.getText().equals("Sign Up")){
                    title.setText("Log in");

                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.llFragmentCont, fragmentLogin);
                    ft.commit();
                }
            }
        });

        fragmentLogin = LoginFragment.newInstance(this);
        fragmentSignUp = SignUpFragment.newInstance(this);;
        fragmentForgotPassword = new ForgotPasswordFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.llFragmentCont, fragmentLogin);
        ft.commit();
    }

    @Override
    public void signupTextOnClick() {
        title.setText("Sign Up");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.llFragmentCont, fragmentSignUp);
        ft.commit();
    }

    @Override
    public void forgotPasswordOnClick() {
        title.setText("Forgot Password");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.llFragmentCont, fragmentForgotPassword);
        ft.commit();
    }

    @Override
    public void loginOnClick(final String username, final String password) {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(LogInActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Login")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                dialog.hide();
//                            mProgressBar.setVisibility(v.GONE);

                if (e == null) {
                    int loggedIn = user.getInt("loggedIn");
                    if (loggedIn == 1) {
                        Toast.makeText(LogInActivity.this, "You have already logged in on the other device!", Toast.LENGTH_SHORT).show();
                        user.logOutInBackground();
                        cleanNotification();
                        return;
                    }

                    user.put("loggedIn", 1);
                    user.saveInBackground();
                    // Success!
                    // register email for push notification
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("email", username);
                    installation.put("loggedin", 1);
                    installation.saveInBackground();

                    Intent intent = new Intent(LogInActivity.this, WaiterMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    LogInActivity.this.finish();
                    startActivity(intent);
                } else {
                    Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void cleanNotification(){
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }

    @Override
    public void signUpOnClick(final String username, final String password) {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(LogInActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Registering...")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();

        ParseUser newUser = new ParseUser();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(username);
        //newUser.put("name",name);
        newUser.put("loggedIn", 1);
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                //mProgressBar.setVisibility(v.GONE);
                dialog.hide();

                if (e == null) {
                    // Success!
                    // register email for push notification
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("email", username);
                    installation.put("loggedin", 1);
                    installation.saveInBackground();


                    Intent intent = new Intent(LogInActivity.this, WaiterMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    LogInActivity.this.finish();
                    startActivity(intent);
                }
                else {
                    Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
