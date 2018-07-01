package com.harrysoft.burstinfowidget.repository

import android.content.Context
import android.preference.PreferenceManager

import com.harrysoft.burstinfowidget.R

class SharedPreferenceConfigRepository(private val context: Context) : PreferenceConfigRepository {

    override var selectedCurrency: String
        get() =
            PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.currency_key), context.getString(R.string.currency_default))
        set(currencyCode) =
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.currency_key), currencyCode).apply()

    override var nodeAddress: String
        get() =
            PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.node_address_key), context.getString(R.string.node_address_default))
        set(nodeAddress) =
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.node_address_key), nodeAddress).apply()

    override var updateInterval: Int
        get() {
            return try {
                Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.update_interval_key), context.getString(R.string.update_interval_default_value))!!)
            } catch (e: NumberFormatException) {
                Integer.parseInt(context.getString(R.string.update_interval_default_value))
            }

        }
        set(updateInterval) =
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.update_interval_key), updateInterval.toString()).apply()
}
