package com.example.anna.test;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

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
import java.util.Map;

import db.CityDB;
import fragments.DetailsFragment;
import helpers.PreferenceService;

public class MainActivity extends AppCompatActivity {
    private ImageView iv1;
    private TextView tvCity;
    private SearchView sv;
    private SQLiteDatabase dbWrite = null;
    private SQLiteDatabase dbRead = null;
    SimpleCursorAdapter adapter = null;
    private Cursor cursor = null;
    private String city_global = "";
    private String weather_global = "";
    private double temperature_global = -1000;
    private String weather_previous = "";
    private PreferenceService preferenceService;
    final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        // welcome toast, from shared preference file "user"
        preferenceService = new PreferenceService(this);
        Map<String, String> map = preferenceService.getPreferences("user");
        Toast.makeText(MainActivity.this, "Welcome, " + map.get("username") + "!", Toast.LENGTH_LONG).show();
// listen on suggestions

        sv = (SearchView) this.findViewById(R.id.svCity);
        sv.setIconifiedByDefault(false);
        sv.setSubmitButtonEnabled(true);
        sv.setQueryHint("Search city");
        sv.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionClick(int position) {
                city_global = getSuggestion(position);
                Log.i(TAG, "suggestion=" + city_global);

                handleWeather();

                return true;
            }

            private String getSuggestion(int position) {
                Cursor cursor2 = (Cursor) sv.getSuggestionsAdapter().getItem(
                        position);

                String suggest = cursor2.getString(cursor2
                        .getColumnIndex("city"));
                return suggest;

            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });

// listen on searchView
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String city) {
                tvCity.setText(city);
                if (city.length() > 2) {
                    String prefix = city.substring(0, 3);

                    cursor = dbRead.rawQuery("SELECT * FROM cities WHERE city MATCH '" + prefix + "*'", null);
                    adapter = new SimpleCursorAdapter(getApplicationContext(),
                            R.xml.mytextview, cursor, new String[]{"city"},
                            new int[]{R.id.tv_suggest});

                    sv.setSuggestionsAdapter(adapter);
                    while (cursor.moveToNext()) {
                        String res = cursor.getString(cursor.getColumnIndex("city"));
                        Log.i(TAG, res);
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String city) {

                tvCity.setText("submit:" + city);
                city_global = city;
                handleWeather();

                try {
                    dbWrite.execSQL("insert into cities values(null,'" + city + "')");

                } catch (Exception e) {
                    Log.i(TAG, "duplicate city");
                }
                return false;
            }

        });
        /*
        Activity and Fragment
         */
        findViewById(R.id.btnShowDetails).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(1000);
                v.startAnimation(alphaAnimation);
                Bundle bundle = new Bundle();
                bundle.putString("weather", weather_previous);
                bundle.putString("temperature", temperature_global + "");
                DetailsFragment blankFragment = new DetailsFragment();
                blankFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.frag, blankFragment).commit();
            }
        });

        findViewById(R.id.btnShowHistoricalData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoricalActivity.class);
                intent.putExtra("city", city_global);
                intent.putExtra("weather", weather_previous);
                intent.putExtra("temperature", temperature_global + "");
                startActivity(intent);
            }
        });
    }


    public void handleWeather() {

        try {
            getWeather();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        while (weather_global.equals(""))
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        loadImageVolley();
    }


    public void init() {
        iv1 = (ImageView) findViewById(R.id.ivCity);
        tvCity = (TextView) findViewById(R.id.tvCity);
        sv = (SearchView) findViewById(R.id.svCity);
        CityDB cityDB = new CityDB(this);
        dbWrite = cityDB.getWritableDatabase();
        dbRead = cityDB.getReadableDatabase();

        try {
            dbWrite.execSQL("drop table cities");
        } catch (Exception e) {

        }
        String create = "CREATE VIRTUAL TABLE cities USING fts3(_id integer primary key autoincrement,city TEXT UNIQUE)";
        dbWrite.execSQL(create);
        dbWrite.execSQL("insert into cities values(null,'Mountain View')");
    }

    /*
    HTTP requesting/responding and JSON parsing
     */
    // get json
    public void getWeather() throws IOException, JSONException {
        new Thread() {
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city_global.trim().replace(" ", "_") + "&appid=44db6a862fba0b067b1930da0d769e98&units=imperial");
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
                    br.close();
                    isr.close();
                    is.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    jresult_weather = new JSONObject(res);
                    JSONArray weatherArray = jresult_weather.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    weather_global = weatherObject.getString("main");
                    JSONObject main = jresult_weather.getJSONObject("main");
                    temperature_global = main.getDouble("temp"); // F

                    Log.i(TAG, "weather=" + weather_global);
                    Log.i(TAG, "temperature=" + temperature_global);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    /*
    Asynchronous image loading
     */
    /*
    Clear     http://www.forestwander.com/wp-content/original/2009_01/Sky-View.JPG
    Clouds      http://img11.deviantart.net/06c6/i/2006/217/5/0/cloudy_sky_v_by_surczak.jpg
    Rain       https://www.flickr.com/photos/laffy4k/155406169
    default     http://streetfightmagcom.b.presscdn.com/wp-content/uploads/weather_channel.png
     */
    public void loadImageVolley() {
        Log.i(TAG, "weather2=" + weather_global);
        String imageUrl = null;
        if (weather_global.equals("Clear"))
            imageUrl = "http://www.forestwander.com/wp-content/original/2009_01/Sky-View.JPG";
        else if (weather_global.equals("Clouds"))
            imageUrl = "http://img11.deviantart.net/06c6/i/2006/217/5/0/cloudy_sky_v_by_surczak.jpg";
        else if (weather_global.equals("Rain"))
            imageUrl = "https://www.flickr.com/photos/laffy4k/155406169";
        else
            imageUrl = "http://streetfightmagcom.b.presscdn.com/wp-content/uploads/weather_channel.png";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(20);
        ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {

            @Override
            public Bitmap getBitmap(String key) {
                return lruCache.get(key);
            }

            @Override
            public void putBitmap(String key, Bitmap value) {
                lruCache.put(key, value);
            }
        };
        ImageLoader imageLoader = new ImageLoader(requestQueue, imageCache);
        ImageLoader.ImageListener listener = imageLoader.getImageListener(iv1, R.drawable.wait, R.drawable.wait);
        imageLoader.get(imageUrl, listener);
        // end
        weather_previous = weather_global;
        weather_global = ""; // set empty because when loading images, thread has to wait until the value becomes not empty
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbWrite.close();
    }

}
