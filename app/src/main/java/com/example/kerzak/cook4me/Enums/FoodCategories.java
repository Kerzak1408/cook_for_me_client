package com.example.kerzak.cook4me.Enums;

import java.util.Arrays;

/**
 * Created by Kerzak on 26-May-17.
 */

public enum FoodCategories {
    Pasta, Chicken, Beef, Lamb, Pork, Salad, Fish;

    public static String[] getNames() {
            return Arrays.toString(FoodCategories.values()).replaceAll("^.|.$", "").split(", ");
    }
}
