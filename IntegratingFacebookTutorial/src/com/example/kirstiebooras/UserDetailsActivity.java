package com.example.kirstiebooras;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

public class UserDetailsActivity extends Activity {

    private ProfilePictureView userProfilePictureView;
    private TextView userNameView;
    private TextView userGenderView;
    private TextView userEmailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.userdetails);

        userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
        userNameView = (TextView) findViewById(R.id.userName);
        userGenderView = (TextView) findViewById(R.id.userGender);
        userEmailView = (TextView) findViewById(R.id.userEmail);

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            makeMeRequest();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Check if the user is currently logged
            // and show any cached content
            updateViewsWithProfileInfo();
        } else {
            // If the user is not logged in, go to the
            // activity showing the login view.
            startLoginActivity();
        }
    }

    private void makeMeRequest() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            // Create a JSON object to hold the profile info
                            JSONObject userProfile = new JSONObject();
                            try {
                                // Populate the JSON object
                                userProfile.put("facebookId", user.getId());
                                userProfile.put("name", user.getName());
                                if (user.getProperty("gender") != null) {
                                    userProfile.put("gender", user.getProperty("gender"));
                                }
                                if (user.getProperty("email") != null) {
                                    userProfile.put("email", user.getProperty("email"));
                                }

                                // Save the user profile info in a user property
                                ParseUser currentUser = ParseUser.getCurrentUser();
                                currentUser.put("profile", userProfile);
                                currentUser.saveInBackground();

                                // Show the user info
                                updateViewsWithProfileInfo();
                            } catch (JSONException e) {
                                Log.d(DivvieApplication.TAG, "Error parsing returned user data. " + e);
                            }

                        } else if (response.getError() != null) {
                            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) ||
                                    (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                Log.d(DivvieApplication.TAG, "The facebook session was invalidated." + response.getError());
                                logout();
                            } else {
                                Log.d(DivvieApplication.TAG,
                                        "Some other error: " + response.getError());
                            }
                        }
                    }
                }
        );
        request.executeAsync();
    }

    private void updateViewsWithProfileInfo() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser.has("profile")) {
            JSONObject userProfile = currentUser.getJSONObject("profile");
            try {

                if (userProfile.has("facebookId")) {
                    userProfilePictureView.setProfileId(userProfile.getString("facebookId"));
                } else {
                    // Show the default, blank user profile picture
                    userProfilePictureView.setProfileId(null);
                }

                if (userProfile.has("name")) {
                    userNameView.setText(userProfile.getString("name"));
                } else {
                    userNameView.setText("");
                }

                if (userProfile.has("gender")) {
                    userGenderView.setText(userProfile.getString("gender"));
                } else {
                    userGenderView.setText("");
                }

                if (userProfile.has("email")) {
                    userEmailView.setText(userProfile.getString("email"));
                } else {
                    userEmailView.setText("");
                }

            } catch (JSONException e) {
                Log.d(DivvieApplication.TAG, "Error parsing saved user data.");
            }
        }
    }

    public void onLogoutClick(View v) {
        logout();
    }

    private void logout() {
        // Log the user out
        ParseUser.logOut();

        // Go to the login view
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, SigninRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
