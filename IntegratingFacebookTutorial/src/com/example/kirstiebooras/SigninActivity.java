package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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

    private static final String TAG = "Signin";

    private EditText email;
    private EditText password;
    private static Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_activity);

        res = getResources();

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
    }

    public void onSignInClick(View v) {
        String emailTxt = email.getText().toString();
        String passwordTxt = password.getText().toString();

        if (emailTxt.equals("") || passwordTxt.equals("")) {
            Toast.makeText(getApplicationContext(), res.getString(R.string.complete_form_toast),
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
                                .setTitle(res.getString(R.string.sign_in))
                                .setMessage(res.getString(R.string.signin_failed_alert_message))
                                .setPositiveButton(res.getString(R.string.ok),
                                        new DialogInterface.OnClickListener() {
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
                .setTitle(res.getString(R.string.forgot_password_alert_title))
                .setMessage(res.getString(R.string.forgot_password_alert_message))
                .setView(resetPasswordEmail)
                .setPositiveButton(res.getString(R.string.reset),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String resetPasswordEmailTxt = resetPasswordEmail.getText().toString();
                                resetPassword(resetPasswordEmailTxt);
                            }
                })
                .setNegativeButton(res.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
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
                                    res.getString(R.string.reset_success_toast),
                                    Toast.LENGTH_LONG).show();
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
        int INVALID_EMAIL = 125;
        int EMAIL_NOT_FOUND = 205;
        if (errorCode == INVALID_EMAIL) {
            message = res.getString(R.string.reset_invalid_email_alert_message);
        } else if (errorCode == EMAIL_NOT_FOUND) {
            message = res.getString((R.string.reset_no_account_alert_message));
        } else {
            message = res.getString((R.string.reset_failed_alert_message));
        }

        new AlertDialog.Builder(this)
                .setTitle(res.getString((R.string.reset_failed_alert_title)))
                .setMessage(message)
                .setPositiveButton(res.getString((R.string.ok)), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }
}
