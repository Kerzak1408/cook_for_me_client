
package com.example.kerzak.cook4me.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.kerzak.cook4me.DataStructures.CookingData;
import com.example.kerzak.cook4me.Enums.FoodCategories;
import com.example.kerzak.cook4me.Listeners.DatePickerListener;
import com.example.kerzak.cook4me.Listeners.TextMaxLengthListener;
import com.example.kerzak.cook4me.Listeners.TimePickerListener;
import com.example.kerzak.cook4me.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.R.attr.data;

public class CookingInfoActivity extends AppCompatActivity {

    EditText timePickerInput;
    EditText datePickerInput;
    EditText timePickerInput2;
    EditText datePickerInput2;
    EditText portionsCountInput;
    EditText foodNameInput;
    EditText priceInput;
    EditText notesInput;
    EditText categoriesInput;
    Button cookConfirm;
    Button cancelButton;
    HashMap<CharSequence,Boolean> selectedCategories;
    public static CookingData thisCookingData;
    String login = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cooking_info);
        initializeTimePickers();
        initializeCurrencySpinner();
        initializeCookConfirmButton();
        initializeDatePickers();
        initializeCancelButton();
        initializePortionsCountInput();
        initializeFoodNameInput();
        initializePriceInput();
        initializeNotesInput();
        initializeCategories();

        login = getIntent().getStringExtra("login");
    }

    private void initializeCategories() {

        selectedCategories = new HashMap<>();
        CharSequence[] allCategories = FoodCategories.getNames();
        for (CharSequence category : allCategories) {
            selectedCategories.put(category, false);
        }

        categoriesInput = (EditText) findViewById(R.id.categoriesInput);
        categoriesInput.setOnClickListener(
                new View.OnClickListener() {
                    final CharSequence[] items = FoodCategories.getNames();
                    final boolean[] marked = new boolean[items.length];
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(CookingInfoActivity.this);
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

    private void initializeNotesInput() {
        notesInput = (EditText) findViewById(R.id.notesInput);
        notesInput.addTextChangedListener(new TextMaxLengthListener(200));
    }

    private void initializeFoodNameInput() {
        foodNameInput = (EditText) findViewById(R.id.foodNameInput);
        foodNameInput.addTextChangedListener(new TextMaxLengthListener(40));
    }

    private void initializePortionsCountInput() {
        portionsCountInput = (EditText) findViewById(R.id.portionsCount);
        portionsCountInput.addTextChangedListener(new TextMaxLengthListener(4));
    }

    private void initializePriceInput(){
        priceInput = (EditText) findViewById(R.id.priceInput);
        priceInput.addTextChangedListener(new TextMaxLengthListener(8));
    }

    private void initializeCancelButton() {
        cancelButton = (Button) findViewById(R.id.cancelCooking);
        cancelButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(CookingInfoActivity.this,MapsActivity.class);
                        finish();
                        CookingInfoActivity.this.startActivity(myIntent);
                    }
                }
        );
    }

    private void initializeCookConfirmButton() {
        cookConfirm = (Button) findViewById(R.id.cookInfoOK);
        cookConfirm.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //here
                        boolean everythingOK = true;
                        if ((portionsCountInput.getText().toString()).equals("")) {
                            everythingOK = false;
                            portionsCountInput.setError(getString(R.string.error_field_required));
                        }
                        if ((foodNameInput.getText().toString()).equals("")) {
                            everythingOK = false;
                            foodNameInput.setError(getString(R.string.error_field_required));
                        }
                        if ((priceInput.getText().toString()).equals("")) {
                            everythingOK = false;
                            priceInput.setError(getString(R.string.error_field_required));
                        }
                        int[] timeFrom = new int[5];
                        int[] timeTo = new int[5];
                        String[] temp = (datePickerInput.getText().toString()).split("\\.");
                        for (int i=0; i<3; i++)
                            timeFrom[i] = Integer.parseInt(temp[i]);
                        temp = (timePickerInput.getText().toString()).split(":");
                        for (int i=0; i<2; i++)
                            timeFrom[i + 3] = Integer.parseInt(temp[i]);
                        temp = (datePickerInput2.getText().toString()).split("\\.");
                        for (int i=0; i<3; i++)
                            timeTo[i] = Integer.parseInt(temp[i]);
                        temp = (timePickerInput2.getText().toString()).split(":");
                        for (int i=0; i<2; i++)
                            timeTo[i + 3] = Integer.parseInt(temp[i]);

                        datePickerInput2.setError(null);
                        timePickerInput2.setError(null);
                        if (timeFrom[2] > timeTo[2]) {
                            datePickerInput2.setError("Invalid date");
                            everythingOK = false;
                        }
                        else if (timeTo[2] == timeFrom[2]) {
                            if (timeFrom[1] > timeTo[1]) {
                                datePickerInput2.setError("Invalid date");
                                everythingOK = false;
                            }
                            else if (timeFrom[1] == timeTo[1]) {
                                if (timeFrom[0] > timeTo[0]) {
                                    datePickerInput2.setError("Invalid date");
                                    everythingOK = false;
                                }
                                else if (timeFrom[0] == timeTo[0]) {
                                    if (timeFrom[3] > timeTo[3]) {
                                        timePickerInput2.setError("Invalid time");
                                        everythingOK = false;
                                    }
                                    else if (timeFrom[3] == timeTo[3]) {
                                        if (timeFrom[4] >= timeTo[4]) {
                                            timePickerInput2.setError("Invalid time");
                                            everythingOK = false;
                                        }
                                    }
                                }
                            }
                        }
                        if (everythingOK) {
                                List<String> categories = new LinkedList<String>();
                                for(Map.Entry<CharSequence,Boolean> cat : selectedCategories.entrySet()) {
                                    String key = cat.getKey().toString();
                                    Boolean value = cat.getValue();
                                    if (value)
                                        categories.add(key);
                                }

                                //TODO ID kuchara
                                Switch takeAwaySwitch = (Switch) findViewById(R.id.takeAwaySwitch);
                                boolean takeAway = takeAwaySwitch.isChecked();
                                Spinner currencySpinner = (Spinner) findViewById(R.id.currencySpinner);
                                String currency = currencySpinner.getSelectedItem().toString();
                                //String currency = currencySpinner
                                CookingInfoActivity.thisCookingData = new CookingData("", foodNameInput.getText().toString(), categories,
                                        timeFrom[0],timeFrom[1],timeFrom[2], timeFrom[3], timeFrom[4],
                                        timeTo[0],timeTo[1],timeTo[2],timeTo[3],timeTo[4],
                                        Integer.parseInt(portionsCountInput.getText().toString()), takeAway,
                                        Integer.parseInt(priceInput.getText().toString()), notesInput.getText().toString(), currency);
                                thisCookingData.setLogin(login);
                                Gson gson = new Gson();
                                String json = gson.toJson(CookingInfoActivity.thisCookingData );

                                Intent myIntent = new Intent(CookingInfoActivity.this,MapsActivity.class);
                                myIntent.putExtra("json",json);
                                myIntent.putExtra("login",login);
                                finish();
                                CookingInfoActivity.this.startActivity(myIntent);

                        }

                    }
                }
        );

    }



    private void initializeCurrencySpinner() {

        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("EUR");
        spinnerArray.add("USD");
        spinnerArray.add("CZK");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.currencySpinner);
        sItems.setAdapter(adapter);
    }

    private void initializeDatePickers() {
        datePickerInput = (EditText) findViewById(R.id.datePickerInput);
        datePickerInput2 = (EditText) findViewById(R.id.datePickerInput2);
        datePickerInput.setOnClickListener(new DatePickerListener(datePickerInput,CookingInfoActivity.this));
        datePickerInput2.setOnClickListener(new DatePickerListener(datePickerInput2,CookingInfoActivity.this));
    }

    private void initializeTimePickers() {
        timePickerInput = (EditText) findViewById(R.id.timePickerInput);
        timePickerInput2 = (EditText) findViewById(R.id.timePickerInput2);
        timePickerInput.setOnClickListener(new TimePickerListener(timePickerInput, CookingInfoActivity.this));
        timePickerInput2.setOnClickListener(new TimePickerListener(timePickerInput2, CookingInfoActivity.this));
    }
}
