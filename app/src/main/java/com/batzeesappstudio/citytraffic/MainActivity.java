package com.batzeesappstudio.citytraffic;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements
        OnMarkerClickListener,
        OnInfoWindowClickListener,
        OnMarkerDragListener,
        OnSeekBarChangeListener,
        OnMapReadyCallback,
        OnInfoWindowLongClickListener,
        OnInfoWindowCloseListener,
        LocationListener {

    private GoogleMap mMap;

    private final List<Marker> mMarkerRainbow = new ArrayList<Marker>();
    private final Random mRandom = new Random();

    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    public static final String CITYTRAFFICPREFERENCES = "CityTrafficPreference";
    private static final int REQUEST_ACCESS_LOCATION = 0;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView displayName;
    private TextView emailName;
    private ImageView userImage;

    private ImageButton trafficDetected;
    private ImageButton blockDetected;

    private DatabaseReference mDatabase;
    private String TAG = "Status";
    private double latitudeCoordinate;
    private double longitudeCoordinate;

    private LocationManager locationManager;
    private String provider;
    private GoogleApiClient client;
    private View mainMapLayout;
    Criteria criteria;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainMapLayout = findViewById(R.id.mainMapView);

        sharedpreferences = getSharedPreferences(CITYTRAFFICPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("LiveTraffic");
        mDatabase.addChildEventListener(childEventListener);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);


        displayName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textViewDisplayName);
        emailName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textViewUserEmail);
        userImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageViewUserHeader);
        trafficDetected = (ImageButton) findViewById(R.id.imageButtonTraffic);
        blockDetected = (ImageButton) findViewById(R.id.imageButtonBlock);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        checkAndRequestPermissions();

        criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mayRequestContacts();
            return;
        }

        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            Log.d("Location", "Not Available");
        }

        trafficDetected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnectingToInternet(MainActivity.this)) {
                    addTrafficData("Traffic", latitudeCoordinate, longitudeCoordinate);
                    addmarker("Traffic", latitudeCoordinate, longitudeCoordinate);
                }
                else {
                    Snackbar.make(mainMapLayout, "No internet connectivity", Snackbar.LENGTH_LONG)
                            .setAction("Settings", new View.OnClickListener() {
                                @Override
                                @TargetApi(Build.VERSION_CODES.M)
                                public void onClick(View v) {
                                    turnOnInternet();
                                }
                            }).show();
                }
            }
        });

        blockDetected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnectingToInternet(MainActivity.this)) {
                    addTrafficData("Block", latitudeCoordinate, longitudeCoordinate);
                    addmarker("Block", latitudeCoordinate, longitudeCoordinate);
                }
                else {
                    Snackbar.make(mainMapLayout, "No internet connectivity", Snackbar.LENGTH_LONG)
                            .setAction("Settings", new View.OnClickListener() {
                                @Override
                                @TargetApi(Build.VERSION_CODES.M)
                                public void onClick(View v) {
                                    turnOnInternet();
                                }
                            }).show();
                }
            }
        });

        if (user.getPhotoUrl() != null) {
            Picasso.with(MainActivity.this).load(user.getPhotoUrl()).placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher).into(userImage);
        }

        if (user.getDisplayName() == null || user.getDisplayName().equals(""))
            displayName.setText("Name not Added");
        else
            displayName.setText(user.getDisplayName());

        emailName.setText(user.getEmail());

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        if(!isConnectingToInternet(MainActivity.this)){
            Snackbar.make(mainMapLayout, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            turnOnInternet();
                        }
                    }).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 9003){
            getData();
        }
    }

    private void feedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Your Support Email Address"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "City Traffic Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, "");

        startActivity(Intent.createChooser(intent, "Send Feedback"));
    }

    private void rateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName()));
        startActivity(intent);
    }

    private void signOut() {

        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        editor.putBoolean("REMEMBERME", false);
        editor.putString("LASTPROFILEIMAGE", "");
        editor.commit();
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {

                            case R.id.nav_label:
                                goToMainActivity();
                                return true;
                            case R.id.nav_logo:
                                try {
                                    startActivity(new Intent(MainActivity.this, Intro.class));
                                } catch (Exception ex) {
                                    Toast.makeText(MainActivity.this, "Feature Coming Sonn", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            case R.id.nav_rate:
                                rateApp();
                                return true;
                            case R.id.nav_feedback:
                                feedback();
                                return true;
                            case R.id.nav_exit:
                                editor.putBoolean("REMEMBERME", false);
                                editor.commit();
                                signOut();
                                return true;
                        }
                        mDrawerLayout.openDrawer(GravityCompat.START);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnInfoWindowLongClickListener(this);

        LatLng nowTraffic = new LatLng(latitudeCoordinate, longitudeCoordinate);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nowTraffic, 15));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

        map.setContentDescription("Live Traffic Data");

        getData();

        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {

                }
            });
        }
    }

    private void getData() {
        if(mMap != null) {
            mMap.clear();
        }
        Query getLocations = mDatabase.limitToFirst(100);
        getLocations.addChildEventListener(childEventListener);
    }


    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onClearMap(View view) {
        if (!checkReady()) {
            return;
        }
        mMap.clear();
    }

    public void onResetMap(View view) {
        if (!checkReady()) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mayRequestContacts();
            return;
        }
        locationManager.requestLocationUpdates(provider, 1000, 1, this);
        mMap.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mayRequestContacts();
            return;
        }
        try{
            locationManager.requestLocationUpdates(provider, 1000, 1, this);
        }
        catch (Exception ex){
            Log.d("Permission Exception", "");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mayRequestContacts();
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!checkReady()) {
            return;
        }
        float rotation = seekBar.getProgress();
        for (Marker marker : mMarkerRainbow) {
            marker.setRotation(rotation);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Do nothing.
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Do nothing.
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    private void goToMainActivity() {
        Intent intent = new Intent(MainActivity.this, UserProfile.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Click Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        //Toast.makeText(this, "Close Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        //Toast.makeText(this, "Info Window long click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    public void addTrafficData(String type, double lat, double lon) {
        GeoLocation location = new GeoLocation();
        location.setID(user.getUid());
        Log.d("Main Activity",user.getDisplayName());
        location.setUsername(user.getDisplayName());
        location.setType(type);
        location.setLongti(lon+"");
        location.setLati(lat+"");
        location.setTime(new Date().toString());

        String key = mDatabase.child("GeoLocations").push().getKey();
        Map<String, Object> postValues = location.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, postValues);
        mDatabase.updateChildren(childUpdates);
    }

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            GeoLocation locationAll = dataSnapshot.getValue(GeoLocation.class);


            Calendar c = Calendar.getInstance();
//            Date date = null;//new Date(locationAll.getTime());
//            try {
//                Log.e("Main",locationAll.getTime());
//                date = (Date)df.parse(locationAll.getTime());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
            long millisecs = 2*3600*1000; // added to change from BST to GMT
            Date date = new Date(locationAll.getTime().replace("BST", "GMT"));
            date.setTime(date.getTime()+millisecs);
            Date oldDate = new Date(c.getTime().getTime());

            int oldSeconds = (int)(oldDate.getTime()/1000);
            int newSeconds = (int) (date.getTime()/1000);
            int timeDifference = oldSeconds - newSeconds;
            if(timeDifference >= 900){
                deleteMarkersFromServer();
            }
            else {
                addmarker(locationAll);
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, dataSnapshot.getKey() + ":" + dataSnapshot.getValue().toString());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, dataSnapshot.getKey() + ":" + dataSnapshot.getValue().toString());
            if(mMap!=null)
            mMap.clear();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(), "Could not update.", Toast.LENGTH_SHORT).show();
        }
    };

    private void addmarker(String title, double lat, double lon) {
        LatLng nowTraffic = new LatLng(lat, lon);
        String markerGuy;
        Log.d("Main Activity","Initiated marking");
        if(user.getDisplayName() == null || user.getDisplayName().equals("")){
            markerGuy = "Unknown";
        }
        else {
            markerGuy = user.getDisplayName();
        }
        int iconI;
        if(title == "Traffic"){
            iconI = R.drawable.traffic_jam;
        }
        else{
            iconI = R.drawable.road_block;
        }
        Marker newData = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(iconI))
                .position(nowTraffic)
                .title(title)
                .snippet("Marked by "+markerGuy));
    }

    private void addmarker(GeoLocation geo) {
        LatLng nowTraffic = new LatLng(Float.parseFloat(geo.getLati()), Float.parseFloat(geo.getLongti()));
        String markerGuy;
        if(geo.getDisplayName() == null || geo.getDisplayName().equals("")){
            markerGuy = "Unknown";
        }
        else {
            markerGuy = geo.getDisplayName();
        }

        if(mMap != null) {
            Marker newData = mMap.addMarker(new MarkerOptions()
                    .position(nowTraffic)
                    .title(geo.getType())
                    .snippet("Marked by " + markerGuy));
        }
    }

    private void deleteMarkersFromServer(){
        //Query applesQuery = mDatabase.limitToFirst(100);
        Query applesQuery = mDatabase.orderByKey();

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        latitudeCoordinate = location.getLatitude();
        longitudeCoordinate = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.batzeesappstudio.citytraffic/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.batzeesappstudio.citytraffic/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    class CustomInfoWindowAdapter implements InfoWindowAdapter {

        private final View mWindow;
        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {
            int badge;
                badge = 0;
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);
            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            Snackbar.make(mainMapLayout, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_LOCATION);
                        }
                    }).show();
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_LOCATION);
        }
        return false;
    }

    private  boolean checkAndRequestPermissions() {
        int permissionWriteSD = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionWriteSD != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }
        }
        else if(requestCode ==  REQUEST_ID_MULTIPLE_PERMISSIONS) {

            Map<String, Integer> perms = new HashMap<>();
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "SD Card & location services permission granted");
                    getData();
                } else {
                    Log.d(TAG, "Some permissions are not granted ask again ");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showDialogOK("Sd Card Access and Location Services Permissions are required",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                checkAndRequestPermissions();
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                break;
                                        }
                                    }
                                });
                    }
                    else {
//                        Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
//                        startInstalledAppDetailsActivity(MainActivity.this);
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    public boolean isConnectingToInternet(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    public static void startInstalledAppDetailsActivity(Context context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    private void turnOnInternet(){
        Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
        startActivityForResult(settingsIntent, 9003);
    }
}
