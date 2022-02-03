package com.example.android.officecriminal

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.officecriminal.database.Crime
import com.example.android.officecriminal.databinding.CrimeFragmentBinding
import com.example.android.officecriminal.picker.DatePickerFragment
import com.example.android.officecriminal.picker.ImagePickerFragment
import com.example.android.officecriminal.picker.TimePickerFragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DIALOG_FILE = "DialogFile"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val DATE_FORMAT = "EEE, MMM, dd"

/* CrimeFragment — контроллер, взаимодействующий с объектами модели и представления.
* Его задача — выдача подробной информации о конкретном преступлении и ее обновление
* при модификации пользователем.*/

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    companion object {
        /* Создание экземпляра фрагмента */
        fun newInstance(crimeId: UUID): CrimeFragment {
            /* Создание пакета аргументов */
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                /* Присоединение пакета аргументов к фрагменту */
                arguments = args
            }
        }
    }

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var callPoliceCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var phoneButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var binding: CrimeFragmentBinding
    private val picasso: Picasso = Picasso.get()
    private val officeCrimeViewModel: OfficeCriminalViewModel by lazy {
        ViewModelProvider(this).get(OfficeCriminalViewModel::class.java)
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {}

    /* Инициализация и настройка экземпляра фрагмента */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        officeCrimeViewModel.loadCrime(crimeId)
    }

    /* Создание и настройка представления фрагмента */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = CrimeFragmentBinding.inflate(inflater, container, false)

        titleField = binding.crimeTitle
        dateButton = binding.crimeDate
        timeButton = binding.crimeTime
        solvedCheckBox = binding.crimeSolved
        callPoliceCheckBox = binding.callPolice
        reportButton = binding.crimeReport
        suspectButton = binding.crimeSuspect
        phoneButton = binding.crimePhone
        photoButton = binding.crimeCamera
        photoView = binding.crimePhoto

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        officeCrimeViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = officeCrimeViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.example.android.officecriminal.fileprovider",
                        photoFile)
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        /* создаем анонимный класс, который реализует интерфейс слушателя TextWatcher */
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?, start: Int, count: Int, after: Int) {}
            /* вызываем toString() для объекта CharSequence, представляющего ввод пользователя.
            * Эта функция возвращает строку, которая затем присваивается заголовку Crime */
            override fun onTextChanged(
                sequence: CharSequence?, start: Int, before: Int, count: Int
            ) {
                crime.title = sequence.toString()
            }
            override fun afterTextChanged(sequence: Editable?) {}
        }

        titleField.addTextChangedListener(titleWatcher)
        /* назначение слушателя, обновляющего поле solvedCheckBox объекта Crime */
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        /* назначение слушателя, обновляющего поле callPoliceCheckBox объекта Crime */
        callPoliceCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.callPolice = isChecked
            }
        }

        /* вывод увеличенного фото в фрагменте изображения */
        photoView.setOnClickListener {
            ImagePickerFragment.newInstance(photoFile).apply {
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_FILE)
            }
        }

        /* календарь */
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        /* часы */
        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }

        /* Отправка отчета */
        reportButton.setOnClickListener {
            /* конструктор Intent  получает строку с командой действия, тип задается явно,
            * текст отчета и строка темы включаются в дополнения */
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_suspect))
            }.also { intent ->
                /* создание списка выбора для отображения activity, реагирующих на неявный интент */
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        /* Выбор подозреваемого */
        suspectButton.apply {
            /* конструктор Intent  получает строку с командой действия и
            * местонахождение соответствующих данных */
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

            /* предварительная проверка того, от какой части ОС поступил вызов PackageManager,
            * чтобы удостовериться в наличии подходящей activity */
            val packageManager: PackageManager = requireActivity().packageManager
            /* поиск activity, соответствующей переданному интенту */
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            /* если поиск вернул null, то приложения адресной книги нет и кнопка блокируется */
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                /* процесс проверки разрешения и запрос разрешения у пользователя */
                when {
                    /* 1. если приложению уже было предоставлено разрешение */
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED -> {
                        startActivityForResult(pickContactIntent, REQUEST_CONTACT)
                    }
                    /* 2. запрос разрешения с выводом пользователького интерфейса обоснования */
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                        showSnackBar(it, getString(R.string.permission_required),
                            Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {
                            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }
                    /* 3. запрос разрешения */
                    else -> requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }
        }

        /* Телефонный звонок подозреваемому */
        phoneButton.setOnClickListener {
            if (crime.phone.isEmpty()) {
                requireView().showSnackBar(it, getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {}
            } else {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:" + crime.phone)
                startActivity(dialIntent)
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            /* предварительная проверка в наличии подходящей activity */
            val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(photoIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(photoIntent,
                        PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                startActivityForResult(photoIntent, REQUEST_PHOTO)
            }
        }
    }

    /* Fragment.onStop() вызывается каждый раз, когда фрагмент переходит в состояние остановки.
    * Данные будут сохранены, когда закроется экран подробностей или при переключении задачи  */
    override fun onStop() {
        super.onStop()
        if (titleField.text.isNullOrEmpty()) {
            crime.title = "..."
        }
        officeCrimeViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        /* отзыв разрешения после неуспешной операции записи в файл */
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        /* смена названия заголовка на стандартное */
        requireActivity().title = getString(R.string.app_name)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        /* создание запроса всех отображаемых имен контактов в возвращенных данных */
        when {
            /* 1. если не выполнен ответ на произведеный запрос */
            resultCode != Activity.RESULT_OK -> return
            /* 2. если получен ответ на запрос контакта и возвращаемые данные не пустые */
            requestCode == REQUEST_CONTACT && data != null -> {
                /* ссылка на конкретный контакт, выбранный пользователем */
                val contactUri: Uri? = data.data
                /* указать, для каких полей запрос должен возвращать значение:
                * DISPLAY_NAME - имя контакта, NUMBER - номер контакта */
                val queryFields = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER)
                /* выполняемый запрос в базу данных контактов и получение объекта Cursor */
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                        .query(it, queryFields, null, null, null)
                }
                cursor?.use {
                    /* проверка, что возвращенный курсор содержит хотя бы одну строку */
                    if (it.count == 0) {
                        return
                    }
                    /* первый столбец первой строки данных - имя выбранного контакта,
                    * второй столбец - номер выбранного контакта */
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    val phone = it.getString(1)
                    crime.suspect = suspect
                    crime.phone = phone
                    /* сохранение полученной информации в базу данных */
                    officeCrimeViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
            /* 3. получен ответ на запрос камеры */
            requestCode == REQUEST_PHOTO -> {
                /* отзыв разрешения после успешного завершения записи в файл */
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(hour: Int, minute: Int) {
        crime.date.hours = hour
        crime.date.minutes = minute
        updateUI()
    }

    private fun updateUI() {
        if (crime.title.isEmpty()) {
            requireActivity().title = "Новое преступление"
        } else {
            requireActivity().title = crime.title
            titleField.setText(crime.title)
        }
        dateButton.text = DateFormat.format("EEE, MMM d, yyyy", crime.date)
        timeButton.text = DateFormat.format("h:mm a", crime.date)
        solvedCheckBox. apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        callPoliceCheckBox. apply {
            isChecked = crime.callPolice
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }

        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            picasso.load(photoUri).into(photoView)
        }
    }

    /* функця создает четыре строки, соединяет их и возвращает полный отчет */
    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }
}

fun View.showSnackBar(
    view: View, msg: String, length: Int, actionMessage: CharSequence?, action: (View) -> Unit
) {
    val snackBar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
        snackBar.setAction(actionMessage) {
            action(this)
        }.show()
    } else {
        snackBar.show()
    }
}