package com.harrysoft.burstinfowidget.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import com.harry1453.burst.explorer.entity.BurstAddress
import com.harry1453.burst.explorer.entity.BurstValue

@Entity
class SavedAccountDetails {
    @PrimaryKey
    var appWidgetId: Int = 0

    @ColumnInfo(name = "address")
    var address: BurstAddress? = null

    @ColumnInfo(name = "balance")
    var balance: BurstValue? = null

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "rewardRecipientName")
    var rewardRecipientName: String? = null
}
