package bbbbb.com.socialdining.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.ianpanton.serverplease.R;


public class LoginFragment extends Fragment {
    public interface LoginFragmentListener{
        void signupTextOnClick();
        void forgotPasswordOnClick();
        void loginOnClick(String username, String password);
    }

    private LoginFragmentListener loginFragmentListener;

    public static LoginFragment newInstance(LoginFragmentListener listener){
        LoginFragment fragment = new LoginFragment();
        fragment.setLoginFragmentListener(listener);

        return fragment;
    }

    public void setLoginFragmentListener(LoginFragmentListener loginFragmentListener) {
        this.loginFragmentListener = loginFragmentListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        View signupText = rootView.findViewById(R.id.l_signupText);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loginFragmentListener != null){
                    loginFragmentListener.signupTextOnClick();
                }
            }
        });

        View forgotPassword = rootView.findViewById(R.id.l_forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loginFragmentListener != null){
                    loginFragmentListener.forgotPasswordOnClick();
                }
            }
        });

        final EditText emailEditText = (EditText)rootView.findViewById(R.id.l_emailField);
        final EditText passwordEditText = (EditText)rootView.findViewById(R.id.l_passwordField);

        View loginBtn = rootView.findViewById(R.id.l_loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                username = username.trim();
                password = password.trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginFragment.this.getContext(), R.string.login_error_message, Toast.LENGTH_SHORT).show();
                } else if(loginFragmentListener != null) {
                    loginFragmentListener.loginOnClick(username, password);
                }
            }
        });

        return rootView;
    }
}
