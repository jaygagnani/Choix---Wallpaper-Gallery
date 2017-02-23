package com.app.monkmad.choix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor preferenceEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAnalytics.getInstance(this);

        sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        preferenceEditor = null;

        setContentView(R.layout.activity_splash);

//        if(MainActivity.isOnline()) {
            new GetCategory(this).execute();
//        }
//        else{
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//            alertDialogBuilder.setCancelable(false);
//            alertDialogBuilder.setMessage("Please check your internet connection and try again.");
//            alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    if (MainActivity.checkInternetConnected(getApplicationContext())) {
//                        if(MainActivity.isOnline()){
//                            new GetCategory(getApplicationContext()).execute();
//                        }
//                    }
//
//                }
//            });
//            alertDialogBuilder.show();
//            Toast.makeText(this, "No internet available", Toast.LENGTH_SHORT).show();
//        }

    }

    private class GetCategory extends AsyncTask<Void, Void, String[]> {

        Context context;

        URL url;
        InputStream inputStream;
        HttpURLConnection urlConnection;
        String result;

        String[] categoryList;

        private GetCategory(Context context){
            this.context = context;
            result = null;
            inputStream = null;
        }

        @Override
        protected String[] doInBackground(Void... voids) {

            if(MainActivity.isOnline()) {
                try {
                    url = new URL("http://52.38.86.215/api/choix");
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        Toast.makeText(context, "Cannot connect to the network", Toast.LENGTH_SHORT).show();
                        throw new IOException("HTTP error code " + responseCode);
                    }
                    inputStream = urlConnection.getInputStream();
                    if (inputStream != null) {
                        result = readStream(inputStream);
                    }

                    return convertString(result);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else{
                result = null;
            }
            return null;
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return response.toString();
        }

        private String[] convertString(String result) throws JSONException {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("response");
            JSONArray jsonArray1 = jsonArray.getJSONArray(0);

            int arrayLength = jsonArray1.length();
            String[] actualCategoryNames = new String[arrayLength];

            for(int i=0; i<arrayLength; i++){
                actualCategoryNames[i] = jsonArray1.getString(i);
            }

            return actualCategoryNames;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if(strings != null){
                proceedToNextActivity(strings);
            }
            else{
                ifNoInternetDialog(context);
            }
        }
    }

    private void proceedToNextActivity(String[] categoryNames){
        String[] editedCategoryNames = new String[categoryNames.length];

        Intent intent = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        String temp;

        for(int i=0; i<categoryNames.length; i++){
            temp = categoryNames[i].toLowerCase();
            temp = temp.replaceAll("[^a-zA-Z]", "");
            editedCategoryNames[i] = temp;
        }

        extras.putStringArray("category_names", categoryNames);
        extras.putStringArray("edited_category_names", editedCategoryNames);

        intent.putExtras(extras);
        startActivity(intent);

        finish();
    }

    private void ifNoInternetDialog(final Context context){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Please check your internet connection and try again.");
        alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (MainActivity.checkInternetConnected(context)) {
                    if(MainActivity.isOnline()){
                        new GetCategory(getApplicationContext()).execute();
                    }
                    else{
                        ifNoInternetDialog(context);
                    }
                }
                else{
                    ifNoInternetDialog(context);
                }

            }
        });
        alertDialogBuilder.show();
        Toast.makeText(context, "No internet available", Toast.LENGTH_SHORT).show();
    }

}
