package com.mingmin.sharebuy.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateTypeConverter {

    @TypeConverter
    public Long toLong(Date value) {
        return value == null ? null : value.getTime();
    }

    @TypeConverter
    public Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }
}
