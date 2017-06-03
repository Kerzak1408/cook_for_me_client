package com.example.kerzak.cook4me.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kerzak.cook4me.Listeners.CookButtonListener;
import com.example.kerzak.cook4me.WebSockets.CookingData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.kerzak.cook4me.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    /**
     * The echo server on websocket.org.
     */
    private static final String SERVER = "ws://echo.websocket.org";

    /**
     * The timeout value in milliseconds for socket connection.
     */
    private static final int TIMEOUT = 5000;

    private GoogleMap mMap;

    public Marker whereAmI;
    boolean initialized = false;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation = null;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;

    private List<CookingData> cookingDataList;

    com.example.kerzak.cook4me.WebSockets.Client client;

    LinearLayout cookingViewLayout;
    TextView progressTextView;
    ProgressBar progressBar;
    Button confirmLocationButton;
    private ImageView markerImage;
    private TextView loggerView;
    // For switching between cook and eat modes.
    private ImageButton cookButton;

    String json = null;
    String login = null;
    Gson gson = new Gson();

    private boolean cookMode = false;
    public  boolean isInCookMode() {
        return cookMode;
    }

    public void switchCookMode() {
        cookMode = !cookMode;
        cookingViewLayout = (LinearLayout) findViewById(R.id.cookingButtonsLayout);
        progressTextView = (TextView) findViewById(R.id.progressText);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        if (cookMode) {
            cookButton.setImageResource(R.drawable.eat);
        } else {
            cookButton.setImageResource(R.drawable.cook_hat);
        }
        changeCookingButtonsVisibility(cookMode);
    }

    public void changeCookingButtonsVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;

        cookingViewLayout = (LinearLayout) findViewById(R.id.cookingButtonsLayout);
        progressTextView = (TextView) findViewById(R.id.progressText);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        cookingViewLayout.setVisibility(visibility);
        progressBar.setVisibility(visibility);
        progressTextView.setVisibility(visibility);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cookingDataList = new ArrayList<>();
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        cookButton = (ImageButton) findViewById(R.id.cookButton);
        cookButton.setOnClickListener(new CookButtonListener(this));
        cookButton.setVisibility(View.INVISIBLE);
        Bundle extras = getIntent().getExtras();
        json = getIntent().getStringExtra("json");
        login = getIntent().getStringExtra("login");
        cookMode = false;
        if (json != null) {
            switchCookMode();
        }
        changeCookingButtonsVisibility(false);
        loggerView = (TextView) findViewById(R.id.logger);
    }

    private void parseMessageFromServer(String msg) {

    }

    private void customerLogic() {

        loggerView.setText("Customer logic");
        new Thread() {
            @Override
            public void run() {
                try {
                    // Create Socket instance
                    Socket socket = new Socket("192.168.179.94", 6666);

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    // Get input buffer
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    String line = "";

                    while ((line = br.readLine()) != null) {
//                        loggerView.setText(line);
                        String[] splitMsg = line.split("#");
                        Gson gson = new Gson();

                        CookingData cookingData = gson.fromJson(splitMsg[1],CookingData.class);
//                        LatLng cookPosition = cookingData.getLocation();
//                        Marker newCook = mMap.addMarker(new MarkerOptions().position(cookPosition).icon(BitmapDescriptorFactory.defaultMarker(
//                                BitmapDescriptorFactory.HUE_ORANGE)));
                        Message msg = new Message();
                        msg.obj = cookingData;

                        serverMessageHandler.sendMessage(msg);
//                        newCook.setVisible(true);
//                        newCook.setTitle(cookingData.getName());
                    }

                    br.close();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//                serverMessageHandler.sendEmptyMessage(0);
            }
        }.start();
    }


    private void cookLogic(LatLng latLng) {

        final String locationJSON = gson.toJson(latLng);
        CookingData deserialized = gson.fromJson(json, CookingData.class);
        deserialized.setLocation(latLng);
        final String completeJSON = gson.toJson(deserialized);

        // TODO Auto-generated method stub
        new Thread() {
            @Override
            public void run() {
                try {
                    // Create Socket instance
                    Socket socket = new Socket("192.168.179.94", 6666);

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                    bw.write("Ciao server\n");
//                    bw.flush();
                    bw.write("cook#" + completeJSON);
                    bw.flush();
                    // Get input buffer
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    String line = br.readLine();
                    while ((line = br.readLine()) != null) {

                    }
                    br.close();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//                serverMessageHandler.sendEmptyMessage(0);
            }
        }.start();
    }


    // Define Handler object
    private Handler serverMessageHandler = new Handler() {
        @Override
        // When there is message, execute this method
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // Update UI
            loggerView.setText("IN serverMessageHandler");
            CookingData data = (CookingData) msg.obj;
            Marker newMarker = mMap.addMarker(new MarkerOptions().position(data.getLocation()).icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_ORANGE)));
            newMarker.setTitle(data.getName());
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {
            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {


        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        mLocation = location;
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
                15));
        if (initialized) {
            return;
        }
        initialized = true;
        if (cookMode) {
            markerImage = (ImageView) findViewById(R.id.markerImage);
            markerImage.setVisibility(View.VISIBLE);
            confirmLocationButton = (Button) findViewById(R.id.confirmLocation);
            confirmLocationButton.setVisibility(View.VISIBLE);
            cookButton.setVisibility(View.INVISIBLE);
            confirmLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng latLng = mMap.getCameraPosition().target;
                    whereAmI=mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN)));
                    markerImage.setVisibility(View.INVISIBLE);
                    confirmLocationButton.setVisibility(View.INVISIBLE);
                    cookButton.setVisibility(View.VISIBLE);
                    changeCookingButtonsVisibility(true);

                    cookLogic(latLng);
                }
            });
        } else {
            customerLogic();
        }
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        cookButton.setVisibility(View.VISIBLE);


    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        return true;
    }

    public void loadCookMode() {
//        switchCookMode();
        if (mLocation != null ) {
//            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
//            cookButton.setImageResource(R.drawable.eat);
//            whereAmI=mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(
//                    BitmapDescriptorFactory.HUE_GREEN)));
            finish();
            Intent myIntent = new Intent(MapsActivity.this,CookingInfoActivity.class);
            myIntent.putExtra("login",login);
            MapsActivity.this.startActivity(myIntent);
        }

    }

    public void loadEatMode() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                    {
                        switchCookMode();
                        cancelCooking();
                        customerLogic();
                    }
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to quit cook mode? Your cooking session will be canceled. ").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

//        if (mLocation != null ) {
//            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
//            cookButton.setImageResource(R.drawable.cook_hat);
//            whereAmI.remove();
//        }

    }

    private void cancelCooking() {
        mMap.clear();

    }


}
