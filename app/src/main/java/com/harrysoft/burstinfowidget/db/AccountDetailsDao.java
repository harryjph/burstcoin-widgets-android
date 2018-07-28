package com.harrysoft.burstinfowidget.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface AccountDetailsDao {
    @Query("SELECT * FROM SavedAccountDetails WHERE appWidgetId = (:appWidgetId)")
    SavedAccountDetails findByAppWidgetId(int appWidgetId);

    @Insert
    void insert(SavedAccountDetails... savedAccounts);

    @Delete
    void delete(SavedAccountDetails savedAccount);

    @Update
    void update(SavedAccountDetails... savedAccounts);
}
