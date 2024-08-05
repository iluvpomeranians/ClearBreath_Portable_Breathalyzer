package com.example.coen390androidproject_breathalyzerapp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String convertListToJson(List<Float> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    public static List<Float> convertJsonToList(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Float>>() {}.getType();
        return gson.fromJson(jsonString, type);
    }
}
