package com.harrysoft.burstinfowidget.db

import android.arch.persistence.room.TypeConverter

import com.harry1453.burst.explorer.entity.BurstAddress
import com.harry1453.burst.explorer.entity.BurstValue

import java.math.BigInteger

class TypeConverters {
    @TypeConverter
    fun burstValueToString(value: BurstValue?): String? {
        return value?.toUnformattedString()
    }

    @TypeConverter
    fun burstValueFromString(value: String?): BurstValue? {
        return if (value == null) null else BurstValue.fromBurst(value)
    }

    @TypeConverter
    fun burstAddressToString(address: BurstAddress?): String? {
        return address?.getNumericID()?.toString()
    }

    @TypeConverter
    fun burstAddressFromString(address: String?): BurstAddress? {
        return if (address == null) null else BurstAddress(BigInteger(address))
    }
}
