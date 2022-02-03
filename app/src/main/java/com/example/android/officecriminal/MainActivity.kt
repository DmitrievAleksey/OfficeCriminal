package com.example.android.officecriminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android.officecriminal.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /* Подключение интерфейса обратного вызова из CrimeListFragment для замены CrimeListFragment
    * на экземпляр CrimeFragment или вывод CrimeFragment в landscape режиме */
    override fun onCrimeSelected(crimeId: UUID) {
        /* замена фрагмента, размещенного в activity (в контейнере с указанным целочисленным
        * идентификатором ресурса), на новый фрагмент, добавление в очередь стека фрагментов */
        if (binding.detailFragmentContainer != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.detail_fragment_container, CrimeFragment.newInstance(crimeId))
                .addToBackStack(null)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CrimeFragment.newInstance(crimeId))
                .addToBackStack(null)
                .commit()
        }

    }
}