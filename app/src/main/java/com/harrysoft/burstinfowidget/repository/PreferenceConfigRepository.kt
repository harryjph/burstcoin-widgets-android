package com.harrysoft.burstinfowidget.repository

import com.harry1453.burst.explorer.repository.ConfigRepository

interface PreferenceConfigRepository : ConfigRepository {
    var updateInterval: Int
}
