package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private ArrayAdapter<String> adapter;


    public ForecastFragment() {
    }

    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adapter = new ArrayAdapter<String>(
                getActivity().getBaseContext(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getActivity().getBaseContext();
                //Text to be show
                CharSequence text = adapter.getItem(position);
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context,text,duration);
                toast.show();

                Intent detailActivityIntent = new Intent(context, DetailActivity.class);
                detailActivityIntent.putExtra(Intent.EXTRA_TEXT,text);
                startActivity(detailActivityIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final int NUMDAYS = 7;

        @Override
        protected String[] doInBackground(String... params) {

            //URL Connection for rest services
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String forecastJsonStr = null;

            try {

                Uri.Builder uriBuilder = new Uri.Builder();

                uriBuilder.scheme("http");
                uriBuilder.authority("api.openweathermap.org");
                uriBuilder.appendPath("data");
                uriBuilder.appendPath("2.5");
                uriBuilder.appendPath("forecast");
                uriBuilder.appendPath("daily");
                uriBuilder.appendQueryParameter("q", params[0]);
                uriBuilder.appendQueryParameter("cnt",String.valueOf(NUMDAYS));
                uriBuilder.appendQueryParameter("mode", "json");
                uriBuilder.appendQueryParameter("units",params[1]);

                URL url = new URL(uriBuilder.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Response
                InputStream stream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();

                if (stream == null){
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(stream));

                String line;
                while ((line = bufferedReader.readLine())!= null){
                    stringBuffer.append(line);
                }

                if (stringBuffer.length()==0){
                    return null;
                }

                forecastJsonStr = stringBuffer.toString();

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
            }finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if (bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error", e);
                    }
                }
            }
            try {
                String[] weatherData = getWeatherDataFromJson(forecastJsonStr, NUMDAYS);
                return weatherData;
            } catch (JSONException e) {
                Log.e(LOG_TAG,e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] weatherData) {
            super.onPostExecute(weatherData);
            adapter.clear();
            adapter.addAll(weatherData);
/*            for (String weaterDay: weatherData){
                adapter.add(weaterDay);
            }*/
        }
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrs;

    }

    private void updateWeather(){
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = preferences.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        String units = preferences.getString(
                getString(R.string.pref_units_key),
                getString(R.string.pref_location_default));

        fetchWeatherTask.execute(location, units);
    }
}
