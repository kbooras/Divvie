package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
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

    static final String TAG = "Register";

    public EditText email;
    public EditText fullName;
    public EditText password;
    public EditText reenterPassword;
    public String emailTxt;
    public String fullNameTxt;
    public String passwordTxt;
    public String reenterPasswordTxt;
    final int EMAIL_TAKEN = 203;
    final int USERNAME_TAKEN = 202;
    final String FULL_NAME = "fullName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_activity);

        email = (EditText) findViewById(R.id.email);
        fullName = (EditText) findViewById(R.id.fullName);
        password = (EditText) findViewById(R.id.password);
        reenterPassword = (EditText) findViewById(R.id.reenterPassword);

    }

    public void onRegisterClick(View v) {
        emailTxt = email.getText().toString();
        fullNameTxt = fullName.getText().toString();
        passwordTxt = password.getText().toString();
        reenterPasswordTxt = reenterPassword.getText().toString();

        // User must fill up the form
        if (emailTxt.equals("") || fullNameTxt.equals("") || passwordTxt.equals("")) {
            Toast.makeText(getApplicationContext(), "Please complete the register form",
                    Toast.LENGTH_LONG).show();
        } else if (passwordTxt.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password must be at least 6 characters.",
                    Toast.LENGTH_LONG).show();
        } else if (!isValidEmail(emailTxt)) {
            Toast.makeText(getApplicationContext(), "Please enter a valid email.",
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
            user.put(FULL_NAME, fullNameTxt);
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
                                "There is already an account with this email",
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
