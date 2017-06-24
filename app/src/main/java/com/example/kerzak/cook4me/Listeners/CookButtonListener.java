package com.example.kerzak.cook4me.Listeners;

import android.view.View;

import com.example.kerzak.cook4me.Activities.MapsActivity;

/**
 * Created by Kerzak on 23-May-17.
 */

public class CookButtonListener implements View.OnClickListener {


    private MapsActivity mapsActivity;

    public CookButtonListener(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }

    @Override
    public void onClick(View v) {
        if (mapsActivity.isInCookMode()) {
            mapsActivity.loadEatMode();
        } else {
            mapsActivity.loadCookMode(false);
        }
    }

}
