package com.example.kerzak.cook4me.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.kerzak.cook4me.Enums.FoodCategories;
import com.example.kerzak.cook4me.Listeners.CookButtonListener;
import com.example.kerzak.cook4me.WebSockets.ClientThread;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    HashMap<String, Marker> cooks;

    HashMap<Marker, CookingData> cookingDataMap;

    Button filtersButton;
    SeekBar seekBarPrice;
    TextView categoriesFilter;
    TextView priceFilterText;
    int currentPriceInFilter;
    Button applyFiltersButton;
    HashMap<CharSequence,Boolean> selectedCategories;
    EditText categoriesInput;
    private static final String SERVER = "ws://echo.websocket.org";

    /**
     * The timeout value in milliseconds for socket connection.
     */
    private static final int TIMEOUT = 5000;

    private GoogleMap mMap;

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
    Button cancelCookingButton;
    Button registerButton;
    private ImageView markerImage;
    private TextView loggerView;
    // For switching between cook and eat modes.
    private ImageButton cookButton;

    String json = null;
    String login = null;
    Gson gson = new Gson();
    CookingData myCookingData;
    Marker selectedMarker;
    String totalPortions= "";

    private ClientThread clientThread;
//    private CustomerThread customerThread;

    private boolean cookMode = false;
    private boolean registered = false;

    public  boolean isInCookMode() {
        return cookMode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cooks = new HashMap<>();
        cookingDataMap = new HashMap<>();
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

        cancelCookingButton = (Button) findViewById(R.id.cancelCooking);
        cancelCookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEatMode();
            }
        });

        cookButton = (ImageButton) findViewById(R.id.cookButton);
        cookButton.setOnClickListener(new CookButtonListener(this));
        cookButton.setVisibility(View.INVISIBLE);
        Bundle extras = getIntent().getExtras();
        json = getIntent().getStringExtra("json");
        login = LoginActivity.email;
        cookMode = false;
        if (json != null) {
            switchCookMode();
        }
        changeCookingButtonsVisibility(false);
        loggerView = (TextView) findViewById(R.id.logger);



        clientThread = new ClientThread(serverMessageHandler, login);
        clientThread.start();

        registerButton = (Button) findViewById(R.id.registerButton);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                            {
                                registered = true;
                                CookingData data = cookingDataMap.get(selectedMarker);
                                clientThread.writeLine("register#" + data.getLogin());
                                registerButton.setVisibility(View.INVISIBLE);
                                selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                                refreshSnippet(selectedMarker, true);
                                selectedMarker.showInfoWindow();
                            }
                            break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setMessage("Do you really want to register for this cooking? You will not be able to cancel your registration later.").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });

        currentPriceInFilter = -1;

        filtersButtonClick();
        applyFiltersButtonClick();
        initializeCategoriesFilter();
        changePriceFilter();

        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private void filtersButtonClick() {
        filtersButton = (Button) findViewById(R.id.buttonFilters);
        seekBarPrice = (SeekBar) findViewById(R.id.seekBarPrice);
        categoriesFilter = (TextView) findViewById(R.id.categoriesFilter);
        priceFilterText = (TextView) findViewById(R.id.filterPriceText);
        applyFiltersButton = (Button) findViewById(R.id.buttonApplyFilters);

        filtersButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int visibility = View.INVISIBLE;
                        if (seekBarPrice.getVisibility() == View.INVISIBLE ) {
                            visibility = View.VISIBLE;
                            int maxPrice = 0;
                            int tempPrice;
                            for(Map.Entry<Marker, CookingData> cooks : cookingDataMap.entrySet()) {
                                CookingData cookData = cooks.getValue();

                                tempPrice = cookData.getPrice();
                                if (tempPrice > maxPrice)
                                    maxPrice = tempPrice;
                            }
                            seekBarPrice.setMax(maxPrice);
                            if (currentPriceInFilter < 0 || currentPriceInFilter > maxPrice)
                                seekBarPrice.setProgress(maxPrice);
                            priceFilterText.setText(Integer.toString(seekBarPrice.getProgress()));
                        }

                        seekBarPrice.setVisibility(visibility);
                        categoriesFilter.setVisibility(visibility);
                        priceFilterText.setVisibility(visibility);
                        applyFiltersButton.setVisibility(visibility);
                    }
                }
        );
    }

    private void applyFiltersButtonClick() {
        seekBarPrice = (SeekBar) findViewById(R.id.seekBarPrice);
        categoriesFilter = (TextView) findViewById(R.id.categoriesFilter);
        priceFilterText = (TextView) findViewById(R.id.filterPriceText);
        applyFiltersButton = (Button) findViewById(R.id.buttonApplyFilters);

        applyFiltersButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int visibility = View.INVISIBLE;

                        seekBarPrice.setVisibility(visibility);
                        categoriesFilter.setVisibility(visibility);
                        priceFilterText.setVisibility(visibility);
                        applyFiltersButton.setVisibility(visibility);

                        applyFilters(seekBarPrice.getProgress());
                    }
                }
        );
    }

    private void applyFilters(int maxPrice) {

        for(Map.Entry<Marker, CookingData> cooks : cookingDataMap.entrySet()) {
            Marker cookMarker = cooks.getKey();
            CookingData cookData = cooks.getValue();

            if (satisfyFilters(cookData, maxPrice))
                cookMarker.setVisible(true);
            else
                cookMarker.setVisible(false);
        }
    }

    private boolean satisfyFilters (CookingData cookData, int maxPrice) {
        if (maxPrice < cookData.getPrice())
            return false;
        CharSequence cs;
        boolean cats = false;
        for (String c: cookData.getCategories()) {
            cs = (CharSequence) c;
            if (selectedCategories.get(cs)) {
                cats = true;
                break;
            }
        }
        if (!cats) {
            for(Map.Entry<CharSequence, Boolean> allCats : selectedCategories.entrySet()) {
                if (allCats.getValue())
                    return false;
            }
        }

        //TODO
        return true;
    }

    private void changePriceFilter() {
        priceFilterText = (TextView) findViewById(R.id.filterPriceText);
        seekBarPrice =(SeekBar) findViewById(R.id.seekBarPrice);
        seekBarPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                if (fromUser)
                    currentPriceInFilter = progress;
                priceFilterText.setText(Integer.toString(progress));
            }
        });
    }

    private void initializeCategoriesFilter() {

        selectedCategories = new HashMap<>();
        CharSequence[] allCategories = FoodCategories.getNames();
        for (CharSequence category : allCategories) {
            selectedCategories.put(category, false);
        }

        categoriesInput = (EditText) findViewById(R.id.categoriesFilter);
        categoriesInput.setOnClickListener(
                new View.OnClickListener() {
                    final CharSequence[] items = FoodCategories.getNames();
                    final boolean[] marked = new boolean[items.length];
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setMultiChoiceItems(items, marked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                CharSequence selectedCategory = items[which];
                                selectedCategories.put(selectedCategory, isChecked);
                            }
                        });
                        builder.setTitle("Categories");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                categoriesInput.setText("");
                                for (int i = 0; i < marked.length; i++) {
                                    if (marked[i]) {
                                        String prev = categoriesInput.getText().toString();
                                        if (prev.isEmpty()) {
                                            categoriesInput.setText(items[i]);
                                        } else {
                                            categoriesInput.setText(prev + ", "+ items[i]);
                                        }
                                    }
                                }
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
        );


    }

    private void customerLogic() {
        if (clientThread != null) {
            clientThread.writeLine("refresh");
        }
//        loggerView.setText("Customer logic");
//        clientThread.writeLine("cancelCooking");
        // TODO: tell server to finish cook ?
    }


    private void cookLogic(LatLng latLng) {
        json = getIntent().getStringExtra("json");
        CookingData deserialized = gson.fromJson(json, CookingData.class);
        deserialized.setLocation(latLng);
        deserialized.setLogin(login);
        myCookingData = deserialized;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(myCookingData.getPortions());
        final String completeJSON = gson.toJson(deserialized);
        totalPortions = String.valueOf(myCookingData.getPortions());
        // TODO Auto-generated method stub
        clientThread.writeLine("cook#" + completeJSON);
    }


    private Marker myMarker;
    // Define Handler object
    private Handler serverMessageHandler = new Handler() {
        @Override
        // When there is message, execute this method
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            loggerView.setText("IN serverMessageHandler");
            // ADD COOK
            if (msg.arg1 == 0) {

                CookingData data = (CookingData) msg.obj;
                if (!cookMode || myCookingData == null || !data.getLogin().equals(myCookingData.getLogin())) {
                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(data.getLocation()).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_ORANGE)));
//                    loggerView.setText("marker added in handler");
                    newMarker.setTitle(data.getName());

                    cooks.put(data.getLogin(), newMarker);
                    cookingDataMap.put(newMarker, data);
                    refreshSnippet(newMarker, false);
                }


            }
            // REMOVE COOK
            else if (msg.arg1 == 1) {
                String login = (String) msg.obj;
                if (cookMode && myCookingData != null && myCookingData.getLogin().equals(login)) {
                    myCookingData = null;
                    if (myMarker != null) myMarker.remove();
                    loadEatMode();
                } else {
                    Marker toBeRemoved = cooks.get(login);
//                    loggerView.setText("is marker null:" + (toBeRemoved == null));
                    if (toBeRemoved != null) {
//                        loggerView.setText("SELECTED ID = " + selectedMarker.getId() + " TBD ID = " + toBeRemoved.getId());
                        if (toBeRemoved == selectedMarker) {
                            selectedMarker = null;
                            // TODO: info dialog about cooking cancellation
                        }
                        toBeRemoved.remove();
                        if (cooks.containsKey(login)) {
                            cooks.remove(login);
                        }
                    }
                }



            }
            // CLEAR MAP
            else if (msg.arg1 == 2){
                mMap.clear();
            }
//             NEW USER REGISTERED TO COOKING
            else if (msg.arg1 == 3) {
                String[] splitMsg = (String[]) msg.obj;
                String cookName = splitMsg[1];
                int numberOfEaters = Integer.parseInt(splitMsg[2]);
                if (cookName.equals(login)) {
                    progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    progressBar.setProgress(numberOfEaters);
                    progressTextView = (TextView) findViewById(R.id.progressText);
                    progressTextView.setText("Users registered for your cooking: " + numberOfEaters + "/" + totalPortions);
                } else {
                    Marker cooksMarker = cooks.get(cookName);
                    CookingData cooksData = cookingDataMap.get(cooksMarker);
                    cooksData.setRegisteredCooks(numberOfEaters);
                    refreshSnippet(cooksMarker, false);
                }

            }

        }
    };

    private void refreshSnippet(Marker marker, boolean decreasePortions) {
        CookingData data = cookingDataMap.get(marker);
        String details = "";
        details += "PRICE: " + data.getPrice() + " " + data.getCurrency();
        details += "\nFROM: " + data.getHourFrom() + "." + data.getMinuteFrom() + " " +
                data.getDayFrom() + "-" + data.getMonthFrom() + "-" + data.getYearFrom();
        details += "\nTO: " + data.getHourTo() + "." + data.getMinuteTo() + " " +
                data.getDayTo() + "-" + data.getMonthTo() + "-" + data.getYearTo();
        details += "\nCATEGORIES:";
        for (String category : data.getCategories()) {
            details += " " + category;
        }
        int availablePortions = data.getAvailablePortions();
        if (decreasePortions) availablePortions--;
        details += "\nPORTIONS: " + availablePortions + "/" + data.getPortions();
        details += "\nTAKE-AWAY ONLY: " + data.getTakeAwayOnly();
        details += "\nNOTES: " + data.getNotes();
        marker.setSnippet(details);
    }

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
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        setInfoAdapter();
        cookButton.setVisibility(View.VISIBLE);

        if (cookMode) {
            markerImage = (ImageView) findViewById(R.id.markerImage);
            markerImage.setVisibility(View.VISIBLE);
            confirmLocationButton = (Button) findViewById(R.id.confirmLocation);
            confirmLocationButton.setVisibility(View.VISIBLE);
            cookButton.setVisibility(View.INVISIBLE);
            confirmLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.clear();
                    LatLng latLng = mMap.getCameraPosition().target;
                    myMarker=mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN)));
                    markerImage.setVisibility(View.INVISIBLE);
                    confirmLocationButton.setVisibility(View.INVISIBLE);
                    cookButton = (ImageButton) findViewById(R.id.cookButton);
                    cookButton.setVisibility(View.VISIBLE);
                    changeCookingButtonsVisibility(true);

                    cookLogic(latLng);

                }
            });
        } else {
//            loggerView.setText("522 cl");
//            customerLogic();
        }
    }

    private void setInfoAdapter() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Context context = getApplicationContext();
                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rank:
                Intent myIntent = new Intent(MapsActivity.this,SearchUserActivity.class);
                myIntent.putExtra("login",login);
                MapsActivity.this.startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                        loggerView.setText("598 cl");
                        customerLogic();
                    }
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to quit cook mode? Your cooking session will be canceled. ").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void cancelCooking() {
        mMap.clear();
        changeCookingButtonsVisibility(false);
        clientThread.writeLine("cancelCooking");
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (!cookMode) {
            if (selectedMarker != null) {
                selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
//                loggerView.setText("onMarkerClick=" + registered);
            }
            selectedMarker = marker;
            if (!registered) {
                selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                registerButton.setVisibility(View.VISIBLE);
            }
            refreshSnippet(marker, false);
            selectedMarker.showInfoWindow();
            registerButton = (Button) findViewById(R.id.registerButton);

        }
        return true;
    }


    @Override
    public void onMapClick(LatLng latLng) {
        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setVisibility(View.INVISIBLE);
        if (selectedMarker != null && !registered) {
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            loggerView.setText("onMapClick REGISTERED=" + registered);
        }
        selectedMarker = null;
    }
}
