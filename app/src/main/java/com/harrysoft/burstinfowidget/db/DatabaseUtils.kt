package com.harrysoft.burstinfowidget.db

import android.arch.persistence.room.Room
import android.content.Context
import com.harrysoft.burstinfowidget.R
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object DatabaseUtils {
    @JvmStatic
    fun insertSavedAccountDetails(context: Context, savedAccountDetails: SavedAccountDetails): Completable {
        return Completable.fromAction {
            val database = buildDatabase(context)
            try {
                database.accountDetailsDao().insert(savedAccountDetails)
            } finally {
                database.close()
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    @JvmStatic
    fun updateSavedAccountDetails(context: Context, accountDetails: SavedAccountDetails) : Completable {
        return Completable.fromAction({
            val database = buildDatabase(context)
            try {
                database.accountDetailsDao().update(accountDetails)
            } finally {
                database.close()
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    @JvmStatic
    fun getSavedAccountDetails(context: Context, appWidgetId: Int) : Single<SavedAccountDetails> {
        return Single.fromCallable({
            val database = buildDatabase(context)
            try {
                database.accountDetailsDao().findByAppWidgetId(appWidgetId) ?: throw IllegalArgumentException(context.getString(R.string.burst_account_title_not_set))
            } finally {
                database.close()
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteSavedAccountDetails(context: Context, appWidgetIds: IntArray) : Completable {
        return Completable.fromCallable {
            val database = buildDatabase(context)
            try {
                for (appWidgetId in appWidgetIds) {
                    val savedAccountDetails = database.accountDetailsDao().findByAppWidgetId(appWidgetId) ?: continue
                    database.accountDetailsDao().delete(savedAccountDetails)
                }
            } finally {
                database.close()
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun buildDatabase(context: Context): BurstWidgetsDatabase {
        return Room.databaseBuilder(context.applicationContext, BurstWidgetsDatabase::class.java, context.getString(R.string.db_name)).build()
    }
}