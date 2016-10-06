package bbbbb.com.socialdining;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.ianpanton.serverplease.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ServerLoginActivity extends AppCompatActivity {

    public void goToSignup(View view){
        Intent i = new Intent(ServerLoginActivity.this, ServerSignupActivity.class);
        startActivity(i);
    }

    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected Button loginButton;
    protected TextView signUpTextView;
    protected TextView forgotPasswordTextView;
    protected ProgressBar mProgressBar;
    Button mBtnFb;
    Profile mFbProfile;
    ParseUser parseUser;
    String name = null, email = null;
    ACProgressFlower m_dialog;

    public static final List<String> mPermissions = new ArrayList<String>() {{
        add("public_profile");
        add("email");
    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        signUpTextView = (TextView)findViewById(R.id.l_signupText);
        forgotPasswordTextView = (TextView)findViewById(R.id.l_forgotPassword);
        emailEditText = (EditText)findViewById(R.id.l_emailField);
        passwordEditText = (EditText)findViewById(R.id.sl_passwordField);
        loginButton = (Button)findViewById(R.id.l_loginBtn);
        mBtnFb = (Button)findViewById(R.id.fbLoginButton);


        ImageButton m_backBtn = (ImageButton)findViewById(R.id.btnBack);
        m_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        m_dialog = new ACProgressFlower.Builder(ServerLoginActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Login")
                .fadeColor(Color.DKGRAY).build();


        emailEditText.requestFocus();

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServerLoginActivity.this, ServerSignupActivity.class);
                startActivity(intent);
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServerLoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String username = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                username = username.trim();
                password = password.trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(ServerLoginActivity.this, R.string.login_error_message, Toast.LENGTH_SHORT).show();
                } else {

//                    mProgressBar.setVisibility(v.VISIBLE);
                    final ACProgressFlower dialog = new ACProgressFlower.Builder(ServerLoginActivity.this)
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
                                    Toast.makeText(ServerLoginActivity.this, "You have already logged in on the other device!", Toast.LENGTH_SHORT).show();
                                    user.logOutInBackground();
                                    cleanNotification();
                                    return;
                                }

                                user.put("loggedIn", 1);
                                user.saveInBackground();
                                // Success!
                                // register email for push notification
                                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                String username = emailEditText.getText().toString();
                                installation.put("email", username);
                                installation.put("loggedin", 1);
                                installation.saveInBackground();

                                Intent intent = new Intent(ServerLoginActivity.this, ServerMapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                ServerLoginActivity.this.finish();
                                startActivity(intent);
                            } else {
                                Toast.makeText(ServerLoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        mBtnFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseFacebookUtils.logInWithReadPermissionsInBackground(ServerLoginActivity.this, mPermissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {

                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                            m_dialog.show();
                            getUserDetailsFromFB();
                        } else {
                            Log.d("MyApp", "User logged in through Facebook!");
                            m_dialog.show();
                            getUserDetailsFromParse();
                        }
                    }
                });

            }
        });
    }

    protected void showProgress(String text){
        ACProgressFlower dialog = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text(text)
                .fadeColor(Color.DKGRAY).build();
        dialog.show();
    }

    protected void cleanNotification(){
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void getUserDetailsFromFB() {
        // Suggested by https://disqus.com/by/dominiquecanlas/
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture");


        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            /* handle the result */
                        try {

                            Log.d("Response", response.getRawResponse());

                            email = response.getJSONObject().getString("email");
                            name = response.getJSONObject().getString("name");

                            parseUser = ParseUser.getCurrentUser();
                            parseUser.put("email", email);
                            parseUser.put("loggedIn", 1);
                            parseUser.saveInBackground();
                            // Success!
                            // register email for push notification
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            String username = parseUser.getUsername();
                            installation.put("email", username);
                            installation.put("loggedin", 1);
                            installation.saveInBackground();

                            m_dialog.hide();

                            Intent intent = new Intent(ServerLoginActivity.this, ServerMapActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            ServerLoginActivity.this.finish();
                            startActivity(intent);

                        } catch (JSONException e) {
                            m_dialog.hide();
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();

    }


    private void getUserDetailsFromParse() {

        parseUser = ParseUser.getCurrentUser();

        int loggedIn = parseUser.getInt("loggedIn");
        if (loggedIn == 1) {
            Toast.makeText(ServerLoginActivity.this, "You have already logged in on the other device!", Toast.LENGTH_SHORT).show();
            parseUser.logOutInBackground();
            cleanNotification();
            return;
        }

        parseUser.put("loggedIn", 1);
        parseUser.saveInBackground();
        // Success!
        // register email for push notification
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        String username = emailEditText.getText().toString();
        installation.put("email", username);
        installation.put("loggedin", 1);
        installation.saveInBackground();

        m_dialog.hide();

        Intent intent = new Intent(ServerLoginActivity.this, ServerMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ServerLoginActivity.this.finish();
        startActivity(intent);

        Toast.makeText(ServerLoginActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();

    }
}
