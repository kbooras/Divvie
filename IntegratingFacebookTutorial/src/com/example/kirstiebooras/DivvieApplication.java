package com.example.kirstiebooras;

import android.app.Application;

import com.parse.Parse;
import com.parse.integratingfacebooktutorial.R;

/**
 * Created by kirstiebooras on 2/6/15.
 */
public class DivvieApplication extends Application {

    public static final String TAG = "DivvieApplication";
    public ParseTools mParseTools;

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this,
                getString(R.string.PARSE_APPLICATION_ID),
                getString(R.string.PARSE_CLIENT_ID)
        );

        mParseTools = new ParseTools(this);
    }

    public ParseTools getParseTools() {
        return mParseTools;
    }


}