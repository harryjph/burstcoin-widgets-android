package com.harrysoft.burstinfowidget.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = [(SavedAccountDetails::class)], version = 1, exportSchema = false)
@TypeConverters(com.harrysoft.burstinfowidget.db.TypeConverters::class)
abstract class BurstWidgetsDatabase : RoomDatabase() {
    abstract fun accountDetailsDao(): AccountDetailsDao
}
