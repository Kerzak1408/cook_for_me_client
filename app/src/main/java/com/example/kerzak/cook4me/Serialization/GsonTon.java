package com.example.kerzak.cook4me.Serialization;

import com.example.kerzak.cook4me.DataStructures.CookingData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.LongSerializationPolicy;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Kerzak on 22-Jun-17.
 */

public class GsonTon {
    private Gson gson = null;
    private static GsonTon instance = null;

    private GsonTon() {
        gson = new Gson();
    }

    public static GsonTon getInstance() {
        if (instance == null) {
            instance = new GsonTon();
        }
        return instance;
    }

    public Gson getGson() {
        return gson;
    }
}
