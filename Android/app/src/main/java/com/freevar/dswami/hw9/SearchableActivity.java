package com.freevar.dswami.hw9;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.app.SearchManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.lang.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SearchableActivity extends AppCompatActivity {

    JSONObject weather;
    JSONObject currently;
    JSONArray dailyData;
    String address;
    RelativeLayout spinner;
    boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            this.address = intent.getStringExtra(SearchManager.QUERY);
//            System.out.println("Address entered: "+this.address);
            TextView textView = findViewById(R.id.cardPlace);
            textView.setText(this.address);
            SearchableActivity.this.setTitle(this.address);
            RequestQueue queue = Volley.newRequestQueue(SearchableActivity.this);
            requestLocation(queue);
        }

        CardView card_view = findViewById(R.id.card1); // creating a CardView and assigning a value.
        card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchableActivity.this, DetailActivity.class);
                Bundle b = new Bundle();
                TextView textView = findViewById(R.id.cardPlace);
                try{
                    SearchableActivity.this.weather.put("address",textView.getText());
                } catch (Exception e){
                    e.printStackTrace();
                }
                b.putString("weather",SearchableActivity.this.weather.toString());
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    /*
     * Static Method calling Google Geocode volley request to get current location
     * */
    public void requestLocation(RequestQueue queue){

        String[] results = this.address.split(",");
        String city;
        String state;
        String encodeUrl = "http://devendra.us-east-2.elasticbeanstalk.com/api/geocoding/?Street=&City=Irvine&State=";
        try{
            city = results[0];
            state = results[1];
            encodeUrl = "http://devendra.us-east-2.elasticbeanstalk.com/api/geocoding/?Street=&City="+ URLEncoder.encode(city,"UTF-8")+"&State="+URLEncoder.encode(state,"UTF-8");
        } catch (Exception e){
            e.printStackTrace();
        }

        JsonObjectRequest getLoc = new JsonObjectRequest(Request.Method.GET, encodeUrl,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                System.out.println("Location:"+response.toString());
                try{
                    String lat = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat");
                    String lon = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng");
                    String url = "http://devendra.us-east-2.elasticbeanstalk.com/api/current/?latitude="+lat+"&longitude=" + lon;
                    requestWeather(url);
                } catch(JSONException e){ System.out.println(e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Geocode Error:" + error);
            }
        });
        queue.add(getLoc);
    }


    /*
     * Method calling DarkSky volley request to get weather data
     * */
    public void requestWeather(String url){

        JsonObjectRequest getWeather = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadWeather(response);
                        updateCards();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("LocationCurrWeather:" + error);
            }
        });
        Volley.newRequestQueue(SearchableActivity.this).add(getWeather);

    }

    public void loadWeather(JSONObject response){
        this.weather= response;
        try{
            this.currently = response.getJSONObject("currently");
            this.dailyData = response.getJSONObject("daily").getJSONArray("data");
            //        System.out.println("Instance variable currently updated:" + this.currently);
//            System.out.println("Instance variable daily updated:" + this.dailyData);
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    public void updateCards() {

        // UPDATE CARD 1 and CARD 2
        ImageView summaryIcon = findViewById(R.id.summaryIcon);
        TextView cardTemp = findViewById(R.id.cardTemp);
        TextView cardSummary = findViewById(R.id.cardSummary);
        TextView humidity = findViewById(R.id.valueHumidity);
        TextView windSpeed = findViewById(R.id.valueWindSpeed);
        TextView visibility = findViewById(R.id.valueVisibility);
        TextView pressure = findViewById(R.id.valuePressure);
        try{
            int resID = getResId(this.currently.getString("icon").replace("-","_"), R.drawable.class);
            summaryIcon.setImageResource(resID);
            cardTemp.setText(String.valueOf(Math.round(this.currently.getDouble("temperature"))) + '\u2109');
            cardSummary.setText(this.currently.getString("summary"));
            humidity.setText(String.valueOf(Math.round(this.currently.getDouble("humidity")*100.0)) + "%");
            windSpeed.setText(String.format("%.2f", this.currently.getDouble("windSpeed")) + " mph");
            visibility.setText(String.format("%.2f", this.currently.getDouble("visibility")) + " mi");
            pressure.setText(String.format("%.2f", this.currently.getDouble("pressure")) + " mb");

        } catch (Exception e){
            System.out.println(e);
        }

        // BUILD CARD 3
        LinearLayout parent = findViewById(R.id.parentScroll);
        try{
            for (int i=0; i<this.dailyData.length()&&i<8;i++){
                JSONObject jsonResponse = this.dailyData.getJSONObject(i);

                // Add Row
                LinearLayout row = new LinearLayout(SearchableActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(15,0,15,0);
                row.setLayoutParams(params);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0,30, 0,30);
//                int rowID = getResId("border", R.drawable.class);
                row.setBackgroundResource(R.drawable.border);
                parent.addView(row);

                // Add four columns
                LinearLayout col = new LinearLayout(SearchableActivity.this);
                col.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                col.setOrientation(LinearLayout.HORIZONTAL);

                row.addView(col);

                TextView date = new TextView(SearchableActivity.this);
                date.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,3f));
                date.setPadding(50,0,0,0);
//                date.setGravity(Gravity.CENTER);
                date.setTextSize(18);
//                date.setTextColor(Color.parseColor("#808080"));
                String varDate = String.valueOf(Math.round(jsonResponse.getDouble("time")));
                Date dateObj = new Date(Long.parseLong(varDate) * 1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                date.setText(dateFormat.format(dateObj));


                ImageView icon = new ImageView(SearchableActivity.this);
                icon.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1f));
                int resID = getResId(jsonResponse.getString("icon").replace("-","_"), R.drawable.class);
                icon.setImageResource(resID);

                TextView tempLow = new TextView(SearchableActivity.this);
                tempLow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,2f));
                tempLow.setGravity(Gravity.CENTER);
                tempLow.setTextSize(25);
                tempLow.setText(String.valueOf(Math.round(jsonResponse.getDouble("temperatureLow"))));
                TextView tempHigh = new TextView(SearchableActivity.this);
                tempHigh.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,2f));
                tempHigh.setGravity(Gravity.CENTER);
                tempHigh.setTextSize(25);
                tempHigh.setText(String.valueOf(Math.round(jsonResponse.getDouble("temperatureHigh"))));

                col.addView(date);
                col.addView(icon);
                col.addView(tempLow);
                col.addView(tempHigh);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        // Handling Favorite Button & Click Listener
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        FloatingActionButton fab = findViewById(R.id.fab);
        this.isFavorite=false;
        if (sharedPreferences.contains("favorite_set")){
            Set<String> set = sharedPreferences.getStringSet("favorite_set", null);
            if (set!=null && set.contains(this.address)){
//                System.out.println("Read"+ set.toString());
                this.isFavorite=true;
            }
        }
        if (this.isFavorite){
            fab.setImageResource(R.drawable.favminus);
        } else{
            fab.setImageResource(R.drawable.favplus);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("Favorites", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Set<String> set = sharedPreferences.getStringSet("favorite_set", null);
                if(set==null){
                    set = new HashSet<>();
                }
//                System.out.println("Write"+ set.toString());
                isFavorite = !isFavorite;
                FloatingActionButton fab = findViewById(R.id.fab);
                if (isFavorite){
                    fab.setImageResource(R.drawable.favminus);
                    set.add(address);
                    Toast.makeText(SearchableActivity.this, address + " was added to favorites",
                            Toast.LENGTH_LONG).show();
                } else{
                    fab.setImageResource(R.drawable.favplus);
                    set.remove(address);
                    Toast.makeText(SearchableActivity.this, address + " was removed from favorites",
                            Toast.LENGTH_LONG).show();
                }
                editor.clear();
                editor.putStringSet("favorite_set",set);
                editor.commit();
            }
        });

        spinner.setVisibility(View.GONE);
    }
}
