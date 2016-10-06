package bbbbb.com.socialdining;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ianpanton.serverplease.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ForgotPasswordActivity extends AppCompatActivity {

    protected Button forgotPasswordBtn;
    protected TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        forgotPasswordBtn = (Button)findViewById(R.id.fp_button);
        View m_backBtn = findViewById(R.id.llBack);
        m_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                email = (TextView)findViewById(R.id.fp_email);
                String str_email = email.getText().toString();
                if (str_email.equals("")){
                    Toast.makeText(ForgotPasswordActivity.this, "Please input the email address!", Toast.LENGTH_LONG).show();
                    return;
                }
                ParseUser.requestPasswordResetInBackground(str_email,
                        new RequestPasswordResetCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    // An email was successfully sent with reset instructions.
                                    Toast.makeText(ForgotPasswordActivity.this, "Successfully sent new password to your email", Toast.LENGTH_LONG).show();
                                } else {
                                    // Something went wrong. Look at the ParseException to see what's up.
                                    Toast.makeText(ForgotPasswordActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }
}
