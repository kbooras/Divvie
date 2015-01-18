package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

/**
 * Shows the pending group transactions the user has.
 * Shows who has paid them, who still owes them, and completed group charges.
 * Created by kirstiebooras on 1/16/15.
 */
public class HomeActivity extends Activity {

    static final String TAG = "Home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_activity);

        // Check if there is a currently logged in user
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // If the current user is null, send to sign in or register
            Intent intent = new Intent(getApplicationContext(), SigninRegisterActivity.class);
            startActivity(intent);
        }
        else if (ParseFacebookUtils.isLinked(currentUser)) {
            // If the current user is not null and is linked to a Facebook account
            // send to user details activity
            Intent intent = new Intent(getApplicationContext(), UserDetailsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.logout:
                ParseUser.logOut();
                Log.v(TAG, "User signed out!");
                startSigninRegisterActivity();
                break;

            case R.id.add:
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // If the current user is null, send to sign in or register
            startSigninRegisterActivity();
        }
    }

    private void startSigninRegisterActivity() {
        Intent intent = new Intent(this, SigninRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
