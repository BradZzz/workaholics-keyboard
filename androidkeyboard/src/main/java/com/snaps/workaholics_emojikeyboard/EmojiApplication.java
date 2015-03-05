package com.snaps.workaholics_emojikeyboard;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EmojiApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    Log.i("EmojiApplication","OnCreate");
    
    // Initialize Crash Reporting.
    ParseCrashReporting.enable(this);

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(this, getResources().getString(R.string.PARSE_TOKEN), getResources().getString(R.string.PARSE_SECRET_TOKEN));


    ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);
    
    ParsePush.subscribeInBackground("", new SaveCallback() {
      @Override
      public void done(ParseException e) {
      	Log.i("ServiceEmojiKeyboard","Done with subscribe");
        if (e == null) {
          Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
        } else {
          Log.e("com.parse.push", "failed to subscribe for push", e);
        }
      }
    });
  }
}