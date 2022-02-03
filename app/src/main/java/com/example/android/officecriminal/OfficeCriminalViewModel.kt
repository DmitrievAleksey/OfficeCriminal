package com.example.android.officecriminal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.android.officecriminal.database.Crime
import com.example.android.officecriminal.database.CrimeRepository
import java.io.File
import java.util.*

/* OfficeCriminalViewModel представляет собой централизованное хранилище для объектов Crime.
* Объект ViewModel инкапсулирует данные для нового экрана */

class OfficeCriminalViewModel: ViewModel() {

    /* В свойстве crimeRepository хранится связь с объектом БД */
    private val crimeRepository = CrimeRepository.get()

    /* crimeIdLiveData хранит текущий идентификатор UUID */
    private val crimeIdLiveData = MutableLiveData<UUID>()

    val crimeListLiveData = crimeRepository.getCrimes()

    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
    }

    /* Сохранения объекта преступления в базу данных */
    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }

    fun getPhotoFile(crime: Crime): File {
        return crimeRepository.getPhotoFile(crime)
    }
}