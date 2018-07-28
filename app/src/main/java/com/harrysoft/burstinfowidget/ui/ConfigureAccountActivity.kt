package com.harrysoft.burstinfowidget.ui

import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.harry1453.burst.BurstUtils
import com.harry1453.burst.explorer.entity.BurstAddress
import com.harrysoft.burstinfowidget.R
import com.harrysoft.burstinfowidget.db.DatabaseUtils
import com.harrysoft.burstinfowidget.db.SavedAccountDetails
import com.harrysoft.burstinfowidget.widget.BurstAddressWidget

import io.reactivex.disposables.CompositeDisposable

class ConfigureAccountActivity : Activity() {

    private var appWidgetId: Int = 0
    private val compositeDisposable = CompositeDisposable()
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

        if (appWidgetId == -1) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        dialog = EnterAddressDialog(this)

        dialog!!.show()
    }

    private fun onAddressEntered(textBox: TextView) {
        try {
            val burstAddress = BurstAddress(textBox.text.toString())
            val savedAccountDetails = SavedAccountDetails()

            savedAccountDetails.appWidgetId = appWidgetId
            savedAccountDetails.address = burstAddress

            compositeDisposable.add(DatabaseUtils.insertSavedAccountDetails(this, savedAccountDetails)
                    .subscribe({ this.onSaved() }, { Toast.makeText(this, R.string.error_saving, Toast.LENGTH_LONG).show() }))
        } catch (e: BurstUtils.ReedSolomon.DecodeException) {
            textBox.error = getString(R.string.error_burst_rs_invalid)
        }

    }

    private fun onSaved() {
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        try {
            BurstAddressWidget.getUpdatePendingIntent(this, appWidgetId).send(appWidgetId)
            if (dialog != null) dialog!!.dismiss()
            finish()
        } catch (ignored: PendingIntent.CanceledException) { }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private inner class EnterAddressDialog internal constructor(context: Context) : Dialog(context) {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContentView(R.layout.enter_account_address)

            findViewById<Button>(R.id.account_address_save).setOnClickListener({ onAddressEntered(findViewById(R.id.account_address)) })
        }

        override fun dismiss() {
            super.dismiss()
            finish()
        }
    }
}
