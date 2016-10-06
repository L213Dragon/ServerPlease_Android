package bbbbb.com.socialdining.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ianpanton.serverplease.R;


public class SignUpFragment extends Fragment {

    public interface SignUpFragmentListener{
        void signUpOnClick(String username, String password);
    }

    public void setSignUpFragmentListener(SignUpFragmentListener signUpFragmentListener) {
        this.signUpFragmentListener = signUpFragmentListener;
    }

    private SignUpFragmentListener signUpFragmentListener;

    public static SignUpFragment newInstance(SignUpFragmentListener listener){
        SignUpFragment fragment = new SignUpFragment();
        fragment.setSignUpFragmentListener(listener);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

        final EditText passwordEditText = (EditText)rootView.findViewById(R.id.s_passwordField);
        final EditText emailEditText = (EditText)rootView.findViewById(R.id.s_emailField);
        final EditText confirmpasswordEditText = (EditText)rootView.findViewById(R.id.s_confirmpasswordFiled);
        Button signUpButton = (Button)rootView.findViewById(R.id.s_signupBtn);
        ProgressBar mProgressBar = (ProgressBar)rootView.findViewById(R.id.s_progressBar);

        emailEditText.requestFocus();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = passwordEditText.getText().toString();
                String confirmpassword = confirmpasswordEditText.getText().toString();
                String email = emailEditText.getText().toString();
                // String name = usernameEditText.getText().toString();

                password = password.trim();
                confirmpassword = confirmpassword.trim();
                email = email.trim();
                // name = name.trim();

                if (password.isEmpty() || email.isEmpty()) {
                    Toast.makeText(SignUpFragment.this.getContext(), R.string.signup_error_message, Toast.LENGTH_LONG).show();
                } else if (!password.equals(confirmpassword)){
                    Toast.makeText(SignUpFragment.this.getContext(), R.string.signup_confirm_error_message, Toast.LENGTH_LONG).show();
                } else if (password.length() < 6) {
                    Toast.makeText(SignUpFragment.this.getContext(), "Password must be at least 6 letters", Toast.LENGTH_LONG).show();
                } else if(signUpFragmentListener != null){
                    signUpFragmentListener.signUpOnClick(email, password);
                }
            }
        });

        return rootView;
    }
}
