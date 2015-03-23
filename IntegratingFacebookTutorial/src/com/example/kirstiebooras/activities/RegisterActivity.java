package com.example.kirstiebooras.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kirstiebooras.helpers.Constants;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.HashMap;

/**
 * Displays screen for a user to register for an account.
 * Created by kirstiebooras on 1/15/15.
 */
public class RegisterActivity extends Activity {

    private static final String TAG = "Register";

    private EditText mEmail;
    private EditText mFullName;
    private EditText mPassword;
    private EditText mReenterPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_activity);

        mEmail = (EditText) findViewById(R.id.email);
        mFullName = (EditText) findViewById(R.id.fullName);
        mPassword = (EditText) findViewById(R.id.password);
        mReenterPassword = (EditText) findViewById(R.id.reenterPassword);

    }

    public void onRegisterClick(View v) {
        final String emailTxt = mEmail.getText().toString().toLowerCase();
        final String fullNameTxt = mFullName.getText().toString();
        String passwordTxt = mPassword.getText().toString();
        String reenterPasswordTxt = mReenterPassword.getText().toString();

        // User must fill up the form
        if (emailTxt.equals("") || fullNameTxt.equals("") || passwordTxt.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.complete_form_toast),
                    Toast.LENGTH_LONG).show();
        } else if (passwordTxt.length() < 6) {
            Toast.makeText(getApplicationContext(), getString(R.string.password_length_toast),
                    Toast.LENGTH_LONG).show();
        } else if (!isValidEmail(emailTxt)) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_valid_email_toast),
                    Toast.LENGTH_LONG).show();
        } else if (!passwordTxt.equals(reenterPasswordTxt)){
            Toast.makeText(getApplicationContext(), getString(R.string.password_match_toast),
                    Toast.LENGTH_LONG).show();
        } else {
            // Save new user data into Parse.com Data Storage
            registerUser(emailTxt, passwordTxt, fullNameTxt);
        }
    }

    public void registerUser(final String email, final String password, final String fullName) {
        Log.d(TAG, "regusterUser");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.USER_EMAIL, email);
        map.put("password", password);
        map.put(Constants.USER_FULL_NAME, fullName);
        ParseCloud.callFunctionInBackground("registerUser", map, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    try {
                        ParseUser.logIn(email, password);
                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } catch (ParseException e2) {
                        Log.v(TAG, "Error logging in new user: " + e2.getMessage());
                    }
                } else if (e.getCode() == ParseException.EMAIL_TAKEN
                        || e.getCode() == ParseException.USERNAME_TAKEN) {
                    Log.v(TAG, "error code " + e.toString());
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.account_email_exists_toast), Toast.LENGTH_LONG).show();
                }
                else {
                    Log.v(TAG, "Sign up failed :( " + e.getMessage());
                }
            }
        });
    }

    private boolean isValidEmail(CharSequence emailTxt) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches();
    }
}
