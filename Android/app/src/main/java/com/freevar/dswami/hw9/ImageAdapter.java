package com.freevar.dswami.hw9;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    ArrayList<String> imageUrls;
    Context context;
    ImageLoader mImageLoader;

    public static class ImageViewHolder extends RecyclerView.ViewHolder{

        public NetworkImageView mImageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.photo);
        }
    }

    public ImageAdapter(ArrayList<String> imageUrls, Context context){
        this.imageUrls = imageUrls;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo,viewGroup,false);
        ImageViewHolder ivh = new ImageViewHolder(v);
        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = this.imageUrls.get(position);
        // Instantiate the RequestQueue.
        mImageLoader = CustomVolleyRequestQueue.getInstance(this.context).getImageLoader();
        holder.mImageView.setImageUrl(url,mImageLoader);
    }

    @Override
    public int getItemCount() {
        return this.imageUrls.size();
    }




}
