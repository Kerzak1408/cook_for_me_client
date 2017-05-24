package com.example.kerzak.cook4me.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.kerzak.cook4me.Listeners.DatePickerListener;
import com.example.kerzak.cook4me.Listeners.TimePickerListener;
import com.example.kerzak.cook4me.R;

import java.util.ArrayList;
import java.util.List;

public class CookingInfoActivity extends AppCompatActivity {

    EditText timePickerInput;
    EditText datePickerInput;
    EditText timePickerInput2;
    EditText datePickerInput2;
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
