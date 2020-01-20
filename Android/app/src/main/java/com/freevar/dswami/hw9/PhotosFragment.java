package com.freevar.dswami.hw9;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;

public class PhotosFragment extends Fragment {
    RecyclerView rv;
    ArrayList<String> imageUrls;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment,container,false);
        imageUrls=new ArrayList<>();
        try {
            JSONObject response = new JSONObject(getArguments().getString("photos"));
            JSONArray items = response.getJSONArray("items");
            for (int i=0; i<items.length();i++){
                this.imageUrls.add(items.getJSONObject(i).getString("link"));
            }
            System.out.println(this.imageUrls.toString());
        } catch(Exception e){
            e.printStackTrace();
        }

        rv=(RecyclerView)view.findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        ImageAdapter adapter = new ImageAdapter(this.imageUrls,getActivity());
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);

        return view;
    }
}
