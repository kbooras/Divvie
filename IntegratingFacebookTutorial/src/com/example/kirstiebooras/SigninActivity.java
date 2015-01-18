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
import com.parse.RequestPasswordResetCallback;
import com.parse.integratingfacebooktutorial.R;

/**
 * Displays screen for a user to login with their email
 * and password.
 * Created by kirstiebooras on 1/15/15.
 */
public class SigninActivity extends Activity {

    static final String TAG = "Signin";

    public EditText email;
    public EditText password;
    public String emailTxt;
    public String passwordTxt;
    public final int INVALID_EMAIL = 125;
    public final int EMAIL_NOT_FOUND = 205;

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
                        Log.v(TAG, "Sign in success!");
                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        startActivity(intent);
                    } else {
                        Log.v(TAG, "Sign in failed :(" + e.toString());
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

    public void onForgotPasswordClick(View v) {
        final EditText resetPasswordEmail = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setMessage("Enter the email associated with your account")
                .setView(resetPasswordEmail)
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String resetPasswordEmailTxt = resetPasswordEmail.getText().toString();
                        resetPassword(resetPasswordEmailTxt);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    public void resetPassword(String resetPasswordEmailTxt) {
        ParseUser.requestPasswordResetInBackground(resetPasswordEmailTxt,
                new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // An email was successfully sent with reset
                            // instructions.
                            Toast.makeText(getApplicationContext(),
                                    "Reset email sent!",
                                    Toast.LENGTH_LONG);
                            Log.v(TAG, "reset email sent!");
                        } else {
                            // Something went wrong. Look at the ParseException
                            // to see what's up.
                            Log.v(TAG, String.valueOf(e.getCode()));
                            displayResetErrorMessage(e.getCode());
                        }
                    }
                });
    }

    public void displayResetErrorMessage(int errorCode) {
        String message;
        if (errorCode == INVALID_EMAIL) {
            message = "The email you entered was invalid";
        } else if (errorCode == EMAIL_NOT_FOUND) {
            message = "The email you entered is not associated with an account";
        } else {
            message = "The reset password failed. Please try again later.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password Failed")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }
}
