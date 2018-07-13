package com.mvcoder.videosegment.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by mvcoder on 2017/10/13.
 */

public class GsonUtil {

    private Gson gson;

    private Gson printGson;

    private GsonUtil() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    public static GsonUtil getInstance() {
        return GsonFactory.instance;
    }

    private static class GsonFactory {
        protected static final GsonUtil instance = new GsonUtil();
    }

    public Gson prettyPrintGson() {
        if (printGson == null)
            printGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
        return printGson;
    }

    public Gson defaultGson() {
        return gson;
    }

    public Gson exclusiveClassesGson(final Class<?>... classes) {
        GsonBuilder builder = new GsonBuilder();
        builder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                for (int i = 0; i < classes.length; i++) {
                    if (clazz.getName().equals(classes[i].getName())) {
                        return true;
                    }
                }
                return false;
            }
        });
        return builder.create();
    }

    /**
     * @param isExclusive isExclusive == true时，gson序列化将忽略fileds的字段;
     *                    false则只显示fileds字段
     * @param fields
     * @return
     */
    public Gson fieldsGson(final boolean isExclusive, final String... fields) {
        GsonBuilder builder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
        builder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                for (int i = 0; i < fields.length; i++) {
                    if (f.getName().equals(fields[i])) {
                        if (isExclusive) return true;
                        else return false;
                    }
                }
                return !isExclusive;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
        return builder.create();
    }

}
