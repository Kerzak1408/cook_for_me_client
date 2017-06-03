package com.example.kerzak.cook4me.WebSockets;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kerzak on 24-May-17.
 */

public class CookingData implements Serializable {
    private String login;
    private String name;
    private List<String> categories;
    private int dayFrom;
    private int monthFrom;
    private int yearFrom;
    private int hourFrom;
    private int minuteFrom;
    private int dayTo;
    private int monthTo;
    private int yearTo;
    private int hourTo;
    private int minuteTo;
    private int portions;
    private boolean takeAwayOnly;
    private int price;
    private String notes;
    private String currency;
    private LatLng location;

    public CookingData(String login, String name, List<String> categories, int dayFrom, int monthFrom, int yearFrom,
                       int hourFrom, int minuteFrom, int dayTo, int monthTo, int yearTo, int hourTo, int minuteTo,
                       int portions, boolean takeAwayOnly, int price, String notes, String currency) {
        this.login = login;
        this.name = name;
        this. categories = categories;
        this.dayFrom = dayFrom;
        this.monthFrom = monthFrom;
        this.yearFrom = yearFrom;
        this.hourFrom = hourFrom;
        this.minuteFrom = minuteFrom;
        this.dayTo = dayTo;
        this.monthFrom = monthTo;
        this.yearTo = yearTo;
        this.hourTo = hourTo;
        this.minuteTo = minuteTo;
        this.portions = portions;
        this.takeAwayOnly = takeAwayOnly;
        this.price = price;
        this.notes = notes;
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public  LatLng getLocation() {
        return this.location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
