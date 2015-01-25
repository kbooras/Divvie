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

    private EditText email;
    private EditText fullName;
    private EditText password;
    private EditText reenterPassword;
    private static Resources res;
    private final int EMAIL_TAKEN = 203;
    private final int USERNAME_TAKEN = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_activity);

        res = getResources();

        email = (EditText) findViewById(R.id.email);
        fullName = (EditText) findViewById(R.id.fullName);
        password = (EditText) findViewById(R.id.password);
        reenterPassword = (EditText) findViewById(R.id.reenterPassword);

    }

    public void onRegisterClick(View v) {
        String emailTxt = email.getText().toString();
        String fullNameTxt = fullName.getText().toString();
        String passwordTxt = password.getText().toString();
        String reenterPasswordTxt = reenterPassword.getText().toString();

        // User must fill up the form
        if (emailTxt.equals("") || fullNameTxt.equals("") || passwordTxt.equals("")) {
            Toast.makeText(getApplicationContext(),
                    res.getString(R.string.complete_form_toast),
                    Toast.LENGTH_LONG).show();
        } else if (passwordTxt.length() < 6) {
            Toast.makeText(getApplicationContext(),
                    res.getString(R.string.password_length_toast),
                    Toast.LENGTH_LONG).show();
        } else if (!isValidEmail(emailTxt)) {
            Toast.makeText(getApplicationContext(),
                    res.getString(R.string.enter_valid_email_toast),
                    Toast.LENGTH_LONG).show();
        } else if (!passwordTxt.equals(reenterPasswordTxt)){
            Toast.makeText(getApplicationContext(),
                    res.getString(R.string.password_match_toast),
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
                                res.getString(R.string.account_email_exists_toast),
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
