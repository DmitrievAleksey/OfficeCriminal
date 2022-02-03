package com.example.android.officecriminal.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.android.officecriminal.databinding.ImagePickerFragmentBinding
import com.squareup.picasso.Picasso
import java.io.File

private const val ARG_FILE = "file"

class ImagePickerFragment: DialogFragment() {

    private lateinit var binding: ImagePickerFragmentBinding
    private lateinit var file: File
    private lateinit var photoView: ImageView

    companion object {
        fun newInstance(photoFile: File): ImagePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_FILE, photoFile)
            }
            return ImagePickerFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ImagePickerFragmentBinding.inflate(inflater, container, false)
        photoView = binding.imageView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        file = arguments?.getSerializable(ARG_FILE) as File
        if (file.exists()) {
            val picasso = Picasso.get()
            picasso.load(file).into(photoView)
        }
    }
}