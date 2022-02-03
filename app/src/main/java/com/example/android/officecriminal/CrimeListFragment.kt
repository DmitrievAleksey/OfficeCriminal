package com.example.android.officecriminal

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.officecriminal.database.Crime
import com.example.android.officecriminal.databinding.CrimeListFragmentBinding
import com.example.android.officecriminal.databinding.ListItemCrimeCommonBinding
import com.example.android.officecriminal.databinding.ListItemCrimePoliceBinding
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment: Fragment() {

    /* Интерфейс обратного вызова для передачи функциональности обратно хостингу (MainActivity).
    * Позволяет передавать события кликов из CrimeListFragment обратно на хост-activity */
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private val viewTypeCommon = 0
    private val viewTypePolice = 1
    private var callbacks: Callbacks? = null
    private lateinit var binding: CrimeListFragmentBinding
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var textEmptyList: TextView
    private lateinit var addCrimeButton: Button
    private lateinit var photoFile: File
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())
    private val picasso: Picasso = Picasso.get()

    private val officeCrimeViewModel: OfficeCriminalViewModel by lazy {
        ViewModelProvider(this).get(OfficeCriminalViewModel::class.java)
    }


    /* Функция жизненного цикла onAttach(Context) вызывается, когда фрагмент прикреплется к
    * activity. Аргумент Context, являющийся экземпляром activity, в которой размещен фрагмент,
    * присваивается свойству callback */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CrimeListFragmentBinding.inflate(inflater, container, false)

        /* Класс RecyclerView будет жить в файле макета CrimeListFragment fragment_crime_list*/
        crimeRecyclerView = binding.crimeRecyclerView
        textEmptyList = binding.textEmptyList
        addCrimeButton = binding.addCrime

        /* При создании RecyclerView ему назначается объект LayoutManager. RecyclerView не
        * отображает элементы на самом экране, управление передается объекту LayoutManager */
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        /* Инициализация адаптера recycler view пустым списком преступлений, так как фрагмент
        * будет ждать результатов из базы данных, прежде чем он сможет его заполнить */
        crimeRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* Функция LiveData.observe(LifecycleOwner, Observer) используется для регистрации
        * наблюдателя за экземпляром LiveData и связи наблюдения с жизненным циклом фрагмента.
        * Блок наблюдателя выполняется всякий раз, когда обновляется список в LiveData.
        * Время жизни наблюдателя длится столько же, сколько и у компонента, представленного
        * LifecycleOwner (фрагмент) */
        officeCrimeViewModel.crimeListLiveData.observe(viewLifecycleOwner,
            /* объект Observer реализующий новые данные из LiveData */
            { crimes -> crimes.let {
                Log.i(TAG, "Got crimes ${crimes.size}")
                updateUI(crimes) }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        addCrimeButton.setOnClickListener {
            val crime = Crime()
            officeCrimeViewModel.addCrime(crime)
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    /* Для дальнейшего исключения доступа к activity переменная callbacks устанавливается в null */
    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.crime_list_fragment, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                officeCrimeViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Каждый элемент представления в RecyclerView будет обернут в экземпляр ViewHolder.
    * В конструкторе CommonCrimeHolder представление для закрепления передаеется в качестве
    * аргумента в конструктор классов RecyclerView.ViewHolder */
    private inner class CommonCrimeHolder(binding: ListItemCrimeCommonBinding):
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var crime: Crime
        private val photoView: ImageView = binding.crimePhotoItem
        private val titleTextView: TextView = binding.crimeTitle
        private val dateTextView: TextView = binding.crimeDate
        private val solvedImageView: ImageView = binding.crimeSolved

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime

            photoFile = officeCrimeViewModel.getPhotoFile(crime)
            if (photoFile.exists()) {
                picasso.load(photoFile).into(photoView)
            }

            titleTextView.text = crime.title
            dateTextView.text = crime.date.toString()
            if (crime.isSolved) {
                solvedImageView.visibility = View.VISIBLE
            } else {
                solvedImageView.visibility = View.INVISIBLE
            }
        }

        /* Уведомление хост-activity через интерфейс Callbacks при нажатии*/
        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class PoliceCrimeHolder(binding: ListItemCrimePoliceBinding):
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var crime: Crime
        private val photoView: ImageView = binding.crimePhotoItem
        private val titleTextView: TextView = binding.crimeTitle
        private val dateTextView: TextView = binding.crimeDate
        private val solvedImageView: ImageView = binding.crimeSolved
        private val policeButton: ImageButton = binding.policeButton

        init {
            itemView.setOnClickListener(this)
            policeButton.setOnClickListener {
                Toast.makeText(context, "Полиция! Срочный вызов!", Toast.LENGTH_SHORT).show()
            }
        }

        fun bind(crime: Crime) {
            this.crime = crime

            photoFile = officeCrimeViewModel.getPhotoFile(crime)
            if (photoFile.exists()) {
                picasso.load(photoFile).into(photoView)
            }

            titleTextView.text = crime.title
            dateTextView.text = crime.date.toString()
            if (crime.isSolved) {
                solvedImageView.visibility = View.VISIBLE
            } else {
                solvedImageView.visibility = View.GONE
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    /* Адаптер создает ViewHolder по запросу и связывает ViewHolder с данными из модельного слоя.
    * Адаптер будет использовать два типа холдера представления данных: PoliceCrimeHolder и
    * CommonCrimeHolder */
    private inner class CrimeAdapter(var crimes: List<Crime>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        /* Создание представления на экране, оборачивание его в холдер и возврат результата. */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return if (viewType == viewTypePolice) {
                PoliceCrimeHolder(ListItemCrimePoliceBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            } else {
                CommonCrimeHolder(ListItemCrimeCommonBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            }
        }
        /* Заполнение данного холдера holder данными из позиции position списка crimes */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val crime = crimes[position]
            if (getItemViewType(position) == viewTypePolice) {
                val policeHolder: PoliceCrimeHolder = holder as PoliceCrimeHolder
                policeHolder.bind(crime)
            } else {
                val commonHolder: CommonCrimeHolder = holder as CommonCrimeHolder
                commonHolder.bind(crime)
            }
        }

        /* Функция возвращает количество элементов в списке crimes */
        override fun getItemCount(): Int {
            return crimes.size
        }

        override fun getItemViewType(position: Int): Int {
            return if (crimes[position].callPolice) viewTypePolice
            else viewTypeCommon
        }
    }

    private fun updateUI(crimes: List<Crime>) {
        if (crimes.isNotEmpty()) {
            textEmptyList.visibility = View.INVISIBLE
            addCrimeButton.visibility = View.INVISIBLE
            crimeRecyclerView.visibility = View.VISIBLE

            adapter = CrimeAdapter(crimes)
            crimeRecyclerView.adapter = adapter
        } else {
            crimeRecyclerView.visibility = View.INVISIBLE
            textEmptyList.visibility = View.VISIBLE
            addCrimeButton.visibility = View.VISIBLE
        }
    }
}