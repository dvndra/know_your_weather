package com.freevar.dswami.hw9;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class WeeklyFragment extends Fragment {

    JSONArray weekly;
    private LineChart mLineChart;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weekly_fragment,container,false);
        try {
            this.weekly = new JSONArray(getArguments().getString("weekly"));
        } catch(Exception e){
            e.printStackTrace();
        }
        this.mLineChart = view.findViewById(R.id.chart);
        makeChart();
        ImageView weeklyIcon = view.findViewById(R.id.weeklyIcon);
        TextView weeklySummary = view.findViewById(R.id.weeklySummary);
        try{
            int resID = MainActivity.getResId(this.weekly.getJSONObject(0).getString("icon").replace("-","_"), R.drawable.class);
            weeklyIcon.setImageResource(resID);
            weeklySummary.setText(this.weekly.getJSONObject(0).getString("summary"));
        } catch (Exception e){
            e.printStackTrace();
        }

        return view;
    }

    public void makeChart(){
        ArrayList<Entry> minTemp = new ArrayList<>();
        ArrayList<Entry> maxTemp = new ArrayList<>();
        long maxValue=-1000;
        long minValue= 1000;
        try {
            for (int i = 0; i < this.weekly.length() && i < 8; i++) {
                JSONObject jsonResponse = this.weekly.getJSONObject(i);
                long minVal=Math.round(jsonResponse.getDouble("temperatureLow"));
                long maxVal = Math.round(jsonResponse.getDouble("temperatureHigh"));
                minTemp.add(new Entry(i, minVal));
                maxTemp.add(new Entry(i, maxVal));
                if (maxVal>maxValue){maxValue=maxVal;}
                if (minVal<minValue){minValue=minVal;}
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        maxValue = ((maxValue/10) + 1)*10;
        minValue = (minValue/10)*10;
        LineDataSet minTempDataset = new LineDataSet(minTemp, "Minimum Temperature   ");
        minTempDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        minTempDataset.setColor(Color.parseColor("#C67FFF"));
        LineDataSet maxTempDataset = new LineDataSet(maxTemp, "Maximum Temperature");
        maxTempDataset.setAxisDependency(YAxis.AxisDependency.RIGHT);
        maxTempDataset.setColor(Color.parseColor("#FAA616"));

        // use the interface ILineDataSet
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(minTempDataset);
        dataSets.add(maxTempDataset);
        LineData data = new LineData(dataSets);

        Legend legend = this.mLineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(14f);
        legend.setTextColor(Color.parseColor("#FFFFFF"));
        legend.setXEntrySpace(25f);
        legend.setMaxSizePercent(1.00f);
        legend.setWordWrapEnabled(true);

        XAxis xAxis= this.mLineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.parseColor("#808080"));
        xAxis.setSpaceMin(0);

        YAxis left= this.mLineChart.getAxisLeft();
        left.setDrawGridLines(false);
        left.setTextSize(10f);
        left.setTextColor(Color.parseColor("#808080"));
        left.setAxisMaximum(maxValue);
        left.setAxisMinimum(minValue);
        left.setGranularity(10f);
        left.setGranularityEnabled(true);

        YAxis right= this.mLineChart.getAxisRight();
        right.setDrawGridLines(false);
        right.setTextSize(10f);
        right.setTextColor(Color.parseColor("#808080"));
        right.setAxisMaximum(maxValue);
        right.setAxisMinimum(minValue);
        right.setGranularity(10f);
        right.setGranularityEnabled(true);

        this.mLineChart.setData(data);
        this.mLineChart.invalidate(); // refresh
    }
}
