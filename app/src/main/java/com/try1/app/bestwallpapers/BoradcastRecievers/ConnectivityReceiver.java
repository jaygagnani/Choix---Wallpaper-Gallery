package com.try1.app.bestwallpapers.BoradcastRecievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.try1.app.bestwallpapers.MainActivity;

/**
 * Created by jay on 10/02/2017.
 */

public class ConnectivityReceiver extends BroadcastReceiver {

    MainActivity mainActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isOnline = MainActivity.isOnline();

        if(isOnline){
            mainActivity = new MainActivity();
            mainActivity.createUI();
        }
        else{
            // if no internet connection
            Toast.makeText(context, "No internet available", Toast.LENGTH_SHORT).show();
        }
    }

}
