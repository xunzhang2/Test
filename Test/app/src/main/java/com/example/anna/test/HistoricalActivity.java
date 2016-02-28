package com.example.anna.test;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class HistoricalActivity extends Activity {
    private PullToRefreshListView lv;
    private ArrayAdapter<String> adapter;
    private String city_global = "";
    private String weather_global = "";
    private String temperature_previous = "";
    private String temperature_global = "";
    final public String TAG = "HistoricalActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical);

// get data transmitted from MainActivity
        Intent intent = getIntent();
        city_global = intent.getStringExtra("city");
        weather_global = intent.getStringExtra("weather");
        temperature_global = intent.getStringExtra("temperature");
        temperature_previous = temperature_global;

// initialize the ListView with current weather and temperature
        lv = (PullToRefreshListView) findViewById(R.id.mylv);
        List<String> list = new ArrayList<>();
        list.add("Weather: " + weather_global);
        list.add("Temperature: " + temperature_global);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
// listen to refreshing
        lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            getWeather();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // wait for the returned data
                        while (temperature_global.equals(""))
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        return null;
                    }

                    /*
                    If there is new temperature, update.
                    Otherwise, notify user that this is the lastest temperature.
                     */
                    protected void onPostExecute(Void result) {
                        if (!temperature_global.equals(temperature_previous)) {
                            adapter.add("Temperature: " + temperature_global);
                            temperature_previous = temperature_global;
                        } else {
                            adapter.add(temperature_global + " is the latest temperature.");
                        }
                        temperature_global = "";
                        lv.onRefreshComplete();
                    }

                    /*
                    1) call OpenWeather API
                    2) return json
                    3) parse json
                     */
                    public void getWeather() throws IOException, JSONException {
                        new Thread() {
                            public void run() {
                                URL url = null;
                                try {
                                    url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city_global.replace(" ", "_") + "&appid=44db6a862fba0b067b1930da0d769e98&units=imperial");
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                                URLConnection urlConnection;
                                String tmp;
                                String res = null;
                                JSONObject jresult_weather;
                                try {
                                    urlConnection = url.openConnection();
                                    InputStream is = urlConnection.getInputStream();
                                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                                    BufferedReader br = new BufferedReader(isr);
                                    StringBuilder sb = new StringBuilder();
                                    while ((tmp = br.readLine()) != null)
                                        sb.append(tmp);
                                    res = sb.toString();
                                    Log.i(TAG, city_global);
                                    Log.i(TAG, res);
                                    br.close();
                                    isr.close();
                                    is.close();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    jresult_weather = new JSONObject(res);
                                    JSONObject main = jresult_weather.getJSONObject("main");
                                    temperature_global = main.getString("temp"); // F
                                    Log.i(TAG, "temperature=" + temperature_global);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();


                    }
                }.execute();
            }
        });
    }
}
