package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

/**
 * Displays screen for a user to login with their email
 * and password.
 * Created by kirstiebooras on 1/15/15.
 */
public class SigninActivity extends Activity {

    public EditText email;
    public EditText password;
    public String emailTxt;
    public String passwordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_activity);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
    }

    public void onSignInClick(View v) {
        emailTxt = email.getText().toString();
        passwordTxt = password.getText().toString();

        if (emailTxt.equals("") || passwordTxt.equals("")) {
            Toast.makeText(getApplicationContext(), "Please complete the form",
                    Toast.LENGTH_LONG).show();
        } else {
            ParseUser.logInInBackground(emailTxt, passwordTxt, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null) {
                        Log.v("signin", "success!");
                        Intent intent = new Intent(getApplicationContext(),
                                UserDetailsActivity.class);
                        startActivity(intent);
                    } else {
                        Log.v("signin", "failed " + e.toString());
                        new AlertDialog.Builder(SigninActivity.this)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .setTitle("Login")
                                .setMessage("Email or password invalid.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                }
            });
        }
    }
}
