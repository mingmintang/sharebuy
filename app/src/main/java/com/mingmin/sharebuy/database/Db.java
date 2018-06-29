package com.mingmin.sharebuy.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {EndOrder.class, Buyer.class}, version = 1)
public abstract class Db extends RoomDatabase {
    public abstract EndOrderDAO endOrderDAO();
    public abstract BuyerDAO buyerDAO();

    private static Db instance;
    public static Db getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, Db.class, "data.db").build();
        }
        return instance;
    }
}
