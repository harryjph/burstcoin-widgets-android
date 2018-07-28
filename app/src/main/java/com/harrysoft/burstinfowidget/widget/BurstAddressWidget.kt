package com.harrysoft.burstinfowidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.harry1453.burst.explorer.entity.Account
import com.harry1453.burst.explorer.service.BurstBlockchainService
import com.harry1453.burst.explorer.service.BurstServiceProviders
import com.harrysoft.burstinfowidget.R
import com.harrysoft.burstinfowidget.db.BurstWidgetsDatabase
import com.harrysoft.burstinfowidget.db.DatabaseUtils
import com.harrysoft.burstinfowidget.db.SavedAccountDetails
import com.harrysoft.burstinfowidget.repository.PreferenceConfigRepository
import com.harrysoft.burstinfowidget.repository.SharedPreferenceConfigRepository
import com.harrysoft.burstinfowidget.service.AndroidNetworkService
import com.harrysoft.burstinfowidget.ui.ConfigureAccountActivity
import io.reactivex.disposables.CompositeDisposable

class BurstAddressWidget : AppWidgetProvider() {

    private val compositeDisposable = CompositeDisposable()

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, blockchainService: BurstBlockchainService, configRepository: PreferenceConfigRepository) {
        val views = RemoteViews(context.packageName, R.layout.burst_widget)

        views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_account_title_unloaded))
        views.setOnClickPendingIntent(R.id.burst_widget, getSetAddressPendingIntent(context, appWidgetId))
        views.setOnClickPendingIntent(R.id.burst_logo_view, getUpdatePendingIntent(context, appWidgetId))

        appWidgetManager.updateAppWidget(appWidgetId, views)

        fun onLoadError() {
            views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_account_title_refresh_error))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun onAccountNotSet() {
            views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_account_title_not_set))
            views.setTextViewText(R.id.burst_widget_text1, "")
            views.setTextViewText(R.id.burst_widget_text2, context.getString(R.string.burst_account_hint_not_set))
            views.setTextViewText(R.id.burst_widget_text3, "")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun onSavedAccount(account: SavedAccountDetails, refresh: Boolean) {

            fun onAccount(account: Account) {
                val savedAccount = SavedAccountDetails()
                savedAccount.appWidgetId = appWidgetId
                savedAccount.address = account.address
                savedAccount.name = account.name
                savedAccount.balance = account.balance
                savedAccount.rewardRecipientName = account.rewardRecipientName

                compositeDisposable.add(DatabaseUtils.updateSavedAccountDetails(context, savedAccount)
                        .subscribe({ onSavedAccount(savedAccount, false) }, { t -> t.printStackTrace(); onLoadError() }))
            }

            if (refresh) {
                views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_account_title_refreshing, account.address!!.getFullAddress()))
            } else {
                views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_account_title, account.address!!.getFullAddress()))
            }

            val name = context.getString(R.string.burst_account_name, if(account.name == null) context.getString(R.string.loading) else account.name)
            val balance = context.getString(R.string.burst_account_balance, if(account.balance == null) context.getString(R.string.loading) else account.balance)
            val transactionCount = context.getString(R.string.burst_account_reward_recipient, if(account.rewardRecipientName == null) context.getString(R.string.loading) else account.rewardRecipientName)

            views.setTextViewText(R.id.burst_widget_text1, name)
            views.setTextViewText(R.id.burst_widget_text2, balance)
            views.setTextViewText(R.id.burst_widget_text3, transactionCount)

            appWidgetManager.updateAppWidget(appWidgetId, views)

            if (refresh) {
                compositeDisposable.add(blockchainService.fetchAccount(account.address!!.getNumericID())
                        .subscribe({ fetchedAccount -> onAccount(fetchedAccount) }, { t -> t.printStackTrace(); onLoadError() }))
            }
        }

        compositeDisposable.add(DatabaseUtils.getSavedAccountDetails(context, appWidgetId)
                .subscribe({ recentBlocks -> onSavedAccount(recentBlocks, true) }, { t -> t.printStackTrace(); if (t.message == context.getString(R.string.burst_account_title_not_set)) onAccountNotSet() else onLoadError() }))

        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        manager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + configRepository.updateInterval * 1000, getUpdatePendingIntent(context, appWidgetId))
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val configRepository = SharedPreferenceConfigRepository(context)
        val burstBlockchainService = BurstServiceProviders.getBurstServiceProvider(BurstServiceProviders.getObjectService(AndroidNetworkService(context)), configRepository).burstBlockchainService

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, burstBlockchainService, configRepository)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        DatabaseUtils.deleteSavedAccountDetails(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        compositeDisposable.dispose()
    }

    companion object {
        fun getUpdatePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
            return PendingIntent.getBroadcast(context, appWidgetId, Intent(context, BurstAddressWidget::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId)), PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun getSetAddressPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
            return PendingIntent.getActivity(context, appWidgetId, Intent(context.applicationContext, ConfigureAccountActivity::class.java).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
