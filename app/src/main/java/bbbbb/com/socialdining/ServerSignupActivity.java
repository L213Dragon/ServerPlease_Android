package bbbbb.com.socialdining;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ianpanton.serverplease.R;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class ServerSignupActivity extends AppCompatActivity {


    protected EditText usernameEditText;
    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected Button signUpButton;
    protected EditText confirmpasswordEditText;
    protected ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        passwordEditText = (EditText)findViewById(R.id.ss_passwordField);
        emailEditText = (EditText)findViewById(R.id.ss_emailField);
        confirmpasswordEditText = (EditText)findViewById(R.id.ss_confirmpasswordFiled);
        signUpButton = (Button)findViewById(R.id.ss_signupBtn);
        mProgressBar = (ProgressBar)findViewById(R.id.s_progressBar);
        // usernameEditText = (EditText)findViewById(R.id.ss_usernameField);

        ImageButton m_backBtn = (ImageButton)findViewById(R.id.btnBack);
        m_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        emailEditText.requestFocus();


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String password = passwordEditText.getText().toString();
                String confirmpassword = confirmpasswordEditText.getText().toString();
                String email = emailEditText.getText().toString();
                // String name = usernameEditText.getText().toString();

                password = password.trim();
                confirmpassword = confirmpassword.trim();
                email = email.trim();
                // name = name.trim();

                if (password.isEmpty() || email.isEmpty()) {
                    Toast.makeText(ServerSignupActivity.this, R.string.signup_error_message, Toast.LENGTH_LONG).show();
                } else if (!password.equals(confirmpassword)){
                    Toast.makeText(ServerSignupActivity.this, R.string.signup_confirm_error_message, Toast.LENGTH_LONG).show();
                } else if (password.length() < 6) {
                    Toast.makeText(ServerSignupActivity.this, "Password must be at least 6 letters", Toast.LENGTH_LONG).show();
                } else {
                    //mProgressBar.setVisibility(v.VISIBLE);
                    final ACProgressFlower dialog = new ACProgressFlower.Builder(ServerSignupActivity.this)
                            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                            .themeColor(Color.WHITE)
                            .text("Registering...")
                            .fadeColor(Color.DKGRAY).build();
                    dialog.show();

                    ParseUser newUser = new ParseUser();
                    newUser.setUsername(email);
                    newUser.setPassword(password);
                    newUser.setEmail(email);
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
                                String username = emailEditText.getText().toString();
                                installation.put("email", username);
                                installation.put("loggedin", 1);
                                installation.saveInBackground();


                                Intent intent = new Intent(ServerSignupActivity.this, ServerMapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                ServerSignupActivity.this.finish();
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(ServerSignupActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}

