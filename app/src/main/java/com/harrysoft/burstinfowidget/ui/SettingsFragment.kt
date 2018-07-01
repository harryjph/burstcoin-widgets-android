package com.harrysoft.burstinfowidget.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.Toast

import com.harrysoft.burstinfowidget.R
import com.harrysoft.burstinfowidget.repository.PreferenceConfigRepository
import com.harrysoft.burstinfowidget.repository.SharedPreferenceConfigRepository
import com.harrysoft.burstinfowidget.util.CurrencyUtils
import com.harrysoft.burstinfowidget.util.VersionUtils

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var configRepository: PreferenceConfigRepository

    override fun onAttach(context: Context) {
        configRepository = SharedPreferenceConfigRepository(context)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()

        if (context == null) {
            return
        }

        val versionString = VersionUtils.getVersionName(context!!)

        val updateIntervalPreference = findPreference(getString(R.string.update_interval_key)) as EditTextPreference
        updateIntervalPreference.text = configRepository.updateInterval.toString()
        updateIntervalPreference.summary = configRepository.updateInterval.toString()
        updateIntervalPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                try {
                    val newInterval = Integer.parseInt(newValue)
                    if (newInterval < 60) {
                        Toast.makeText(context, R.string.update_interval_error_less_than_60, Toast.LENGTH_LONG).show()
                    } else {
                        configRepository.updateInterval = newInterval
                        updateIntervalPreference.text = newInterval.toString()
                        updateIntervalPreference.summary = newInterval.toString()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, R.string.update_interval_error_invalid, Toast.LENGTH_LONG).show()
                }

            }
            false
        }

        val currencyPreference = findPreference(getString(R.string.currency_key)) as ListPreference
        CurrencyUtils.setupCurrencyPreferenceData(context!!, configRepository, currencyPreference)
        currencyPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                CurrencyUtils.setupCurrencyPreferenceData(context!!, configRepository, currencyPreference, newValue)
            }
            false
        }

        val nodeAddressPreference = findPreference(getString(R.string.node_address_key)) as EditTextPreference
        nodeAddressPreference.text = configRepository.nodeAddress
        nodeAddressPreference.summary = configRepository.nodeAddress
        nodeAddressPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                configRepository.nodeAddress = newValue
                nodeAddressPreference.text = newValue
                nodeAddressPreference.summary = newValue
            }
            false
        }

        val resetNodeAddressPreference = findPreference(getString(R.string.reset_node_address))
        resetNodeAddressPreference.setOnPreferenceClickListener {
            val defaultNodeAddress = getString(R.string.node_address_default)
            configRepository.nodeAddress = defaultNodeAddress
            nodeAddressPreference.text = defaultNodeAddress
            nodeAddressPreference.summary = defaultNodeAddress
            false
        }

        val burstWallet = findPreference(getString(R.string.burst_wallet))
        burstWallet.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.mobile_wallet_link))))
            false
        }

        val burstWebsite = findPreference(getString(R.string.burst_website))
        burstWebsite.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_website_link))))
            false
        }

        val burstWiki = findPreference(getString(R.string.burst_wiki))
        burstWiki.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_wiki_link))))
            false
        }

        val burstTelegram = findPreference(getString(R.string.burst_telegram))
        burstTelegram.setOnPreferenceClickListener {
            var intent: Intent
            try {
                context!!.packageManager.getPackageInfo(getString(R.string.telegram_package_name), 0)
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_telegram_direct_link)))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            } catch (e: PackageManager.NameNotFoundException) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_telegram_web_link)))
            }

            startActivity(intent)
            false
        }

        val cryptoguruDiscord = findPreference(getString(R.string.cryptoguru_discord))
        cryptoguruDiscord.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.cryptoguru_discord_link))))
            false
        }

        val burstPoCCExplorer = findPreference(getString(R.string.burst_pocc_explorer))
        burstPoCCExplorer.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_explorer_link))))
            false
        }

        val appGithub = findPreference(getString(R.string.app_github))
        appGithub.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))))
            false
        }

        /*final Preference appContributors = findPreference(getString(R.string.contributors));
           appContributors.setOnPreferenceClickListener(preference -> {
               startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.contributors_link))));
               return false;
           });*/

        /*final Preference appDonate = findPreference(getString(R.string.app_donate));
        appDonate.setOnPreferenceClickListener(preference -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.app_donate), getString(R.string.donate_address));
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), R.string.app_donate_copied, Toast.LENGTH_LONG).show();
            }
            return false;
        });*/

        val appVersion = findPreference(getString(R.string.app_version))
        appVersion.summary = versionString
    }
}
