package com.mingmin.sharebuy.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public interface EndOrderDAO {
    @Query("select * from endOrders where state = 2 order by updateTime DESC")
    List<EndOrder> getEndOrders();

    @Transaction
    @Query("select * from endOrders where state = 2 order by updateTime DESC")
    List<EndOrderAndBuyers> getEndOrdersWithBuyers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEndOrder(EndOrder endOrder);

    @Delete
    void deleteEndOrders(EndOrder... endOrders);
}
