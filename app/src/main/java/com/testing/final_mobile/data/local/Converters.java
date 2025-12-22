package com.testing.final_mobile.data.local;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Converters {

    private static final Gson gson = new Gson();

    /**
     * Converter for Date to Long and vice-versa.
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * Converter for List<String> to JSON String and vice-versa.
     */
    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    /**
     * Converter for Map<String, Boolean> to JSON String and vice-versa.
     */
    @TypeConverter
    public static Map<String, Boolean> fromStringBooleanMap(String value) {
        if (value == null) {
            return null;
        }
        Type mapType = new TypeToken<Map<String, Boolean>>() {}.getType();
        return gson.fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromMap(Map<String, Boolean> map) {
        if (map == null) {
            return null;
        }
        return gson.toJson(map);
    }
}
