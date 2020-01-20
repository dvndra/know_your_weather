package com.freevar.dswami.hw9;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.support.v7.widget.SearchView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.lang.*;
import org.json.JSONObject;
import org.json.JSONArray;


public class MainActivity extends AppCompatActivity {

    SearchView.SearchAutoComplete searchAutoComplete;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    @Override
    protected void onResume() {
        super.onResume();

        FavoriteAdapter mFavoriteAdapter  = new FavoriteAdapter(MainActivity.this, getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter and add fragments with relevant data.
        this.mViewPager = (ViewPager) findViewById(R.id.viewPager);
        this.mViewPager.setOffscreenPageLimit(10);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.dots);
        tabLayout.setupWithViewPager(this.mViewPager);
        this.mViewPager.setAdapter(mFavoriteAdapter);
    }

    public void dataChanged() {
        this.mViewPager.getAdapter().notifyDataSetChanged();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu,menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Get SearchView autocomplete object.
        searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.greyBack));
        searchAutoComplete.setTextColor(Color.WHITE);
        searchAutoComplete.setDropDownBackgroundResource(R.color.greyBack);
        searchAutoComplete.setThreshold(1);

        // Create a new ArrayAdapter and add data to search auto complete object.
        ArrayList<String> autoCompleteSuggestions = new ArrayList<>();
        ArrayAdapter<String> autoAdapter = new ArrayAdapter<String>(this, R.layout.auto_complete, autoCompleteSuggestions);
        searchAutoComplete.setAdapter(autoAdapter);

        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText("" + queryString);
                System.out.println("autocomplete:" + queryString);
            }
        });

        // Fetching AutoComplete Suggestions
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getSuggestions(newText);
                return true;
            }
        });
        return true;
    }

    public void getSuggestions(String newText){

        try{
            String url = "http://devendra.us-east-2.elasticbeanstalk.com/api/autocomplete/?input=" + URLEncoder.encode(newText,"UTF-8");
            JsonObjectRequest suggestions = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            loadSuggestions(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("currWeather:" + error);
                }
            });
            Volley.newRequestQueue(MainActivity.this).add(suggestions);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadSuggestions(JSONObject response){
        try{
            JSONArray suggestions = response.getJSONArray("predictions");
            ArrayList<String> autoCompleteSuggestions = new ArrayList<>();
            for (int i=0; i<suggestions.length();i++){
                String suggestion = suggestions.getJSONObject(i).getString("description");
                autoCompleteSuggestions.add(suggestion);
            }
            System.out.println(autoCompleteSuggestions.toString());
            ArrayAdapter<String> autoAdapter = new ArrayAdapter<String>(this, R.layout.auto_complete, autoCompleteSuggestions);
            searchAutoComplete.setAdapter(autoAdapter);
            searchAutoComplete.showDropDown();

        } catch (Exception e){
            e.printStackTrace();
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
}
