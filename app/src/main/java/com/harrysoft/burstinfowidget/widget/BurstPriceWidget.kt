package com.harrysoft.burstinfowidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews

import com.harry1453.burst.explorer.entity.BurstPrice
import com.harry1453.burst.explorer.service.BurstPriceService
import com.harry1453.burst.explorer.service.BurstServiceProviders
import com.harrysoft.burstinfowidget.R
import com.harrysoft.burstinfowidget.repository.PreferenceConfigRepository
import com.harrysoft.burstinfowidget.service.AndroidNetworkService
import com.harrysoft.burstinfowidget.repository.SharedPreferenceConfigRepository
import com.harrysoft.burstinfowidget.util.CurrencyUtils

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class BurstPriceWidget : AppWidgetProvider() {

    private val compositeDisposable = CompositeDisposable()

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, priceService: BurstPriceService, configRepository: PreferenceConfigRepository) {
        val views = RemoteViews(context.packageName, R.layout.burst_widget)

        views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_price_title_refreshing))
        views.setOnClickPendingIntent(R.id.burst_logo_view, getUpdatePendingIntent(context, appWidgetId))

        appWidgetManager.updateAppWidget(appWidgetId, views)

        fun onFiatPrice(fiatPrice: BurstPrice) {
            views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_price_title))
            views.setTextViewText(R.id.burst_widget_text1, context.getString(R.string.burst_price_price, fiatPrice.currencyCode, CurrencyUtils.formatCurrencyAmount(fiatPrice.currencyCode, fiatPrice.price, true)))
            views.setTextViewText(R.id.burst_widget_text3, context.getString(R.string.burst_price_market_capital, fiatPrice.currencyCode, CurrencyUtils.formatCurrencyAmount(fiatPrice.currencyCode, fiatPrice.marketCapital, false)))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun onBitcoinPrice(bitcoinPrice: BurstPrice) {
            views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_price_title))
            views.setTextViewText(R.id.burst_widget_text2, context.getString(R.string.burst_price_price, bitcoinPrice.currencyCode, CurrencyUtils.formatCurrencyAmount(context.getString(R.string.bitcoin_code), bitcoinPrice.price, true)))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun onLoadError() {
            views.setTextViewText(R.id.burst_widget_title, context.getString(R.string.burst_price_title_refresh_error))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        compositeDisposable.add(priceService.fetchPrice(configRepository.selectedCurrency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ fiatPrice -> onFiatPrice(fiatPrice) }, { onLoadError() }))

        compositeDisposable.add(priceService.fetchPrice(context.getString(R.string.bitcoin_code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bitcoinPrice -> onBitcoinPrice(bitcoinPrice) }, { onLoadError() }))


        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        manager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + configRepository.updateInterval * 1000, getUpdatePendingIntent(context, appWidgetId))
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val configRepository = SharedPreferenceConfigRepository(context)
        val burstPriceService = BurstServiceProviders.getBurstServiceProvider(BurstServiceProviders.getObjectService(AndroidNetworkService(context)), configRepository).burstPriceService
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, burstPriceService, configRepository)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        compositeDisposable.dispose()
    }

    companion object {
        fun getUpdatePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
            return PendingIntent.getBroadcast(context, appWidgetId, Intent(context, BurstPriceWidget::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId)), PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
