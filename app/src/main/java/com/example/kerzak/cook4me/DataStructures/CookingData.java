package com.example.kerzak.cook4me.DataStructures;

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
    private float ranking;

    public String getName() {
        return name;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public CookingData(String login, String name, List<String> categories, int dayFrom, int monthFrom, int yearFrom,
                       int hourFrom, int minuteFrom, int dayTo, int monthTo, int yearTo, int hourTo, int minuteTo,
                       int portions, boolean takeAwayOnly, int price, String notes, String currency) {
        this.login = login;
        this.name = name;
        this.setCategories(categories);
        this.setDayFrom(dayFrom);
        this.setMonthFrom(monthFrom);
        this.setYearFrom(yearFrom);
        this.setHourFrom(hourFrom);
        this.setMinuteFrom(minuteFrom);
        this.setDayTo(dayTo);
        this.setMonthFrom(monthTo);
        this.setYearTo(yearTo);
        this.setHourTo(hourTo);
        this.setMinuteTo(minuteTo);
        this.setPortions(portions);
        this.setTakeAwayOnly(takeAwayOnly);
        this.setPrice(price);
        this.setNotes(notes);
        this.setCurrency(currency);
    }

    public float getRanking() {
        return ranking;
    }

    public void setRanking(float ranking) {
        this.ranking = ranking;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isTakeAwayOnly() {
        return takeAwayOnly;
    }

    public void setTakeAwayOnly(boolean takeAwayOnly) {
        this.takeAwayOnly = takeAwayOnly;
    }

    public int getPortions() {
        return portions;
    }

    public void setPortions(int portions) {
        this.portions = portions;
    }

    public int getMinuteTo() {
        return minuteTo;
    }

    public void setMinuteTo(int minuteTo) {
        this.minuteTo = minuteTo;
    }

    public int getHourTo() {
        return hourTo;
    }

    public void setHourTo(int hourTo) {
        this.hourTo = hourTo;
    }

    public int getYearTo() {
        return yearTo;
    }

    public void setYearTo(int yearTo) {
        this.yearTo = yearTo;
    }

    public int getMonthTo() {
        return monthTo;
    }

    public void setMonthTo(int monthTo) {
        this.monthTo = monthTo;
    }

    public int getDayTo() {
        return dayTo;
    }

    public void setDayTo(int dayTo) {
        this.dayTo = dayTo;
    }

    public int getMinuteFrom() {
        return minuteFrom;
    }

    public void setMinuteFrom(int minuteFrom) {
        this.minuteFrom = minuteFrom;
    }

    public int getHourFrom() {
        return hourFrom;
    }

    public void setHourFrom(int hourFrom) {
        this.hourFrom = hourFrom;
    }

    public int getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(int yearFrom) {
        this.yearFrom = yearFrom;
    }

    public int getMonthFrom() {
        return monthFrom;
    }

    public void setMonthFrom(int monthFrom) {
        this.monthFrom = monthFrom;
    }

    public int getDayFrom() {
        return dayFrom;
    }

    public void setDayFrom(int dayFrom) {
        this.dayFrom = dayFrom;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}