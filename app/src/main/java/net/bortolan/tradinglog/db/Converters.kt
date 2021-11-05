package net.bortolan.tradinglog.db

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.*

class Converters {
    @TypeConverter
    fun fromDate(date: Date) = date.time

    @TypeConverter
    fun fromLong(millis: Long) = Date(millis)

    @TypeConverter
    fun fromBigDecimal(bigDecimal: BigDecimal) = bigDecimal.toEngineeringString()

    @TypeConverter
    fun fromEngineeringString(string: String) = string.toBigDecimal()
}