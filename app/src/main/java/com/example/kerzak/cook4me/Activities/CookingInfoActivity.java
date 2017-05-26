package com.example.kerzak.cook4me.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.kerzak.cook4me.Enums.FoodCategories;
import com.example.kerzak.cook4me.Listeners.DatePickerListener;
import com.example.kerzak.cook4me.Listeners.TextMaxLengthListener;
import com.example.kerzak.cook4me.Listeners.TimePickerListener;
import com.example.kerzak.cook4me.R;
import com.example.kerzak.cook4me.WebSockets.CookingData;

import java.util.ArrayList;
import java.util.List;

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
    }

    private void initializeCategories() {

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
                        finish();
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
                        CookingData cookingData = new CookingData();
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
