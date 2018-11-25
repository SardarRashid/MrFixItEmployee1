package com.example.rashidsaddique.mrfixitemployee;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import com.google.android.gms.location.LocationListener;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rashidsaddique.mrfixitemployee.Common.Common;
import com.example.rashidsaddique.mrfixitemployee.Model.Token;
import com.example.rashidsaddique.mrfixitemployee.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    private GoogleMap mMap;

    //Presence System
    DatabaseReference onlineRef,currentUserRef;


    //Play Services

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int  UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geoFire;

    Marker mCurrent;

    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    //Robot Animation

    private List<LatLng> PolyLineList;
    private Marker robotMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition, endPosition,currentPosition;
    private int index,next;
    //private Button btnGo;
    private PlaceAutocompleteFragment places;
    AutocompleteFilter typeFilter;
    private String destination;
    private PolylineOptions polylineOptions,blackPolyLineOptions;
    private Polyline blackPolyLine,greyPolyLine;

    private IGoogleAPI mServices;




    //FireBase Storage
    FirebaseStorage firebaseStorage;
    StorageReference  storageReference;



    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index<PolyLineList.size()-1)
            {
                index++;
                next = index+1;
            }
            if (index < PolyLineList.size()-1);
            {
                startPosition = PolyLineList.get(index);
                endPosition = PolyLineList.get(next);
            }
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat = v*endPosition.latitude+(1-v)*startPosition.latitude;
                    LatLng newPos = new LatLng(lat,lng);
                    robotMarker.setPosition(newPos);
                    robotMarker.setAnchor(0.0f,0.0f);
                    robotMarker.setRotation(getBearing(startPosition,newPos));


                    //Code for blinking Car Problem...


                    float rotation = getBearing (startPosition, newPos);
                    if (robotMarker.getRotation()>rotation)
                        robotMarker.setRotation(robotMarker.getRotation()
                                - valueAnimator.getAnimatedFraction());
                    else
                        robotMarker.setRotation(robotMarker.getRotation()
                                +valueAnimator.getAnimatedFraction());

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this,3000);
        }
    };


    private float getBearing(LatLng startPosition, LatLng endPosition) {

        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);


        if (startPosition.latitude< endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        else if (startPosition.latitude< endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        return -1;


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init firebase storage

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView txtName = (TextView)navigationHeaderView.findViewById(R.id.txtEmployeeName);
        CircleImageView imageAvatar = (CircleImageView) navigationHeaderView.findViewById(R.id.image_avatar);

        //Access from common.currentUser in Main Activity after login we have set this data
        txtName.setText(Common.currentUser.getName());

        if(Common.currentUser.getAvtarUrl()!= null
                && !TextUtils.isEmpty(Common.currentUser.getAvtarUrl())) {
            Picasso.with(this).load(Common.currentUser.getAvtarUrl()).into(imageAvatar);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Presence System
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.employees_location_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserRef.onDisconnect().removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Init View
        location_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener( new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {

                    FirebaseDatabase.getInstance().goOnline();

                    startLocationUpdates();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_SHORT).show();
                } else {

                    FirebaseDatabase.getInstance().goOffline();

                    stopLocationUpdates();
                    mCurrent.remove();
                    mMap.clear();
                    if(handler != null)
                    handler.removeCallbacks(drawPathRunnable);
                    Snackbar.make(mapFragment.getView(), "You are Off Line", Snackbar.LENGTH_SHORT).show();
                }
            }

        });


        PolyLineList = new ArrayList<>();


        //Places Api
        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();


        places = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (location_switch.isChecked())
                {
                    destination = place.getAddress().toString();
                    destination = destination.replace("","+");

                    getDirection();
                }
                else {
                    Toast.makeText(EmployeeHome.this, "Please change your status to ONLINE", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(Status status) {
                Toast.makeText(EmployeeHome.this, ""+status.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        //Geo Fire

        ref = FirebaseDatabase.getInstance().getReference(Common.employees_location_tbl);
        geoFire = new GeoFire(ref);

        setUpLocation();
        mServices = Common.getGoogleAPI();
        updateFirebaseToken();
    }


    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token  = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);

    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("Mr Fix It",requestApi); //print URL for Debug
            mServices.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                JSONObject jasonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jasonObject.getJSONArray("routes");
                                for (int i=0; i<jsonArray.length();i++)
                                {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    PolyLineList = decodePoly(polyline);
                                }
                                //Adjusting bounds
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for(LatLng latLng:PolyLineList)
                                    builder.include(latLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(PolyLineList);
                                greyPolyLine = mMap.addPolyline(polylineOptions);


                                blackPolyLineOptions = new PolylineOptions();
                                blackPolyLineOptions.color(Color.BLACK);
                                blackPolyLineOptions.width(5);
                                blackPolyLineOptions.startCap(new SquareCap());
                                blackPolyLineOptions.endCap(new SquareCap());
                                blackPolyLineOptions.jointType(JointType.ROUND);
                                blackPolyLine = mMap.addPolyline(blackPolyLineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(PolyLineList
                                                .get(PolyLineList.size()-1))
                                        .title("Work Locatiion"));

                                //Animation
                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator ValueAnimator) {
                                        List<LatLng> points = greyPolyLine.getPoints();
                                        int percentValue = (int)ValueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int)(size * (percentValue/100.0f));
                                        List<LatLng> p = points.subList(0, newPoints);
                                        blackPolyLine.setPoints(p);
                                    }
                                });
                                polyLineAnimator.start();

                                robotMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                                handler = new Handler();
                                index = 1;
                                next = 1;
                                handler.postDelayed(drawPathRunnable,3000);



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(EmployeeHome.this,""+t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        if(location_switch.isChecked())
                            displayLocation();
                    }

                }
        }
    }

    private void setUpLocation() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED )
        {
            //Request runtime permission

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else {
            if (checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                if(location_switch.isChecked())
                    displayLocation();
            }
        }

    }
    //LocationRequest request = LocationRequest.create();

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST)
                        .show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void stopLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);

    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(Common.mLastLocation != null)
        {
            if(location_switch.isChecked())
            {
                final double latitude = Common.mLastLocation.getLatitude();
                final double longitude = Common.mLastLocation.getLongitude();


                //Create LatLng from mLastLocation and this is center point
                LatLng center = new LatLng(latitude,longitude);
                //distance in metters
                //heading 0 is north side, 90 is east , 180 is south and 270 is west
                LatLng northSide = SphericalUtil.computeOffset(center,100000,0);
                LatLng southSide = SphericalUtil.computeOffset(center,100000,180);

                LatLngBounds bounds = LatLngBounds.builder()
                        .include(northSide)
                        .include(southSide)
                        .build();

                places.setBoundsBias(bounds);
                places.setFilter(typeFilter);

                //Update to FireBase

                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                        //Add marker
                        if(mCurrent != null)
                            mCurrent.remove(); //Remove Already Marker
                        mCurrent = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                .title("You"));

                        //Move Camera to This position

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                        //Draw animation rotate marker
                        rotateMarker(mCurrent,-360,mMap);
                    }
                });
            }
        }
        else
        {
            Log.d("ERROR","Cannot get your location");
        }

    }
    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mCurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator =  new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float)elapsed/duration);
                float rot = t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot > 180?rot/2:rot);

                if(t<1.0)
                {
                    handler.postDelayed(this,16);
                }

            }
        });
    }


    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.employee_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_work_history) {
            // Handle the camera action
        } else if (id == R.id.nav_bill) {

        } else if (id == R.id.nav_help) {

        }else if (id == R.id.nav_setting) {

        }
        else if (id == R.id.nav_change_pwd) {
            showDialogChangePwd();

        }
        else if (id == R.id.nav_sign_out) {
            signOut();

        }
        else if (id == R.id.nav_update_info) {
            showDialogUpdateInfo();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EmployeeHome.this);
        alertDialog.setTitle("UPDATE INFORMATION");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_update_information,null);

        final MaterialEditText edtName = (MaterialEditText)layout_pwd.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText)layout_pwd.findViewById(R.id.edtPhone);
        final ImageView image_upload = (ImageView) layout_pwd.findViewById(R.id.image_upload);
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        alertDialog.setView(layout_pwd);

        //set Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(EmployeeHome.this);
                waitingDialog.show();

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();

                Map<String,Object> updateInfo = new HashMap<>();
                if(!TextUtils.isEmpty(name))
                    updateInfo.put("name", name);
                if(!TextUtils.isEmpty(phone))
                    updateInfo.put("phone", phone);

                DatabaseReference employeeInformation = FirebaseDatabase.getInstance().getReference(Common.employees_tbl);
                employeeInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Toast.makeText(EmployeeHome.this, "Information Update ", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(EmployeeHome.this, "Information Update Failed ", Toast.LENGTH_SHORT).show();
                            waitingDialog.dismiss();
                            }
                        });


            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

            }
        });
        alertDialog.show();

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select picture: "),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                &&data != null && data.getData() != null)
        {
            Uri saveUri = data.getData();
            if(saveUri != null)
            {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Uploading...");
                mDialog.show();

                String imageName = UUID.randomUUID().toString(); //Random name Image upload
                final StorageReference imageFolder = storageReference.child("images/"+imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mDialog.dismiss();
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //update image property on user model
                                        Map<String,Object>avtarUpdate = new HashMap<>();
                                        avtarUpdate.put("avtarUrl",uri.toString());

                                        DatabaseReference employeeInformation = FirebaseDatabase.getInstance().getReference(Common.employees_tbl);
                                        employeeInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(avtarUpdate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                            Toast.makeText(EmployeeHome.this, "Uploaded ", Toast.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(EmployeeHome.this, "Upload Error ", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });

                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0* taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        mDialog.setMessage("Uploaded"+progress+"%");
                    }
                });
            }
        }

    }

    private void showDialogChangePwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EmployeeHome.this);
        alertDialog.setTitle("Change Password");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_pwd,null);

        final MaterialEditText edtpassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewpassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatpassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        //set button
        alertDialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               final android.app.AlertDialog waitingDialog = new SpotsDialog(EmployeeHome.this);
               waitingDialog.show();

               if (edtNewpassword.getText().toString().equals(edtRepeatpassword.getText().toString()))
                {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    //get auth creditinals from the user  for re_authentication
                    //only with email

                    AuthCredential credential = EmailAuthProvider.getCredential(email,edtpassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatpassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            //Update Employee Information password update
                                                            Map<String,Object>password = new HashMap<>();
                                                                    password.put("password",edtRepeatpassword.getText().toString());

                                                            DatabaseReference employeeInformation = FirebaseDatabase.getInstance().getReference(Common.employees_tbl);
                                                            employeeInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .updateChildren(password)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                                Toast.makeText(EmployeeHome.this, "password changed ..", Toast.LENGTH_SHORT).show();
                                                                            else
                                                                                Toast.makeText(EmployeeHome.this, "password was changed but not update to employee information", Toast.LENGTH_SHORT).show();
                                                                                waitingDialog.dismiss();
                                                                        }
                                                                    });


                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(EmployeeHome.this, "Password doesn't chnage", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });

                                    }
                                    else
                                    {
                                        waitingDialog.dismiss();
                                        Toast.makeText(EmployeeHome.this, "Wrong old Password", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                }
                else
                {
                    waitingDialog.dismiss();
                    Toast.makeText(EmployeeHome.this, "Password doesn't match", Toast.LENGTH_SHORT).show();

                }

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();

    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(EmployeeHome.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();

    }

//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try
        {
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.style_map)
            );
            if(!isSuccess)
                Log.e("ERROR","Map style load failed..");
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }
}
