package com.example.kerzak.cook4me.Listeners;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.kerzak.cook4me.Activities.CookingInfoActivity;

import java.util.Calendar;

/**
 * Created by Kerzak on 24-May-17.
 */

public class TimePickerListener implements View.OnClickListener {
    Calendar c = Calendar.getInstance();
    int curr_hour = c.get(Calendar.HOUR_OF_DAY);
    int curr_minute = c.get(Calendar.MINUTE);

    EditText timePickerInput;
    Context context;

    public TimePickerListener(EditText timePickerInput, Context context) {
        this.timePickerInput = timePickerInput;
        this.context = context;
        timePickerInput.setText(String.format("%02d",curr_hour) + ":" + String.format("%02d",curr_minute));
    }

    @Override
    public void onClick(View v) {
        String previousText = timePickerInput.getText().toString();
        String[] previousTextArr = previousText.split(":");
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timePickerInput.setText( String.format("%02d",selectedHour) + ":" + String.format("%02d",selectedMinute));
            }
        }, curr_hour, curr_minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
}
