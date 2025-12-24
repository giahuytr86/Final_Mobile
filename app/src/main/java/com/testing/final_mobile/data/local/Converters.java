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

    @TypeConverter
    public static String fromList(List<String> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toList(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }
}
