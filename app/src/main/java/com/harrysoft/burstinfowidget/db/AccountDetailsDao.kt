package com.harrysoft.burstinfowidget.db

import android.arch.persistence.room.*

@Dao
interface AccountDetailsDao {
    @Query("SELECT * FROM SavedAccountDetails WHERE appWidgetId = (:appWidgetId)")
    fun findByAppWidgetId(appWidgetId: Int): SavedAccountDetails

    @Insert
    fun insert(vararg savedAccounts: SavedAccountDetails)

    @Delete
    fun delete(savedAccount: SavedAccountDetails)

    @Update
    fun update(vararg savedAccounts: SavedAccountDetails)
}
