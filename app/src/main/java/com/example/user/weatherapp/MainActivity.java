package com.example.user.weatherapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WeatherCityFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class WeatherCityFragment extends Fragment {
        private final String privateKey = "20714408113343d654e4d4d3cc828";
        private static final String LOG_TAG ="WEATHER_CITY_FRAGMENT";
        EditText mEditCity;
        TextView mTextWeather;


        public WeatherCityFragment() {
        }

        private URL constructURLQuery(String city) throws MalformedURLException {

            //api.worldweatheronline.com/free/v2/weather.ashx?q=Medellin&format=json&num_of_days=5&key=20714408113343d654e4d4d3cc828

            final String WEATHER_BASE_URL = "api.worldweatheronline.com";
            final String GET_METHOD = "weather.ashx";
            final String API_KIND = "free";
            final String API_VERSION = "v2";
            final String KEY = "20714408113343d654e4d4d3cc828";
            final String format = "json";

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(WEATHER_BASE_URL)
                    .appendPath(API_KIND)
                    .appendPath(API_VERSION)
                    .appendPath(GET_METHOD)
                    .appendQueryParameter("q", city)
                    .appendQueryParameter("format", format)
                    .appendQueryParameter("num_of_days","1")
                    .appendQueryParameter("key",KEY);

            Uri uri = builder.build();
            Log.d(LOG_TAG, "Builded uri: " + uri.toString());
            return  new URL(uri.toString());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            Button buttonGetRepos = (Button)rootView.findViewById(R.id.button_get_weather);
            mEditCity = (EditText)rootView.findViewById(R.id.edit_text_city);
            mTextWeather = (TextView)rootView.findViewById(R.id.text_view_city_weather);

            buttonGetRepos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String city = mEditCity.getText().toString();
                    String message = String.format(getString(R.string.getting_weather_for_city),city);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    new FetchWeatherTask().execute(city);

                }
            });

            return rootView;
        }

        private String readFullResponse(InputStream inputStream) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String response = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            if (stringBuilder.length() > 0) {
                response = stringBuilder.toString();
            }
            Log.d("longitud->",Integer.toString(response.length()));
            Log.d("longitud sb ->",Integer.toString(stringBuilder.length()) );
            return response;
        }

        class  FetchWeatherTask extends AsyncTask<String,Void,String> {

            private final  String CLOUDCOVER = "cloudcover";
            private final  String HUMIDITY = "humidity";
            private final  String VISIBILITY = "visibility";
            private final String TEMP_C = "temp_C";

            private String parseResponse(String response) {
                final String REPO_NAME = "name";
                String listOfRepos;
                List<String> repos = new ArrayList<>();
                List<String> weatherIconUrl = new ArrayList<>();
                try {
                    JSONObject current_condition,req,object,srcimg;
                    JSONObject reposData = new JSONObject(response);

                    req = new JSONObject(reposData.getString("data"));
                    JSONArray reposJsonArray = new JSONArray(req.getString("current_condition"));
                    Log.d("ARRAY",Integer.toString(reposJsonArray.length()));
                    for(int i = 0; i < reposJsonArray.length(); i++) {
                        object = reposJsonArray.getJSONObject(i);
                        repos.add(CLOUDCOVER +":"+object.getString(CLOUDCOVER)+"\n");
                        repos.add(HUMIDITY+":" +object.getString(HUMIDITY)+"\n");
                        repos.add(VISIBILITY+":"+object.getString(VISIBILITY)+"\n");
                        repos.add(TEMP_C+":" +object.getString(TEMP_C));
                    }

                } catch (JSONException e) {
                    Log.e("ERROR", "exception", e);
                }
                listOfRepos = TextUtils.join("", repos);
                return listOfRepos;
            }

            @Override
            protected String doInBackground(String... params) {
                String city;
                String response ="";
                String listofRepos="";
                if(params.length > 0){
                    city = params[0];
                }
                else
                {
                    city = "Pereira";
                }
                try {
                    URL url = constructURLQuery(city);
                    HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
                    try {
                        response= readFullResponse(httpConnection.getInputStream());
                        listofRepos = parseResponse(response);
                    }
                    finally {
                        httpConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return listofRepos;
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);

                mTextWeather.setText(response);
                Log.d(LOG_TAG,"response->> "+response);

            }
        }
    }
}
