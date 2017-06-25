package com.example.kerzak.cook4me.DataStructures;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kerzak on 24-May-17.
 */

public class CookingData implements Serializable {
    private String login;
    private String nickname;
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
    private int availablePortions;
    private int portions;
    private boolean takeAwayOnly;
    private int price;
    private String notes;
    private String currency;
    private LatLng location;
    private float ranking;


    public CookingData(String login, String name, List<String> categories, int dayFrom, int monthFrom, int yearFrom,
                       int hourFrom, int minuteFrom, int dayTo, int monthTo, int yearTo, int hourTo, int minuteTo,
                       int portions, boolean takeAwayOnly, int price, String notes, String currency) {
        this.login = login;
        this.name = name;
        this.categories = categories;
        this.dayFrom = dayFrom;
        this.monthFrom = monthFrom;
        this.yearFrom = yearFrom;
        this.hourFrom = hourFrom;
        this.minuteFrom = minuteFrom;
        this.dayTo = dayTo;
        this.monthTo = monthTo;
        this.yearTo = yearTo;
        this.hourTo = hourTo;
        this.minuteTo = minuteTo;
        this.portions = portions;
        this.availablePortions = portions;
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

    public String getLogin() {
        return login;
    }

    public int getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public int getHourFrom() {
        return hourFrom;
    }

    public int getMinuteFrom() {
        return minuteFrom;
    }

    public int getHourTo() {
        return hourTo;
    }

    public int getMinuteTo() {
        return minuteTo;
    }

    public int getDayFrom() {
        return dayFrom;
    }

    public int getYearFrom() {
        return yearFrom;
    }

    public int getMonthFrom() {
        return monthFrom;
    }

    public int getDayTo() {
        return dayTo;
    }

    public int getMonthTo() {
        return monthTo;
    }

    public int getYearTo() {
        return yearTo;
    }

    public boolean getTakeAwayOnly() {
        return takeAwayOnly;
    }

    public String getNotes() {
        return notes;
    }

    public int getAvailablePortions() {
        return availablePortions;
    }

    public int getPortions() {
        return portions;
    }

    public void setRegisteredCooks(int numberOfEaters) {
        this.availablePortions = portions - numberOfEaters;
    }

    public List<String> getCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    public float getRanking() {
        return ranking;
    }

    public String getDateFromString() {
        return String.format("%02d", dayFrom) + "." + String.format("%02d", monthFrom) + "." + yearFrom;
    }

    public String getDateToString() {
        return String.format("%02d", dayTo) + "." + String.format("%02d", monthTo) + "." + yearTo;
    }

    public String getTimeFromString() {
        return String.format("%02d", hourFrom) + ":" + String.format("%02d", minuteFrom);
    }

    public String getTimeToString() {
        return String.format("%02d", hourTo) + ":" + String.format("%02d", minuteTo);
    }

    public void setAvailablePortions(int availablePortions) {
        this.availablePortions = availablePortions;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
