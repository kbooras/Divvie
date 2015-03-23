package com.example.kirstiebooras.activities;

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

    private static final String TAG = "Signin";

    private EditText mEmail;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_activity);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
    }

    public void onSignInClick(View v) {
        String emailTxt = mEmail.getText().toString().toLowerCase();
        String passwordTxt = mPassword.getText().toString();

        if (emailTxt.equals("") || passwordTxt.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.complete_form_toast),
                    Toast.LENGTH_LONG).show();
        } else {
            ParseUser.logInInBackground(emailTxt, passwordTxt, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null) {
                        Log.v(TAG, "Sign in success!");
                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                        Log.v(TAG, "No network connection");
                        new AlertDialog.Builder(SigninActivity.this)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .setTitle(getString(R.string.no_network_connection))
                                .setMessage(getString(R.string.network_connection_sign_in_alert_message))
                                .setPositiveButton(getString(R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }
                                )
                                .show();
                    } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        new AlertDialog.Builder(SigninActivity.this)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .setTitle(getString(R.string.sign_in))
                                .setMessage(getString(R.string.sign_in_failed_alert_message))
                                .setPositiveButton(getString(R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                .show();
                    } else {
                        Log.v(TAG, "Sign in failed :( " + e.getCode() + " " + e.toString());
                    }
                }
            });
        }
    }

    public void onForgotPasswordClick(View v) {
        final EditText resetPasswordEmail = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.forgot_password_alert_title))
                .setMessage(getString(R.string.forgot_password_alert_message))
                .setView(resetPasswordEmail)
                .setPositiveButton(getString(R.string.reset),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String resetPasswordEmailTxt = resetPasswordEmail.getText().toString();
                                resetPassword(resetPasswordEmailTxt);
                            }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void resetPassword(String resetPasswordEmailTxt) {
        ParseUser.requestPasswordResetInBackground(resetPasswordEmailTxt,
                new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // An email was successfully sent with reset instructions.
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.reset_success_toast),
                                    Toast.LENGTH_LONG).show();
                            Log.v(TAG, "reset email sent!");
                        } else {
                            // Something went wrong. Look at the ParseException to see what's up.
                            Log.v(TAG, String.valueOf(e.getCode()));
                            displayResetErrorMessage(e.getCode());
                        }
                    }
                });
    }

    private void displayResetErrorMessage(int errorCode) {
        String message;
        if (errorCode == ParseException.INVALID_EMAIL_ADDRESS) {
            message = getString(R.string.reset_invalid_email_alert_message);
        } else if (errorCode == ParseException.EMAIL_NOT_FOUND) {
            message = getString((R.string.reset_no_account_alert_message));
        } else {
            message = getString((R.string.reset_failed_alert_message));
        }

        new AlertDialog.Builder(this)
                .setTitle(getString((R.string.reset_failed_alert_title)))
                .setMessage(message)
                .setPositiveButton(getString((R.string.ok)), null)
                .show();
    }
}
