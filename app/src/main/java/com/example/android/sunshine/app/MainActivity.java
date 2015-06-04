package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(LOG_TAG, "onCreate");

    }

    /*Testing the Activity LifeCycle*/
    @Override
    protected void onStart(){
        super.onStart();
        Log.v(LOG_TAG, "onStart");
    }

    /*Testing the Activity LifeCycle*/
    @Override
    protected void onResume(){
        super.onResume();
        Log.v(LOG_TAG, "onResume");
    }

    /*Testing the Activity LifeCycle*/
    @Override
    protected void onPause(){
        super.onPause();
        Log.v(LOG_TAG, "onPause");
    }

    /*Testing the Activity LifeCycle*/
    @Override
    protected void onStop(){
        super.onStop();
        Log.v(LOG_TAG, "onStop");
    }

    /*Testing the Activity LifeCycle*/
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.v(LOG_TAG, "onDestoy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings){

            Intent settingActivityIntent = new Intent(
                    this,
                    SettingsActivity.class);
            startActivity(settingActivityIntent);
            return true;
        }
        if (id == R.id.location_map) {
            openPreferedLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openPreferedLocationInMap(){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String preferedLocation = preferences.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        Intent intentMap = new Intent(Intent.ACTION_VIEW);
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",preferedLocation)
                .build();
        intentMap.setData(geoLocation);
        if (intentMap.resolveActivity(getPackageManager()) == null){
            Toast toast = Toast.makeText(
                    this,
                    "No map resources on your phone",
                    Toast.LENGTH_SHORT);
            toast.show();
        }else {
            startActivity(intentMap);
        }
    }
}
