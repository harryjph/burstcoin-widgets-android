package com.harrysoft.burstinfowidget.repository;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.harrysoft.burstinfowidget.R;

public class SharedPreferenceConfigRepository implements PreferenceConfigRepository {

    private final Context context;

    public SharedPreferenceConfigRepository(Context context) {
        this.context = context;
    }

    @Override
    public void setSelectedCurrency(@NonNull String currencyCode) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.currency_key), currencyCode).apply();
    }

    @NonNull
    @Override
    public String getSelectedCurrency() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.currency_key), context.getString(R.string.currency_default));
    }

    @Override
    public void setNodeAddress(@NonNull String nodeAddress) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.node_address_key), nodeAddress).apply();

    }

    @NonNull
    @Override
    public String getNodeAddress() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.node_address_key), context.getString(R.string.node_address_default));
    }

    @Override
    public int getUpdateInterval() {
        try {
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.update_interval_key), context.getString(R.string.update_interval_default_value)));
        } catch (NumberFormatException e) {
            return Integer.parseInt(context.getString(R.string.update_interval_default_value));
        }
    }

    @Override
    public void setUpdateInterval(int updateInterval) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.update_interval_key), String.valueOf(updateInterval)).apply();
    }
}
