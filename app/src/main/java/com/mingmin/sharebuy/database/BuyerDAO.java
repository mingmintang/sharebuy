package com.mingmin.sharebuy.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface BuyerDAO {
    @Query("select * from buyers where orderId = :orderId")
    List<Buyer> getBuyersByOrderId(String orderId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBuyer(Buyer buyer);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBuyers(List<Buyer> buyers);

    @Delete
    void deleteBuyers(Buyer... buyers);
}
