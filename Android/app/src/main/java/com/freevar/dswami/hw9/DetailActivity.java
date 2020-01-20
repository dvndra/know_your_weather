package com.freevar.dswami.hw9;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class DetailActivity extends AppCompatActivity {

    JSONObject weather;
    JSONObject currently;
    JSONArray dailyData;
    String city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Bundle b = getIntent().getExtras();
        String passedWeather=b.getString("weather");

        try {
            JSONObject weather = new JSONObject(passedWeather);
            String address = weather.getString("address");
            DetailActivity.this.setTitle(address);
            this.city = address.split(",")[0];
            this.weather = weather;
            this.currently = weather.getJSONObject("currently");
            this.dailyData = weather.getJSONObject("daily").getJSONArray("data");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SectionsPageAdapter mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter and add fragments with relevant data.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        Bundle bundle = new Bundle();
        bundle.putString("today", this.currently.toString());
        TodayFragment today = new TodayFragment();
        today.setArguments(bundle);
        bundle.putString("weekly", this.dailyData.toString());
        WeeklyFragment weekly = new WeeklyFragment();
        weekly.setArguments(bundle);
        PhotosFragment photos = new PhotosFragment();

        mSectionsPageAdapter.addFragment(today, "TODAY");
        mSectionsPageAdapter.addFragment(weekly, "WEEKLY");
        mSectionsPageAdapter.addFragment(photos, "PHOTOS");
        fetchPhoto(photos);
        mViewPager.setAdapter(mSectionsPageAdapter);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.tab_today);
        tabLayout.getTabAt(1).setIcon(R.drawable.tab_weekly);
        tabLayout.getTabAt(2).setIcon(R.drawable.tab_photo);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.twitter, menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_twitter) {
            String url = "https://twitter.com/intent/tweet?text=Check Out ";
            try {
                url = url + this.weather.getString("address") + "'s Weather! It is " +
                        (Math.round(this.currently.getDouble("temperature"))) + "\u2109";
                url = url + "! %23CSCI571WeatherSearch";
                Uri uri = Uri.parse(url);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception e) {
                // Chrome is probably not installed
                e.printStackTrace();
            }
        }
        else{
            onBackPressed();
        }
        return true;
    }

    public void fetchPhoto(final PhotosFragment photos){
        String url = "";
        try{
            url = "http://devendra.us-east-2.elasticbeanstalk.com/api/search/?search=" + URLEncoder.encode(this.city,"UTF-8");
        } catch(Exception e){
            e.printStackTrace();
        }
        JsonObjectRequest getPhotos = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Bundle bundle = new Bundle();
                        bundle.putString("photos", response.toString());
                        photos.setArguments(bundle);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("loadImages:" + error);
            }
        });
        Volley.newRequestQueue(DetailActivity.this).add(getPhotos);
    }
}
