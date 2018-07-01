package com.harrysoft.burstinfowidget.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.harry1453.burst.explorer.service.BurstPriceService;
import com.harry1453.burst.explorer.service.BurstServiceProviders;
import com.harrysoft.burstinfowidget.R;
import com.harrysoft.burstinfowidget.repository.PreferenceConfigRepository;
import com.harrysoft.burstinfowidget.service.AndroidNetworkService;
import com.harrysoft.burstinfowidget.repository.SharedPreferenceConfigRepository;
import com.harrysoft.burstinfowidget.util.CurrencyUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class BurstPriceWidget extends AppWidgetProvider {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, BurstPriceService priceService, PreferenceConfigRepository configRepository) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.burst_price_widget);
        views.setTextViewText(R.id.burst_price_price_fiat, context.getString(R.string.burst_price_price, configRepository.getSelectedCurrency(), context.getString(R.string.loading)));
        views.setTextViewText(R.id.burst_price_market_capital, context.getString(R.string.burst_price_market_capital, configRepository.getSelectedCurrency(), context.getString(R.string.loading)));
        views.setTextViewText(R.id.burst_price_price_bitcoin, context.getString(R.string.burst_price_price, context.getString(R.string.bitcoin_code), context.getString(R.string.loading)));

        views.setOnClickPendingIntent(R.id.burst_logo_view, getUpdatePendingIntent(context, appWidgetId));

        appWidgetManager.updateAppWidget(appWidgetId, views);

        String fiatCurrencyCode = configRepository.getSelectedCurrency();

        compositeDisposable.add(priceService.fetchPrice(fiatCurrencyCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fiatPrice -> {
                    views.setTextViewText(R.id.burst_price_price_fiat, context.getString(R.string.burst_price_price, fiatCurrencyCode, CurrencyUtils.formatCurrencyAmount(fiatCurrencyCode, fiatPrice.price, true)));
                    views.setTextViewText(R.id.burst_price_market_capital, context.getString(R.string.burst_price_market_capital, fiatCurrencyCode, CurrencyUtils.formatCurrencyAmount(fiatCurrencyCode, fiatPrice.marketCapital, false)));
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }, error -> {
                    views.setTextViewText(R.id.burst_price_price_fiat, context.getString(R.string.burst_price_price, fiatCurrencyCode, context.getString(R.string.error_loading)));
                    views.setTextViewText(R.id.burst_price_market_capital, context.getString(R.string.burst_price_market_capital, fiatCurrencyCode, context.getString(R.string.error_loading)));
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }));

        compositeDisposable.add(priceService.fetchPrice(context.getString(R.string.bitcoin_code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitcoinPrice -> {
                    views.setTextViewText(R.id.burst_price_price_bitcoin, context.getString(R.string.burst_price_price, context.getString(R.string.bitcoin_code), CurrencyUtils.formatCurrencyAmount(context.getString(R.string.bitcoin_code), bitcoinPrice.price, true)));
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }, error -> {
                    views.setTextViewText(R.id.burst_price_price_bitcoin, context.getString(R.string.burst_price_price, context.getString(R.string.bitcoin_code), context.getString(R.string.error_loading)));
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }));


        final AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (manager != null) {
            manager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (configRepository.getUpdateInterval() * 1000), getUpdatePendingIntent(context, appWidgetId));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PreferenceConfigRepository configRepository = new SharedPreferenceConfigRepository(context);
        BurstPriceService burstPriceService = BurstServiceProviders.getBurstServiceProvider(BurstServiceProviders.getObjectService(new AndroidNetworkService(context)), configRepository).getBurstPriceService();
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, burstPriceService, configRepository);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        compositeDisposable.dispose();
    }

    private PendingIntent getUpdatePendingIntent(Context context, int appWidgetId) {
        return PendingIntent.getBroadcast(context, appWidgetId, new Intent(context, BurstPriceWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId}), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
