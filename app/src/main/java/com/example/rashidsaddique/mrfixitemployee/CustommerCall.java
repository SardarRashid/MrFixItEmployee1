package com.example.rashidsaddique.mrfixitemployee;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rashidsaddique.mrfixitemployee.Common.Common;
import com.example.rashidsaddique.mrfixitemployee.Model.DataMessage;
import com.example.rashidsaddique.mrfixitemployee.Model.FCMResponse;
import com.example.rashidsaddique.mrfixitemployee.Model.Token;
import com.example.rashidsaddique.mrfixitemployee.Remote.IFCMService;
import com.example.rashidsaddique.mrfixitemployee.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustommerCall extends AppCompatActivity {

    TextView txtTime,txtAddress,txtDistance,txtCountDown;
    Button btnAccept, btnCancel;

    MediaPlayer mediaPlayer;
    IGoogleAPI mServices;
    IFCMService mFCMService;

    String customerId;

    String lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custommer_call);

        mServices = Common.getGoogleAPI();
        mFCMService = Common.getFCMServices();

        //init View

        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtCountDown = (TextView) findViewById(R.id.txt_count_down);

        btnAccept = (Button)findViewById(R.id.btnAccept);
        btnCancel = (Button) findViewById(R.id.btnDecline);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);

            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustommerCall.this,EmployeeTracking.class);
                //send customer location to new activity employeeTrcking
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customerId",customerId);

                startActivity(intent);
                finish();

            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if(getIntent()!= null)
        {
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customer");

            getDirection(lat,lng);

        }
        startTimer();

    }

    private void startTimer() {
   CountDownTimer countDownTimer = new CountDownTimer(30000,1000) {
       @Override
       public void onTick(long millisUntilFinished) {
           txtCountDown.setText(String.valueOf(1/1000));


       }

       @Override
       public void onFinish() {
           if(!TextUtils.isEmpty(customerId))
               cancelBooking(customerId);
           else Toast.makeText(CustommerCall.this, "Customer Id must be not null", Toast.LENGTH_SHORT).show();

       }
   }.start();
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

//        Notification notification = new Notification("Cancel","Employee has Cancel Your request");
//        Sender sender = new Sender(token.getToken(),notification);
        Map<String,String>content = new HashMap<>();
        content.put("title","Cancel");
        content.put("message","Employee has Cancel Your request");
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body().success == 1)
                        {
                            Toast.makeText(CustommerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {


                    }
                });

    }

    private void getDirection(String lat,String lng) {

        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("Mr Fix It",requestApi); //print URL for Debug
            mServices.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                JSONObject jasonObject = new JSONObject(response.body().toString());

                                JSONArray routes = jasonObject.getJSONArray("routes");

                                //After First Element get array with name "legs
                                JSONObject object = routes.getJSONObject(0);

                                //After First Element get array with name "legs"
                                JSONArray legs = object.getJSONArray("legs");

                                //get first element of legs Array
                                JSONObject legsObject = legs.getJSONObject(0);

                                //Now , get distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));

                                //get Time
                                JSONObject time = legsObject.getJSONObject("duration");
                                txtTime.setText(time.getString("text"));

                                //getAddress
                                String address = legsObject.getString("end_address");
                                txtAddress.setText(address);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustommerCall.this,""+t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying())
        mediaPlayer.start();
    }
}
