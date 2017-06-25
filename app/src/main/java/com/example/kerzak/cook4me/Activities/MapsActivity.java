package com.example.kerzak.cook4me.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.kerzak.cook4me.DataStructures.CookingData;
import com.example.kerzak.cook4me.Enums.FoodCategories;
import com.example.kerzak.cook4me.Listeners.CookButtonListener;
import com.example.kerzak.cook4me.Listeners.DatePickerListener;
import com.example.kerzak.cook4me.Listeners.TimePickerListener;
import com.example.kerzak.cook4me.Serialization.GsonTon;
import com.example.kerzak.cook4me.WebSockets.ClientThread;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    HashMap<String, Marker> cooks;
    HashMap<Marker, CookingData> cookingDataMap;

    Button filtersButton;
    SeekBar seekBarPrice;
    TextView categoriesFilter;
    TextView priceFilterText;
    TextView priceTextInfoFilter;
    Switch eatingThereSwitch;
    TextView fromTextFilter;
    EditText fromDateFilter;
    EditText fromTimeFilter;
    TextView toTeXtFilter;
    EditText toDateFilter;
    EditText toTimeFilter;
    EditText categoriesInput;
    RatingBar ratingBar;
    Button editCookingButton;

    int currentPriceInFilter;
    boolean notOpenedFilter;
    Button applyFiltersButton;
    HashMap<CharSequence,Boolean> selectedCategories;


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

        changeCookingButtonsVisibility(false);
        loggerView = (TextView) findViewById(R.id.logger);

        clientThread = ClientThread.getInstance(serverMessageHandler);

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
                                selectedMarker = null;
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
        notOpenedFilter = true;

        filtersButtonClick();
        applyFiltersButtonClick();
        initializeCategoriesFilter();
        changePriceFilter();
        initializeDatePickers();
        initializeTimePickers();
        editCookingButtonClick();
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        Drawable drawable = ratingBar.getProgressDrawable();
        drawable.setColorFilter(Color.parseColor("#E74C3C"), PorterDuff.Mode.SRC_ATOP);

        Bundle extras = getIntent().getExtras();
        json = getIntent().getStringExtra("json");
        login = LoginActivity.email;
        cookMode = false;
        if (json != null) {
            switchCookMode();
        } else {
            clientThread.refresh();
        }

        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void editCookingButtonClick() {
        editCookingButton = (Button) findViewById(R.id.editCooking);
        editCookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCookMode(true);
            }
        });
    }


    public void switchCookMode() {
        cookMode = !cookMode;
        cookingViewLayout = (LinearLayout) findViewById(R.id.cookingButtonsLayout);
        progressTextView = (TextView) findViewById(R.id.progressText);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        filtersButton = (Button) findViewById(R.id.buttonFilters);
        boolean edited = getIntent().getBooleanExtra("edited", false);
        int visibility = View.VISIBLE;
        if (cookMode) {
            visibility = View.INVISIBLE;
        }
        filtersButton.setVisibility(visibility);
        cookButton.setVisibility(visibility);

        changeCookingButtonsVisibility(!edited);
    }

    public void changeCookingButtonsVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;

        cookingViewLayout = (LinearLayout) findViewById(R.id.cookingButtonsLayout);
        progressTextView = (TextView) findViewById(R.id.progressText);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        cookingViewLayout.setVisibility(visibility);
        progressBar.setVisibility(visibility);
        progressTextView.setVisibility(visibility);

        if (myCookingData != null) {
            progressBar.setMax(myCookingData.getPortions());
            progressBar.setProgress(myCookingData.getPortions() - myCookingData.getAvailablePortions());
        }
    }

    private void filtersButtonClick() {
        filtersButton = (Button) findViewById(R.id.buttonFilters);
        seekBarPrice = (SeekBar) findViewById(R.id.seekBarPrice);
        categoriesFilter = (TextView) findViewById(R.id.categoriesFilter);
        priceFilterText = (TextView) findViewById(R.id.filterPriceText);
        priceTextInfoFilter = (TextView) findViewById(R.id.priceTextInfoFilter);
        eatingThereSwitch = (Switch) findViewById(R.id.eatingThereSwitch);
        fromTextFilter = (TextView) findViewById(R.id.dateFromTextFilter);
        fromDateFilter = (EditText) findViewById(R.id.dateFromFilter);
        fromTimeFilter = (EditText) findViewById(R.id.timeFromFilter);
        toTeXtFilter = (TextView) findViewById(R.id.dateToTextFilter);
        toDateFilter = (EditText) findViewById(R.id.dateToFilter);
        fromDateFilter = (EditText) findViewById(R.id.timeToFilter);
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

                            if (notOpenedFilter) {
                                setDateAndTimePicker();
                                notOpenedFilter = false;
                            }
                            seekBarPrice.setMax(maxPrice);
                            if (currentPriceInFilter < 0 || currentPriceInFilter > maxPrice)
                                seekBarPrice.setProgress(maxPrice);
                            priceFilterText.setText(Integer.toString(seekBarPrice.getProgress()));
                        }

                        seekBarPrice.setVisibility(visibility);
                        categoriesFilter.setVisibility(visibility);
                        priceFilterText.setVisibility(visibility);
                        priceTextInfoFilter.setVisibility(visibility);
                        eatingThereSwitch.setVisibility(visibility);
                        fromTextFilter.setVisibility(visibility);
                        fromDateFilter.setVisibility(visibility);
                        fromTimeFilter.setVisibility(visibility);
                        toTeXtFilter.setVisibility(visibility);
                        toDateFilter.setVisibility(visibility);
                        toTimeFilter.setVisibility(visibility);
                        applyFiltersButton.setVisibility(visibility);
                    }
                }
        );
    }

    private void applyFiltersButtonClick() {
        seekBarPrice = (SeekBar) findViewById(R.id.seekBarPrice);
        categoriesFilter = (TextView) findViewById(R.id.categoriesFilter);
        priceFilterText = (TextView) findViewById(R.id.filterPriceText);
        priceTextInfoFilter = (TextView) findViewById(R.id.priceTextInfoFilter);
        eatingThereSwitch = (Switch) findViewById(R.id.eatingThereSwitch);
        fromTextFilter = (TextView) findViewById(R.id.dateFromTextFilter);
        fromDateFilter = (EditText) findViewById(R.id.dateFromFilter);
        fromTimeFilter = (EditText) findViewById(R.id.timeFromFilter);
        toTeXtFilter = (TextView) findViewById(R.id.dateToTextFilter);
        toDateFilter = (EditText) findViewById(R.id.dateToFilter);
        fromDateFilter = (EditText) findViewById(R.id.timeToFilter);
        applyFiltersButton = (Button) findViewById(R.id.buttonApplyFilters);


        applyFiltersButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int visibility = View.INVISIBLE;

                        seekBarPrice.setVisibility(visibility);
                        categoriesFilter.setVisibility(visibility);
                        priceFilterText.setVisibility(visibility);
                        priceTextInfoFilter.setVisibility(visibility);
                        eatingThereSwitch.setVisibility(visibility);
                        fromTextFilter.setVisibility(visibility);
                        fromDateFilter.setVisibility(visibility);
                        fromTimeFilter.setVisibility(visibility);
                        toTeXtFilter.setVisibility(visibility);
                        toDateFilter.setVisibility(visibility);
                        toTimeFilter.setVisibility(visibility);
                        applyFiltersButton.setVisibility(visibility);

                        applyFilters(seekBarPrice.getProgress(), eatingThereSwitch.isChecked(), fromDateFilter.getText().toString(),
                                fromTimeFilter.getText().toString(), toDateFilter.getText().toString(), toTimeFilter.getText().toString());
                    }
                }
        );
    }

    private void applyFilters(int maxPrice, boolean eatingThere, String fromDate, String fromTime, String toDate, String toTime) {
        for(Map.Entry<Marker, CookingData> cooks : cookingDataMap.entrySet()) {
            Marker cookMarker = cooks.getKey();
            CookingData cookData = cooks.getValue();

            if (satisfyFilters(cookData, maxPrice, eatingThere, fromDate, fromTime, toDate, toTime))
                cookMarker.setVisible(true);
            else
                cookMarker.setVisible(false);
        }
    }

    private boolean satisfyFilters (CookingData cookData, int maxPrice, boolean eatingThere, String fromDate, String fromTime, String toDate, String toTime) {
        if (maxPrice < cookData.getPrice())
            return false;
        if (eatingThere && cookData.getTakeAwayOnly())
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

        String cookDataDateFrom = cookData.getDayFrom()+"."+cookData.getMonthFrom()+"."+cookData.getYearFrom();
        String cookDataTimeFrom = cookData.getHourFrom()+":"+cookData.getMinuteFrom();

        String cookDataDateTo = cookData.getDayTo()+"."+cookData.getMonthTo()+"."+cookData.getYearTo();
        String cookDataTimeTo = cookData.getHourTo()+":"+cookData.getMinuteTo();

        if (isFirstBeforeSecond(toDate, toTime, cookDataDateFrom, cookDataTimeFrom) || isFirstBeforeSecond(cookDataDateTo, cookDataTimeTo, fromDate, fromTime))
            return false;
        else
            return true;
    }

    boolean isFirstBeforeSecond(String date1, String time1, String date2, String time2) {
        int[] timeFirst = new int[5];
        int[] timeSecond = new int[5];
        String[] temp = date1.split("\\.");
        for (int i=0; i<3; i++)
            timeFirst[i] = Integer.parseInt(temp[i]);
        temp = time1.split(":");
        for (int i=0; i<2; i++)
            timeFirst[i + 3] = Integer.parseInt(temp[i]);
        temp = date2.split("\\.");
        for (int i=0; i<3; i++)
            timeSecond[i] = Integer.parseInt(temp[i]);
        temp = time2.split(":");
        for (int i=0; i<2; i++)
            timeSecond[i + 3] = Integer.parseInt(temp[i]);

        if (timeFirst[2] > timeSecond[2]) {
            return false;
        }
        else if (timeSecond[2] == timeFirst[2]) {
            if (timeFirst[1] > timeSecond[1]) {
                return false;
            }
            else if (timeFirst[1] == timeSecond[1]) {
                if (timeFirst[0] > timeSecond[0]) {
                    return false;
                }
                else if (timeFirst[0] == timeSecond[0]) {
                    if (timeFirst[3] > timeSecond[3]) {
                        return false;
                    }
                    else if (timeFirst[3] == timeSecond[3]) {
                        if (timeFirst[4] >= timeSecond[4]) {
                            return false;
                        }
                    }
                }
            }
        }

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
                                // if no categories were chose
                                if ("".equals(categoriesInput.getText().toString())) {
                                    categoriesInput.setText("Categories");
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
                loggerView.setText("in handler 0");
                CookingData data = (CookingData) msg.obj;
                if (!cookMode || myCookingData == null || !data.getLogin().equals(myCookingData.getLogin())) {
                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(data.getLocation()).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_ORANGE)));
//                    loggerView.setText("marker added in handler");
                    newMarker.setTitle(data.getName());

                    cooks.put(data.getLogin(), newMarker);
                    cookingDataMap.put(newMarker, data);
                    refreshSnippet(newMarker, false);
                    loggerView.setText("new cook placed");
                }


            }
            // REMOVE COOK
            else if (msg.arg1 == 1) {
                String login = (String) msg.obj;
                if (cookMode && myCookingData != null && myCookingData.getLogin().equals(login)) {
//                    myCookingData = null;
//                    if (myMarker != null) myMarker.remove();
//                    loadEatMode();
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
            // I am cooking already
            else if (msg.arg1 == 4) {
                String[] splitMsg = (String[]) msg.obj;
                int customersCount = Integer.parseInt(splitMsg[1]);
                myCookingData = GsonTon.getInstance().getGson().fromJson(splitMsg[3], CookingData.class);
                cookMode = false;
                switchCookMode();
                changeCookingButtonsVisibility(true);
                myMarker = cooks.get(LoginActivity.email);
                myMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

        }
    };

    private void refreshSnippet(Marker marker, boolean decreasePortions) {
        CookingData data = cookingDataMap.get(marker);
        String details = "";
        details += "COOK: " + data.getNickname();
        details += "\nPRICE: " + data.getPrice() + " " + data.getCurrency();
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


        if (cookMode) {
            markerImage = (ImageView) findViewById(R.id.markerImage);
            markerImage.setVisibility(View.VISIBLE);
            confirmLocationButton = (Button) findViewById(R.id.confirmLocation);

            if (getIntent().getBooleanExtra("edited",false)) {
                confirmLocationButton.setVisibility(View.VISIBLE);
            } else {
                myCookingData =
                        GsonTon.getInstance()
                                .getGson()
                                .fromJson(getIntent().getStringExtra("json"), CookingData.class);
                loadCookingMode(myCookingData.getLocation());
            }

            confirmLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng latLng = mMap.getCameraPosition().target;
                    json = getIntent().getStringExtra("json");
                    myCookingData = gson.fromJson(json, CookingData.class);
                    myCookingData.setLocation(latLng);
                    myCookingData.setLogin(login);
                    final String completeJSON = gson.toJson(myCookingData);
                    totalPortions = String.valueOf(myCookingData.getPortions());
                    loadCookingMode(latLng);
                    clientThread.writeLine("cook#" + completeJSON);

                }
            });
        } else {
//            loggerView.setText("522 cl");
//            customerLogic();
        }
    }

    private void loadCookingMode(LatLng myPos) {
        mMap.clear();
        LatLng latLng = myPos;
        myMarker=mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN)));
        markerImage.setVisibility(View.INVISIBLE);
        confirmLocationButton.setVisibility(View.INVISIBLE);
        cookButton = (ImageButton) findViewById(R.id.cookButton);
        cookButton.setVisibility(View.VISIBLE);
        changeCookingButtonsVisibility(true);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(myCookingData.getPortions());
        progressBar.setProgress(myCookingData.getPortions() - myCookingData.getAvailablePortions());
        cookButton.setVisibility(View.INVISIBLE);
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
        Intent myIntent;
        switch (item.getItemId()) {
            case R.id.rank:
                myIntent = new Intent(MapsActivity.this,SearchUserActivity.class);
                myIntent.putExtra("login",login);
                if (cookMode) {
                    String json = GsonTon.getInstance().getGson().toJson(myCookingData);
                    myIntent.putExtra("json", json);
                }
                MapsActivity.this.startActivity(myIntent);
                return true;
            case R.id.logout:
                clientThread.logout();
                myIntent = new Intent(MapsActivity.this,LoginActivity.class);
                MapsActivity.this.startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadCookMode(boolean edit) {
        if (mLocation != null ) {

            finish();
            Intent myIntent = new Intent(MapsActivity.this,CookingInfoActivity.class);
            myIntent.putExtra("login",login);
            if (edit) {
                Gson gson = GsonTon.getInstance().getGson();
                String json = gson.toJson(myCookingData);
                myIntent.putExtra("json",json);
            }
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
            }
            selectedMarker = marker;
            if (!registered) {
                selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                registerButton.setVisibility(View.VISIBLE);
            }
            refreshSnippet(marker, false);
            selectedMarker.showInfoWindow();
            registerButton = (Button) findViewById(R.id.registerButton);
            ratingBar.setVisibility(View.VISIBLE);
            CookingData cookingData = cookingDataMap.get(marker);
            ratingBar.setRating(cookingData.getRanking());
        }
        return true;
    }


    @Override
    public void onMapClick(LatLng latLng) {
        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setVisibility(View.INVISIBLE);
        ratingBar.setVisibility(View.INVISIBLE);
        if (selectedMarker != null && !registered) {
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            loggerView.setText("onMapClick REGISTERED=" + registered);
        }
        selectedMarker = null;
    }

    private void initializeDatePickers() {
        fromDateFilter = (EditText) findViewById(R.id.dateFromFilter);
        toDateFilter = (EditText) findViewById(R.id.dateToFilter);
        fromDateFilter.setOnClickListener(new DatePickerListener(fromDateFilter,MapsActivity.this));
        toDateFilter.setOnClickListener(new DatePickerListener(toDateFilter,MapsActivity.this));
    }

    private void initializeTimePickers() {
        fromTimeFilter = (EditText) findViewById(R.id.timeFromFilter);
        toTimeFilter = (EditText) findViewById(R.id.timeToFilter);
        fromTimeFilter.setOnClickListener(new TimePickerListener(fromTimeFilter, MapsActivity.this));
        toTimeFilter.setOnClickListener(new TimePickerListener(toTimeFilter, MapsActivity.this));
    }

    private void setDateAndTimePicker() {
        fromDateFilter = (EditText) findViewById(R.id.dateFromFilter);
        toDateFilter = (EditText) findViewById(R.id.dateToFilter);
        fromTimeFilter = (EditText) findViewById(R.id.timeFromFilter);
        toTimeFilter = (EditText) findViewById(R.id.timeToFilter);
        String tempDateFrom;
        String tempTimeFrom;
        String minDate = "";
        String minTime = "aa";
        String tempDateTo;
        String tempTimeTo;
        String maxDate = "bb";
        String maxTime = "cc";
        boolean start = true;
        for(Map.Entry<Marker, CookingData> cooks : cookingDataMap.entrySet()) {
            Marker marker = cooks.getKey();
            CookingData cookData = cooks.getValue();

            tempDateFrom = cookData.getDayFrom()+"."+cookData.getMonthFrom()+"."+cookData.getYearFrom();
            tempTimeFrom = cookData.getHourFrom()+":"+cookData.getMinuteFrom();
            tempDateTo = cookData.getDayTo()+"."+cookData.getMonthTo()+"."+cookData.getYearTo();
            tempTimeTo = cookData.getHourTo()+":"+cookData.getMinuteTo();
            if (start) {
                minDate = tempDateFrom;
                minTime = tempTimeFrom;
                maxDate = tempDateTo;
                maxTime = tempTimeTo;
                start = false;
            }
            else {
                if (isFirstBeforeSecond(tempDateFrom, tempTimeFrom, minDate, minTime)) {
                    minDate = tempDateFrom;
                    minTime = tempTimeFrom;
                }
                if (isFirstBeforeSecond(maxDate, maxTime, tempDateTo, tempTimeTo)) {
                    maxDate = tempDateTo;
                    maxTime = tempTimeTo;
                }
            }
        }

        if (!start) {
            fromDateFilter.setText(minDate);
            fromTimeFilter.setText(minTime);
            toDateFilter.setText(maxDate);
            toTimeFilter.setText(maxTime);
        }
    }
}
