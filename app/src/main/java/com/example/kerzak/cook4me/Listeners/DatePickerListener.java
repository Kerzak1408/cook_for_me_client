package com.example.kerzak.cook4me.Listeners;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.kerzak.cook4me.Activities.CookingInfoActivity;

import java.util.Calendar;

/**
 * Created by Kerzak on 24-May-17.
 */

public class DatePickerListener implements View.OnClickListener {
    Calendar c = Calendar.getInstance();
    int curr_day = c.get(Calendar.DAY_OF_MONTH);
    int curr_month = c.get(Calendar.MONTH) + 1;
    int curr_year = c.get(Calendar.YEAR);

    EditText datePickerInput;
    Context context;

    public DatePickerListener(EditText datePickerInput, Context context) {
        this.datePickerInput = datePickerInput;
        this.context = context;
        datePickerInput.setText(curr_day + "." + curr_month + "." + curr_year);
    }

    @Override
    public void onClick(View v) {
        DatePickerDialog datePicker = new DatePickerDialog(context,new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                datePickerInput.setText(dayOfMonth + "." + month + "." + year);
            }
        },curr_year,curr_month,curr_day);
        datePicker.show();
    }

}
