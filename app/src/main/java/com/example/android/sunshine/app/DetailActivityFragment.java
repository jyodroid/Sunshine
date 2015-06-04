package com.example.android.sunshine.app;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private final static String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private final static String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    private String mForecastString;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View detailView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView textView = (TextView) detailView.findViewById(R.id.detail_textview);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            mForecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
                    textView.setText(mForecastString);
        }else {
            textView.setText("No weather data!!");
        }

        return detailView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail, menu);

        //Locate Menu Item with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        //Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(shareActionProvider != null){
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }else{
            Log.d(LOG_TAG, "ShareActionProvider is null");
        }
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastString+FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }
}
