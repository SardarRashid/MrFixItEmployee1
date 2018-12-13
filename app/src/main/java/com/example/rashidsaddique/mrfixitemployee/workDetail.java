package com.example.rashidsaddique.mrfixitemployee;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import com.example.rashidsaddique.mrfixitemployee.Common.Common;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Date;

public class workDetail extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextView txtDate, txtFee,txtBasic, txtTime,txtDistance,txtTotal,txtFrom,txtTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_detail);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initView
        txtBasic = (TextView)findViewById(R.id.txtBasic);
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtFee = (TextView)findViewById(R.id.txtFee);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtDistance = (TextView)findViewById(R.id.txtDistance);
        txtTotal = (TextView)findViewById(R.id.txtFinalPayment);
        txtFrom= (TextView)findViewById(R.id.txtFrom);
        txtTo = (TextView)findViewById(R.id.txtTo);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       settingInformation();

    }

    private void settingInformation() {
        if (getIntent() != null)
        {
            //set Text
            Calendar calender = Calendar.getInstance();
            String date = String.format("%s, %d/%d",convertToDayOfWeek(calender.get(Calendar.DAY_OF_WEEK)), calender.get(Calendar.DAY_OF_MONTH),calender.get(Calendar.MONTH));
            txtDate.setText(date);

            txtFee.setText(String.format("Rs %.2f",getIntent().getDoubleExtra("total,",0.0)));
            txtTotal.setText(String.format("Rs %.2f",getIntent().getDoubleExtra("total,",0.0)));
            txtBasic.setText(String.format("Rs %.2f", Common.basic_fee));
            txtTime.setText(String.format("$ min",getIntent().getStringExtra("time")));
            txtDistance.setText(String.format("$ km",getIntent().getStringExtra("distance")));
            txtFrom.setText(getIntent().getStringExtra("start_address"));
            txtTo.setText(getIntent().getStringExtra("end_address"));

            //Add Marker
            String[] location_end = getIntent().getStringExtra("location_end").split(",");
            LatLng workEndLocation = new LatLng(Double.parseDouble(location_end[0]),Double.parseDouble(location_end[1]));

            mMap.addMarker(new MarkerOptions().position(workEndLocation)
            .title("WORK POSITION")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(workEndLocation,12.0f));

        }
    }

    private String convertToDayOfWeek(int day) {
        switch (day)
        {
            case Calendar.SUNDAY:
                return "SUNDAY";
            case Calendar.MONDAY:
                return "MONDAY";
            case Calendar.TUESDAY:
                return "TUESDAY";
            case Calendar.WEDNESDAY:
                return "WEDNESDAY";
            case Calendar.THURSDAY:
                return "THURSDAY";
            case Calendar.FRIDAY:
                return "FRIDAY";
            case Calendar.SATURDAY:
                return "SATURDAY";
                default:
                    return "Unknown";
        }
    }
}
