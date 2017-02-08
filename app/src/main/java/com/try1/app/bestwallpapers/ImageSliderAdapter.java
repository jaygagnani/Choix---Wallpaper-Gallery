package com.try1.app.bestwallpapers;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by jay on 14/01/2017.
 */

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {

    private ArrayList<String> imageSummaryList;
    private Context context;

    private String bucketUrl, bucketName;

    public ImageSliderAdapter(Context context, ArrayList<String> imageSummaryList, String bucketUrl, String bucketName) {
        this.context = context;
        this.imageSummaryList = imageSummaryList;
        this.bucketUrl = bucketUrl;
        this.bucketName = bucketName;
    }

    @Override
    public ImageSliderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_slider_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ImageSliderAdapter.ViewHolder holder, int position) {
//        Glide.with(context)
//                .load(Uri.parse(bucketUrl + bucketName + "/" + imageSummaryList.get(position+1).toString()))
//                .into(holder.wallpaperImage);

        Picasso.with(context)
                .load(bucketUrl + bucketName + "/" + imageSummaryList.get(position+1).toString())
                .fit()
                .into(holder.wallpaperImage);

    }

    @Override
    public int getItemCount() {
        Toast.makeText(context, imageSummaryList.size()+"", Toast.LENGTH_SHORT).show();
        return imageSummaryList.size()-1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView wallpaperImage;

        public ViewHolder(View itemView) {
            super(itemView);

            wallpaperImage = (ImageView) itemView.findViewById(R.id.recycler_view_slider_item);
            wallpaperImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                Intent intent = new Intent(context, ViewFullImageActivity.class);
                intent.putExtra("BUCKET_URL", bucketUrl);
                intent.putExtra("BUCKET_NAME", bucketName);
                intent.putExtra("IMAGE_SUMMARY", imageSummaryList.get(this.getLayoutPosition()+1));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
}
