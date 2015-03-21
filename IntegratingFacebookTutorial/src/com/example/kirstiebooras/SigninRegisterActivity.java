package com.example.kirstiebooras;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.Arrays;
import java.util.List;

/**
 * Displays the screen for a user to sign in, register, or
 * sign in through Facebook.
 */
public class SigninRegisterActivity extends Activity {

    private static final String TAG = "SigninRegister";
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_register_activity);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            Log.v(TAG, "No current user");
        }
        /* Check if there is a currently logged in user
        // and it's linked to a Facebook account.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            // Go to the user info activity
            showUserDetailsActivity();
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    public void onRegisterClick(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    public void onSignInClick(View v) {
        Intent intent = new Intent(this, SigninActivity.class);
        startActivity(intent);
    }

    public void onFacebookSignInClick(View v) {
        mProgressDialog = ProgressDialog.show(SigninRegisterActivity.this, "", "Logging in...", true);

        List<String> permissions = Arrays.asList("public_profile", "email");
        // NOTE: for extended permissions, like "user_about_me", your app must be reviewed by the Facebook team
        // (https://developers.facebook.com/docs/facebook-login/permissions/)

        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                mProgressDialog.dismiss();
                if (user == null) {
                    Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d(TAG, "User signed up and logged in through Facebook!");
                    // Set user's full name and email
                    setUserInfo();
                    showHomeActivity();
                } else {
                    Log.d(TAG, "User logged in through Facebook!");
                    showHomeActivity();
                }
            }
        });
    }

    private void setUserInfo() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            // Save the user profile info in a user property
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            currentUser.put("facebookId", user.getId());
                            currentUser.put(Constants.USER_FULL_NAME, user.getName());
                            if (user.getProperty("email") != null) {
                                currentUser.put(Constants.USER_EMAIL, user.getProperty("email"));
                            }
                            currentUser.saveInBackground();

                        } else {
                            Log.d(TAG, "setFullName error: " + response.getError());
                        }
                    }
                }
        );
        request.executeAsync();
    }

    private void showHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
