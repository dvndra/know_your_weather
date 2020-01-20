package com.freevar.dswami.hw9;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class FavoriteFragment extends Fragment {

    View view;
    private ArrayList<String> favList;
    String address;
    JSONObject weather;
    JSONObject currently;
    JSONArray dailyData;
    boolean isFavorite;
    RelativeLayout spinner;

    public static FavoriteFragment newInstance(int val) {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        args.putInt("position", val);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.fragment_favorite, container, false);

        //Fetch relevant data for view
        int val = getArguments().getInt("position", -1);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet("favorite_set", null);

        if (set==null){set=new HashSet<>();}
        this.favList = new ArrayList<>(set.size());
        for (String x : set){ this.favList.add(x);}
        getActivity().findViewById(R.id.dots).setVisibility(View.INVISIBLE);
        spinner = this.view.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        if (val==-1){ getCurrentView();}
        else{ getFavoriteView(val);}
        enableCardClick();
        return this.view;
    }


    public void enableCardClick(){

        CardView card_view = this.view.findViewById(R.id.card1); // creating a CardView and assigning a value.
        card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle b = new Bundle();
                TextView textView = view.findViewById(R.id.cardPlace);
                try{
                    weather.put("address",textView.getText());
                } catch (Exception e){ e.printStackTrace(); }
                b.putString("weather",weather.toString());
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    public void getCurrentView(){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        requestCurrLocation(queue);
    }
    public void getFavoriteView(int val){
        String address = this.favList.get(val);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        requestLocation(queue,address);
        this.address=address;
        showFab(address);
    }


    /*
     * Method calling IP-API volley request to get current location
     * */
    public void requestCurrLocation(RequestQueue queue){

        JsonObjectRequest getCurrLoc = new JsonObjectRequest(Request.Method.GET, "http://ip-api.com/json",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                System.out.println("currLoc:"+response.toString());
                try{
                    String city = response.getString("city");
                    String state =  response.getString("region");
                    String country = response.getString("countryCode");
                    TextView textView = view.findViewById(R.id.cardPlace);
                    textView.setText(city+", "+state+", "+country);
                    String lat = response.getString("lat");
                    String lon = response.getString("lon");
                    String url = "http://devendra.us-east-2.elasticbeanstalk.com/api/current/?latitude="+lat+"&longitude=" + lon;
                    requestWeather(url);
                } catch(JSONException e){ System.out.println(e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("IP-API:" + error);
            }
        });
        queue.add(getCurrLoc);
    }

    /*
     * Static Method calling Google Geocode volley request to get current location
     * */
    public void requestLocation(RequestQueue queue, String address){
        TextView textView = this.view.findViewById(R.id.cardPlace);
        textView.setText(address);
        String[] results = address.split(",");
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
                System.out.println("currWeather:" + error);
            }
        });
        Volley.newRequestQueue(getActivity()).add(getWeather);

    }

    public void loadWeather(JSONObject response){
        this.weather= response;
        try{
            this.currently = response.getJSONObject("currently");
            this.dailyData = response.getJSONObject("daily").getJSONArray("data");
            System.out.println("Instance variable weather updated:" + this.weather);
            System.out.println("Instance variable daily updated:" + this.dailyData);
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
        ImageView summaryIcon = this.view.findViewById(R.id.summaryIcon);
        TextView cardTemp = this.view.findViewById(R.id.cardTemp);
        TextView cardSummary = this.view.findViewById(R.id.cardSummary);
        TextView humidity = this.view.findViewById(R.id.valueHumidity);
        TextView windSpeed = this.view.findViewById(R.id.valueWindSpeed);
        TextView visibility = this.view.findViewById(R.id.valueVisibility);
        TextView pressure = this.view.findViewById(R.id.valuePressure);
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
        LinearLayout parent = this.view.findViewById(R.id.parentScroll);
        try{
            for (int i=0; i<this.dailyData.length()&&i<8;i++){
                JSONObject jsonResponse = this.dailyData.getJSONObject(i);

                // Add Row
                LinearLayout row = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(15,0,15,0);
                row.setLayoutParams(params);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0,30, 0,30);
//                int rowID = getResId("border", R.drawable.class);
                row.setBackgroundResource(R.drawable.border);
                parent.addView(row);

                // Add four columns
                LinearLayout col = new LinearLayout(getActivity());
                col.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                col.setOrientation(LinearLayout.HORIZONTAL);

                row.addView(col);

                TextView date = new TextView(getActivity());
                date.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,3f));
                date.setPadding(50,0,0,0);
//                date.setGravity(Gravity.CENTER);
                date.setTextSize(18);
//                date.setTextColor(Color.parseColor("#808080"));
                String varDate = String.valueOf(Math.round(jsonResponse.getDouble("time")));
                Date dateObj = new Date(Long.parseLong(varDate) * 1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                date.setText(dateFormat.format(dateObj));


                ImageView icon = new ImageView(getActivity());
                icon.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1f));
                int resID = getResId(jsonResponse.getString("icon").replace("-","_"), R.drawable.class);
                icon.setImageResource(resID);

                TextView tempLow = new TextView(getActivity());
                tempLow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,2f));
                tempLow.setGravity(Gravity.CENTER);
                tempLow.setTextSize(25);
                tempLow.setText(String.valueOf(Math.round(jsonResponse.getDouble("temperatureLow"))));
                TextView tempHigh = new TextView(getActivity());
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
//        SystemClock.sleep(500);
        getActivity().findViewById(R.id.dots).setVisibility(View.VISIBLE);
        spinner.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("RestrictedApi")
    public void showFab(final String address){
        // Handling Favorite Button & Click Listener
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        FloatingActionButton fab = this.view.findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        this.isFavorite=false;
        if (sharedPreferences.contains("favorite_set")){
            Set<String> set = sharedPreferences.getStringSet("favorite_set", null);
            if (set!=null && set.contains(address)){
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
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Favorites", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Set<String> set = sharedPreferences.getStringSet("favorite_set", null);
                if(set==null){
                    set = new HashSet<>();
                }
//                System.out.println("Write"+ set.toString());
                isFavorite = !isFavorite;
                FloatingActionButton fab = view.findViewById(R.id.fab);
                if (isFavorite){
                    fab.setImageResource(R.drawable.favminus);
                    set.add(address);
                    Toast.makeText(getActivity(), address + " was added to favorites",
                            Toast.LENGTH_LONG).show();
                } else{
                    fab.setImageResource(R.drawable.favplus);
                    set.remove(address);
                    Toast.makeText(getActivity(), address + " was removed from favorites",
                            Toast.LENGTH_LONG).show();
                }
                editor.clear();
                editor.putStringSet("favorite_set",set);
                editor.commit();
                ((MainActivity) getActivity()).dataChanged();
            }
        });
    }
}


