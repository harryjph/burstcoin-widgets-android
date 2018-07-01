package com.harrysoft.burstinfowidget.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.harrysoft.burstinfowidget.R;
import com.harrysoft.burstinfowidget.repository.PreferenceConfigRepository;
import com.harrysoft.burstinfowidget.repository.SharedPreferenceConfigRepository;
import com.harrysoft.burstinfowidget.util.CurrencyUtils;
import com.harrysoft.burstinfowidget.util.VersionUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    PreferenceConfigRepository configRepository;

    @Override
    public void onAttach(Context context) {
        configRepository = new SharedPreferenceConfigRepository(context);
        super.onAttach(context);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getContext() == null) {
            return;
        }

        String versionString = VersionUtils.getVersionName(getContext());

        final EditTextPreference updateIntervalPreference = (EditTextPreference) findPreference(getString(R.string.update_interval_key));
        updateIntervalPreference.setText(String.valueOf(configRepository.getUpdateInterval()));
        updateIntervalPreference.setSummary(String.valueOf(configRepository.getUpdateInterval()));
        updateIntervalPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof String) {
                try {
                    int newInterval = Integer.parseInt((String) newValue);
                    if (newInterval < 60) {
                        Toast.makeText(getContext(), R.string.update_interval_error_less_than_60, Toast.LENGTH_LONG).show();
                    } else {
                        configRepository.setUpdateInterval(newInterval);
                        updateIntervalPreference.setText(String.valueOf(newInterval));
                        updateIntervalPreference.setSummary(String.valueOf(newInterval));
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), R.string.update_interval_error_invalid, Toast.LENGTH_LONG).show();
                }
            }
            return false;
        });

        final ListPreference currencyPreference = (ListPreference) findPreference(getString(R.string.currency_key));
        CurrencyUtils.setupCurrencyPreferenceData(getContext(), configRepository, currencyPreference);
        currencyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof String) {
                CurrencyUtils.setupCurrencyPreferenceData(getContext(), configRepository, currencyPreference, (String) newValue);
            }
            return false;
        });

        final EditTextPreference nodeAddressPreference = (EditTextPreference) findPreference(getString(R.string.node_address_key));
        nodeAddressPreference.setText(configRepository.getNodeAddress());
        nodeAddressPreference.setSummary(configRepository.getNodeAddress());
        nodeAddressPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof String) {
                configRepository.setNodeAddress((String) newValue);
                nodeAddressPreference.setText((String) newValue);
                nodeAddressPreference.setSummary((String) newValue);
            }
            return false;
        });

        final Preference resetNodeAddressPreference = findPreference(getString(R.string.reset_node_address));
        resetNodeAddressPreference.setOnPreferenceClickListener(preference -> {
            String defaultNodeAddress = getString(R.string.node_address_default);
            configRepository.setNodeAddress(defaultNodeAddress);
            nodeAddressPreference.setText(defaultNodeAddress);
            nodeAddressPreference.setSummary(defaultNodeAddress);
            return false;
        });

        final Preference burstWallet = findPreference(getString(R.string.burst_wallet));
        burstWallet.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.mobile_wallet_link))));
            return false;
        });

        final Preference burstWebsite = findPreference(getString(R.string.burst_website));
        burstWebsite.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_website_link))));
            return false;
        });

        final Preference burstWiki = findPreference(getString(R.string.burst_wiki));
        burstWiki.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_wiki_link))));
            return false;
        });

        final Preference burstTelegram = findPreference(getString(R.string.burst_telegram));
        burstTelegram.setOnPreferenceClickListener(preference -> {
            Intent intent;
            try {
                getContext().getPackageManager().getPackageInfo(getString(R.string.telegram_package_name), 0);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_telegram_direct_link)));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } catch (PackageManager.NameNotFoundException e) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_telegram_web_link)));
            }
            startActivity(intent);
            return false;
        });

        final Preference cryptoguruDiscord = findPreference(getString(R.string.cryptoguru_discord));
        cryptoguruDiscord.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.cryptoguru_discord_link))));
            return false;
        });

        final Preference burstPoCCExplorer = findPreference(getString(R.string.burst_pocc_explorer));
        burstPoCCExplorer.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.burst_explorer_link))));
            return false;
        });

        final Preference appGithub = findPreference(getString(R.string.app_github));
        appGithub.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))));
            return false;
        });

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

        final Preference appVersion = findPreference(getString(R.string.app_version));
        appVersion.setSummary(versionString);
    }
}
