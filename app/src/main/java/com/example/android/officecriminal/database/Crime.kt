package com.example.android.officecriminal.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/* Определение сущности для класса модели Crime */
@Entity
data class Crime(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var callPolice: Boolean = false,
                 var isSolved: Boolean = false,
                 var suspect: String = "",
                 var phone: String = "") {
    val photoFileName
        get() = "IMG_$id.jpg"
}