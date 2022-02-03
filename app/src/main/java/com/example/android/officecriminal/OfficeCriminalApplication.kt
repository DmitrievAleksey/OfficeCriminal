package com.example.android.officecriminal

import android.app.Application
import com.example.android.officecriminal.database.CrimeRepository

/* CriminalIntentApplication расширяет Application и переопределяет функцию Application.onCreate()
* для инициализации репозитория, чтобы выполнить работу, как только приложение будет готово.
* Экземпляр приложения не будет постоянно уничтожаться и создаваться вновь, в отличие от activity.
* Он создается, когда приложение запускается, и уничтожается при завершении приложения */

class OfficeCriminalApplication : Application() {
    /* Переопределение ф-и onCreate, передача экземпляра приложения в репозиторий в качестве
    * объекта Context */
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}