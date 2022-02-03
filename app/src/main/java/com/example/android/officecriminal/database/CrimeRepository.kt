package com.example.android.officecriminal.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

/* Класс репозитория инкапсулирует логику для доступа к данным */
class CrimeRepository private constructor(context: Context) {

    /* Ссылка на БД, databaseBuilder() создает конкретную реализацию абстрактного класса
    * CrimeDatabase */
    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME).addMigrations(migration_1_2, migration_2_3).build()

    /* Ссылка на объект ДАО */
    private val crimeDao = database.crimeDao()

    /* Свойство исполнителя для хранения ссылки. Исполнитель — это объект ссылающийся на поток.
    * newSingleThreadExecutor() возвращает экземпляр исполнителя, который указывает на новый поток.
    * Таким образом, любая работа с исполнителем, будет происходить вне основного потока. */
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    /*  Ссылка на функцию ДАО getCrimes*/
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    /* Ссылка на функцию ДАО getCrime */

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        /* Код, который находится в блоке execute, будет выполняться в любом потоке, на который
        * ссылается исполнитель */
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        /* Код, который находится в блоке execute, будет выполняться в любом потоке, на который
        * ссылается исполнитель */
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File {
        return File(filesDir, crime.photoFileName)
    }

    companion object {
        private var INSTANCE: CrimeRepository? = null
        /* Инициализация репозитория при запуске приложения */
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }
        /* Вызов текущего объекта репозитория */
        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}