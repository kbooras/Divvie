package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.parse.integratingfacebooktutorial.R;

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
    private static final int EMAIL_TAKEN = 203;
    private static final int USERNAME_TAKEN = 202;

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
        String emailTxt = mEmail.getText().toString();
        String fullNameTxt = mFullName.getText().toString();
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
            ParseUser user = new ParseUser();
            user.setUsername(emailTxt);
            user.setPassword(passwordTxt);
            user.setEmail(emailTxt);
            user.put("fullName", fullNameTxt);
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Log.v(TAG, "Sign up success!");
                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        startActivity(intent);
                    } else if (e.getCode() == EMAIL_TAKEN || e.getCode() == USERNAME_TAKEN) {
                        Toast.makeText(getApplicationContext(),
                                mResources.getString(R.string.account_email_exists_toast),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.v(TAG, "Sign up failed :(");

                    }
                }
            });
        }
    }

    private boolean isValidEmail(CharSequence emailTxt) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches();
    }
}
