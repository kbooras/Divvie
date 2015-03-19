package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_activity);

        mResources = getResources();

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
            Toast.makeText(getApplicationContext(),
                    mResources.getString(R.string.complete_form_toast),
                    Toast.LENGTH_LONG).show();
        } else if (passwordTxt.length() < 6) {
            Toast.makeText(getApplicationContext(),
                    mResources.getString(R.string.password_length_toast),
                    Toast.LENGTH_LONG).show();
        } else if (!isValidEmail(emailTxt)) {
            Toast.makeText(getApplicationContext(),
                    mResources.getString(R.string.enter_valid_email_toast),
                    Toast.LENGTH_LONG).show();
        } else if (!passwordTxt.equals(reenterPasswordTxt)){
            Toast.makeText(getApplicationContext(),
                    mResources.getString(R.string.password_match_toast),
                    Toast.LENGTH_LONG).show();
        } else {
            // Save new user data into Parse.com Data Storage
            registerUser(emailTxt, passwordTxt, fullNameTxt);
        }
    }

    private void registerUser(final String email, final String password, final String fullName) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.USER_EMAIL, email);
        map.put("password", password);
        map.put(Constants.USER_FULL_NAME, fullName);
        ParseCloud.callFunctionInBackground("registerUser", map, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    Log.v(TAG, "no error ");
                    try {
                        ParseUser.logIn(email, password);
                    } catch (ParseException e2) {
                        Log.v(TAG, e2.toString());
                    }
                    Intent intent = new Intent(getApplicationContext(),
                            HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (e.getCode() == ParseException.EMAIL_TAKEN
                        || e.getCode() == ParseException.USERNAME_TAKEN) {
                    Log.v(TAG, "error code " + e.toString());
                        Toast.makeText(getApplicationContext(),
                                mResources.getString(R.string.account_email_exists_toast),
                                Toast.LENGTH_LONG).show();
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {

                }
                else {
                        Log.v(TAG, "Sign up failed :( " + e.getCode() + " " + e.toString());
                }
            }
        });
    }

    private boolean isValidEmail(CharSequence emailTxt) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches();
    }
}
