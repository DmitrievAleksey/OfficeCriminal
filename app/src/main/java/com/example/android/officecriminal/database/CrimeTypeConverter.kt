package com.example.android.officecriminal.database

import androidx.room.TypeConverter
import java.util.*

/* Преобразователь типов UUID и Date для хранения в БД и обратного преобразования */
class CrimeTypeConverter {

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return  Date(millisSinceEpoch)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String): UUID {
        return UUID.fromString(uuid)
    }
}