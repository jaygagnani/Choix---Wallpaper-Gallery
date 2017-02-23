package com.app.monkmad.choix;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    private final String TAG = getClass().getSimpleName();

    Bundle getExtras;

    private RecyclerView imageSliderRecyclerView;

    private StaggeredGridLayoutManager gridLayoutManager;
    private ImageSliderAdapter sliderAdapter;
    private ProgressBar progressBar;

    private AHBottomNavigation ahBottomNavigation;

    private ArrayList<String> imageSummaryList;
    private String[] categoryList;
    private String[] actualCategoryNames;

    private final String BUCKET_URL = "https://s3-ap-south-1.amazonaws.com/";
    private final String BUCKET_NAME = "wallpapergallery";
    private String category = null;
    private String res = "xhdpi";

    private boolean isOnline;

    DisplayMetrics displayMetrics;
    float height, width;

    AmazonS3Client s3;
    AWSCredentials awsCredentials;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        if(height>=1920 && width>=1080){
            res = "xxhdpi";
        }
        else if(height>=1280 && width>=920){
            res = "xhdpi";
        }
        else if(height>=800 && width>=480){
            res = "hdpi";
        }
        else if(height>=480 && width>=320){
            res = "mdpi";
        }
        else if(height>=320 && width>=240){
            res = "ldpi";
        }
        else{
            res = "mdpi";
        }

        getExtras = getIntent().getExtras();

        categoryList = getExtras.getStringArray("edited_category_names");
        actualCategoryNames = getExtras.getStringArray("category_names");

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.bringToFront();
        progressBar.setIndeterminate(true);

        imageSliderRecyclerView = (RecyclerView) findViewById(R.id.image_slider_recycler_view);

        gridLayoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);

        imageSliderRecyclerView.setLayoutManager(gridLayoutManager);
        imageSliderRecyclerView.setHasFixedSize(true);

        intent = getIntent();

        isOnline = false;
        if (checkInternetConnected(this)) {
            isOnline = isOnline();
        }

        createUI();
    }

    public void createUI(){
        if(isOnline) {

            Log.d(TAG, "FCM token : " + FirebaseInstanceId.getInstance().getToken());
            if(SplashActivity.sharedPreferences.contains(getString(R.string.preference_file_key))){
                category = SplashActivity.sharedPreferences.getString(getString(R.string.preference_file_key), categoryList[0]);
            }
            else{
                category = categoryList[0];
            }

            // Bottom Navigation
            ahBottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation_tabs);
            // Create items
            AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.menu_category, android.R.drawable.ic_menu_sort_by_size, R.color.colorAccent);
            AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.menu_rate_us, android.R.drawable.star_off, R.color.colorAccent);
            AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.menu_share_app, android.R.drawable.ic_menu_share, R.color.colorAccent);
            AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.menu_suggest, android.R.drawable.ic_menu_edit, R.color.colorAccent);

            ahBottomNavigation.addItem(item1);
            ahBottomNavigation.addItem(item2);
            ahBottomNavigation.addItem(item3);
            ahBottomNavigation.addItem(item4);

            ahBottomNavigation.setAccentColor(getResources().getColor(R.color.colorAccent));
            ahBottomNavigation.setOnTabSelectedListener(this);
            clearTabSelection();

            imageSummaryList = new ArrayList<>();

            imageSummaryList = getImagesFromS3Bucket(category, res);

            sliderAdapter = new ImageSliderAdapter(this, imageSummaryList, BUCKET_URL, BUCKET_NAME);
//            sliderAdapter.notifyDataSetChanged();
            imageSliderRecyclerView.setAdapter(sliderAdapter);

            getAds();
        }
        else{
            // if no internet connection
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setMessage("Please check your internet connection and try again.");
            alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (checkInternetConnected(getApplicationContext())) {
                        isOnline = isOnline();
                    }
                    createUI();
                }
            });
            alertDialogBuilder.show();
            Toast.makeText(this, "No internet available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        if(wasSelected) {
            return false;
        }
        if(!isOnline()){
            Toast.makeText(this, "No internet available", Toast.LENGTH_SHORT).show();
            return false;
        }

        switch (position){
            case 0: // Category tab
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Categories")
                        .setItems(actualCategoryNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                category = categoryList[i];
                                imageSummaryList = getImagesFromS3Bucket(category, res);
                                sliderAdapter.updateDataList(imageSummaryList);

                                clearTabSelection();
                            }
                        });

                AlertDialog categoryDialog= builder.create();
                categoryDialog.setCancelable(true);
                categoryDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        clearTabSelection();
                    }
                });
                categoryDialog.show();

                break;

            case 1: // Rate us tab
                rateApp();
                break;

            case 2: // Share tab
                shareApp();
                break;
            case 3: // Suggest tab
                sendSuggestion();
                break;

            default:
                break;
        }

        return true;
    }

    public ArrayList AwsCognitoCredentialsProvider(Context context, String dir, String res) throws ExecutionException, InterruptedException {
        awsCredentials = new AnonymousAWSCredentials();

        return new CreateAWSS3Client(BUCKET_NAME, dir, res).execute(awsCredentials).get();
    }

    class CreateAWSS3Client extends AsyncTask<AWSCredentials, Void, ArrayList> {

        ArrayList<String> imageSummaryList;
        String bucketName, directory, resolution;

        public CreateAWSS3Client(String bucketName, String dir, String res){
            this.bucketName = bucketName;
            this.directory = dir;
            this.resolution = res;

            imageSummaryList = new ArrayList();
            imageSummaryList.clear();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList doInBackground(AWSCredentials... awsCredentials) {
            s3 = new AmazonS3Client(awsCredentials[0]);
            ObjectListing objectListing = s3.listObjects(bucketName, directory+"/"+resolution);

            for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()){
                imageSummaryList.add(objectSummary.getKey());
            }

            return imageSummaryList;
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {
            super.onPostExecute(arrayList);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void getAds(){
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.firebase_admob_app_id));
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("DC712D7CC1728E3D57DD338A75F0C46E")
                .build();

        AdView bottom_banner = (AdView) findViewById(R.id.home_bottom_adView);
        bottom_banner.loadAd(adRequest);
    }

    public void rateApp(){
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        }
        catch (ActivityNotFoundException e) {
            uri = Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName()+"");
            myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(myAppLinkToMarket);
        }
    }

    public void shareApp(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Hey, I just found this app with COOL wallpapers. You should try it out too! https://play.google.com/store/apps/details?id="+getPackageName();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Choix - A wallpaper of choice");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void sendSuggestion() {
        Intent suggestionIntent = new Intent(Intent.ACTION_SENDTO);
        suggestionIntent.setData(Uri.parse("mailto:" + Uri.encode("monkmad2015@gmail.com") + "?subject=" + Uri.encode("Choix: Request for wallpaper design")));

        try{
            startActivity(suggestionIntent);
//            finish();
        }
        catch (ActivityNotFoundException e){
            Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean checkInternetConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static boolean isOnline(){
        Runtime runtime = Runtime.getRuntime();
        try{
            Process ipProcess = runtime.exec("system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();

            return (exitValue == 0);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ArrayList getImagesFromS3Bucket(String category, String res){
        try {
            ArrayList imageSummaryList = AwsCognitoCredentialsProvider(this, category, res);

            return imageSummaryList;
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void clearTabSelection(){
        if(isOnline){
            ahBottomNavigation.setCurrentItem(-1);
            ahBottomNavigation.clearFocus();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        clearTabSelection();
    }

    @Override
    protected void onResume() {
        super.onResume();

        clearTabSelection();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        clearTabSelection();
    }

}
