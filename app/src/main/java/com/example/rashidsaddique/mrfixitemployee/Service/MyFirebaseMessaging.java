package com.example.rashidsaddique.mrfixitemployee.Service;

import android.content.Intent;
import android.util.Log;

import com.example.rashidsaddique.mrfixitemployee.CustommerCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //Customer app send Latlng to  the employee app
        if(remoteMessage.getData() != null) {
            //Log.d("FixIt", remoteMessage.getNotification().getBody());
            Map<String,String> data = remoteMessage.getData();
            String customer = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");
//            LatLng customer_location = new Gson().fromJson(message, LatLng.class);

            Intent intent = new Intent(getBaseContext(), CustommerCall.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("customer", customer);


            startActivity(intent);
        }

    }
}
