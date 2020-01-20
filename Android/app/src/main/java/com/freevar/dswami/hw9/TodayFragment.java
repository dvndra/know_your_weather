package com.freevar.dswami.hw9;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;


public class TodayFragment extends Fragment {

    JSONObject currently;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.today_fragment,container,false);
        try {
            this.currently = new JSONObject(getArguments().getString("today"));
        } catch(Exception e){
            e.printStackTrace();
        }
        updateCards(view);
        return view;
    }

    public void updateCards(View view){
        TextView todayWindSpeed = view.findViewById(R.id.todayWindSpeed);
        TextView todayPressure = view.findViewById(R.id.todayPressure);
        TextView todayPrecipitation = view.findViewById(R.id.todayPrecipitation);
        TextView todayTemp = view.findViewById(R.id.todayTemp);
        TextView todaySummary = view.findViewById(R.id.todaySummary);
        TextView todayHumidity = view.findViewById(R.id.todayHumidity);
        TextView todayVisibility = view.findViewById(R.id.todayVisibility);
        TextView todayCloudCover = view.findViewById(R.id.todayCloudCover);
        TextView todayOzone = view.findViewById(R.id.todayOzone);
        ImageView todayIcon = view.findViewById(R.id.todayIcon);
        try{
            todayWindSpeed.setText(String.format("%.2f",this.currently.getDouble("windSpeed")) + " mph");
            todayPressure.setText(String.format("%.2f",this.currently.getDouble("pressure")) + " mb");
            todayPrecipitation.setText(String.format("%.2f",this.currently.getDouble("precipIntensity")) + " mmph");
            todayTemp.setText(String.valueOf(Math.round(this.currently.getDouble("temperature"))) + '\u2109');
            todayHumidity.setText(String.valueOf(Math.round(this.currently.getDouble("humidity")*100.0)) + "%");
            todayVisibility.setText(String.format("%.2f",this.currently.getDouble("visibility"))+ " mi");
            todayCloudCover.setText(String.valueOf(Math.round(this.currently.getDouble("cloudCover")*100.0)) + "%");
            todayOzone.setText(String.format("%.2f",this.currently.getDouble("ozone"))+ " DU");
            String icon = this.currently.getString("icon").replace("-"," ");
            icon = icon.replace("partly","");
            todaySummary.setText(icon);
            int resID = MainActivity.getResId(this.currently.getString("icon").replace("-","_"), R.drawable.class);
            todayIcon.setImageResource(resID);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}






