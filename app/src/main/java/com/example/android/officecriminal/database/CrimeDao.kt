package com.example.android.officecriminal.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.*

/* Объект доступа к данным (интерфейс, содержащий функции для каждой операции с базой данных).
* Возвращаемый тип у функций запроса обернут в объект LiveData, запуская запрос в фоновом потоке */
@Dao
interface CrimeDao {

    /* запрос на вывод всех записей БД */
    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    /* запрос на вывод записи БД, соответсвующей ID */
    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    /* обновление существующих данных */
    @Update
    fun updateCrime(crime: Crime)

    /* добавление новых данных */
    @Insert
    fun addCrime(crime: Crime)
}