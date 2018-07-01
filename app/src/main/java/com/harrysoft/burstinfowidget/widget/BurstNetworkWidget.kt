package com.harrysoft.burstinfowidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.harry1453.burst.explorer.entity.Block
import com.harry1453.burst.explorer.entity.NetworkStatus

import com.harry1453.burst.explorer.service.BurstBlockchainService
import com.harry1453.burst.explorer.service.BurstNetworkService
import com.harry1453.burst.explorer.service.BurstServiceProviders
import com.harrysoft.burstinfowidget.R
import com.harrysoft.burstinfowidget.repository.PreferenceConfigRepository
import com.harrysoft.burstinfowidget.service.AndroidNetworkService
import com.harrysoft.burstinfowidget.repository.SharedPreferenceConfigRepository
import io.reactivex.android.schedulers.AndroidSchedulers

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat

class BurstNetworkWidget : AppWidgetProvider() {

    private val compositeDisposable = CompositeDisposable()

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, blockchainService: BurstBlockchainService, networkService: BurstNetworkService, configRepository: PreferenceConfigRepository) {
        val views = RemoteViews(context.packageName, R.layout.burst_network_widget)

        views.setTextViewText(R.id.burst_network_title, context.getString(R.string.burst_network_title_refreshing))
        views.setOnClickPendingIntent(R.id.burst_logo_view, getUpdatePendingIntent(context, appWidgetId))

        appWidgetManager.updateAppWidget(appWidgetId, views)

        fun onNetworkStatus(networkStatus: NetworkStatus) {
            views.setTextViewText(R.id.burst_network_title, context.getString(R.string.burst_network_title))
            views.setTextViewText(R.id.burst_network_public_node_count, context.getString(R.string.burst_network_public_node_count, networkStatus.peersData.peersStatus.total().toString()))
            views.setTextViewText(R.id.burst_network_public_node_stats, context.getString(R.string.burst_network_public_node_stats, DecimalFormat("##.###").format(networkStatus.peersData.peersStatus.valid.toDouble() / networkStatus.peersData.peersStatus.total().toDouble() * 100)))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun onRecentBlocks(blocks: List<Block>) {
            views.setTextViewText(R.id.burst_network_title, context.getString(R.string.burst_network_title))
            views.setTextViewText(R.id.burst_network_block_height, context.getString(R.string.burst_network_block_height, blocks[0].blockNumber.toString()))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun onLoadError() {
            views.setTextViewText(R.id.burst_price_title, context.getString(R.string.burst_network_title_refresh_error))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        compositeDisposable.add(networkService.networkStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ networkStatus -> onNetworkStatus(networkStatus) }, { onLoadError() }))

        compositeDisposable.add(blockchainService.fetchRecentBlocks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ recentBlocks -> onRecentBlocks(recentBlocks) }, { onLoadError() }))

        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        manager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + configRepository.updateInterval * 1000, getUpdatePendingIntent(context, appWidgetId))
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val configRepository = SharedPreferenceConfigRepository(context)
        val burstNetworkService = BurstServiceProviders.getBurstServiceProvider(BurstServiceProviders.getObjectService(AndroidNetworkService(context)), configRepository).burstNetworkService
        val burstBlockchainService = BurstServiceProviders.getBurstServiceProvider(BurstServiceProviders.getObjectService(AndroidNetworkService(context)), configRepository).burstBlockchainService

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, burstBlockchainService, burstNetworkService, configRepository)
        }
    }

    override fun onEnabled(context: Context) {

    }

    override fun onDisabled(context: Context) {
        compositeDisposable.dispose()
    }

    private fun getUpdatePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        return PendingIntent.getBroadcast(context, appWidgetId, Intent(context, BurstNetworkWidget::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId)), PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
