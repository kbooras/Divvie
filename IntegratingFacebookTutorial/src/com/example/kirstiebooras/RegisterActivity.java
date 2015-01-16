package com.example.kirstiebooras;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.parse.integratingfacebooktutorial.R;

import java.text.ParseException;

/**
 * Created by kirstiebooras on 1/15/15.
 */
public class RegisterActivity extends Activity {

    public EditText email;
    public EditText password;
    public EditText reenterPassword;
    public String emailTxt;
    public String passwordTxt;
    public String reenterPasswordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_activity);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        reenterPassword = (EditText) findViewById(R.id.reenterPassword);

    }

    public void onRegisterClick(View v) {
        emailTxt = email.getText().toString();
        passwordTxt = password.getText().toString();
        reenterPasswordTxt = reenterPassword.getText().toString();

        // User must fill up the form
        // TODO: check for valid password and email
        if (emailTxt.equals("") || passwordTxt.equals("")) {
            Toast.makeText(getApplicationContext(), "Please complete the register form",
                    Toast.LENGTH_LONG).show();
        } else if (!passwordTxt.equals(reenterPasswordTxt)){
            Toast.makeText(getApplicationContext(), "Passwords do not match",
                    Toast.LENGTH_LONG).show();
        } else {
            // Save new user data into Parse.com Data Storage
            ParseUser user = new ParseUser();
            user.setUsername(emailTxt);
            user.setPassword(passwordTxt);
            user.setEmail(emailTxt);
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Log.v("register", "Success!");
                    } else {
                        Log.v("register", "Failed :(");
                    }
                }
            });
        }
    }
}
