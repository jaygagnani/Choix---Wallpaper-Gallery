package com.app.monkmad.choix;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ViewFullImageActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private Context context;

    private String bucketUrl, bucketName, imageSummary, fullImageUrl;

    private ImageView fullImageView;
    private FloatingActionButton fabSetAsHomeWallpaper, fabSetAsLockWallpaper;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_full_image);

        context = this;

        Intent intent = getIntent();
        bucketUrl = intent.getStringExtra("BUCKET_URL");
        bucketName = intent.getStringExtra("BUCKET_NAME");
        imageSummary = intent.getStringExtra("IMAGE_SUMMARY");

        fullImageUrl = bucketUrl + bucketName + "/" + imageSummary;

        fullImageView = (ImageView) findViewById(R.id.full_image_view);
        Log.d(TAG, "Image summary : " + imageSummary);
        Picasso.with(this).load(fullImageUrl).into(fullImageView);

        fabSetAsHomeWallpaper = (FloatingActionButton) findViewById(R.id.fab_set_as_home_wallpaper);
        fabSetAsHomeWallpaper.setOnClickListener(this);

//        fabSetAsLockWallpaper = (FloatingActionButton) findViewById(R.id.fab_set_as_lock_wallpaper);
//        fabSetAsLockWallpaper.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        if(view.getId() == R.id.fab_set_as_home_wallpaper){
            try{
                bitmap = new GetFullImage(context).execute(fullImageUrl).get();

                wallpaperManager.setBitmap(bitmap);
                Snackbar.make(view, "Image set as home wallpaper.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
            catch (IOException e) {
                e.printStackTrace();
                Snackbar.make(view, "Cannot set image as wallpaper.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
//        else if(view.getId() == R.id.fab_set_as_lock_wallpaper){
//            try {
//                bitmap = new GetFullImage(context).execute(fullImageUrl).get();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    wallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK);
//                    Snackbar.make(view, "Image set as lock screen wallpaper.", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//                else{
//                    Snackbar.make(view, "Your device does not supports this feature.", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            }
//            catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private class GetFullImage extends AsyncTask<String, Void, Bitmap> {

        Context context;
        GetFullImage(Context context){
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                Bitmap tempBitmap = Picasso.with(context).load(strings[0]).get();
                return tempBitmap;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
