package com.myappproj.healthapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * Fragment untuk mengunggah resep dan detailnya, termasuk gambar.
 */
class UpResepFragment1 : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private var filePath: Uri? = null
    private lateinit var storageReference: FirebaseStorage
    private lateinit var databaseReference: FirebaseDatabase
    private lateinit var imageView: ImageView
    private lateinit var uploadDescTextView: TextView
    private lateinit var isimenu: TextInputLayout
    private lateinit var isikalori: TextInputLayout
    private lateinit var isipenyakit: TextInputLayout
    private lateinit var spinner1: Spinner

    /**
     * Metode untuk membuat tampilan fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_up_resep1, container, false)
        val textUpresep = view.findViewById<ImageView>(R.id.back_arrow)

        textUpresep.setOnClickListener {
            // Navigasi ke HomeFragment2 saat tombol diklik
            findNavController().navigate(R.id.action_upResepFragment1_to_resepFragment)
        }
        return view

    }


    /**
     * Metode yang dipanggil setelah tampilan dibuat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase Storage dan Realtime Database
        storageReference = FirebaseStorage.getInstance()
        databaseReference = FirebaseDatabase.getInstance()

        // Inisialisasi views
        imageView = view.findViewById(R.id.pick_image)
        uploadDescTextView = view.findViewById(R.id.upload_desc)
        isimenu = view.findViewById(R.id.isimenu)
        isikalori = view.findViewById(R.id.isikalori)
        isipenyakit = view.findViewById(R.id.isipenyakit)
        spinner1 = view.findViewById(R.id.spinner1)

        // Inisialisasi Spinner
        val spinner1 = view.findViewById<Spinner>(R.id.spinner1)
        val spinnerData = resources.getStringArray(R.array.spinner_data)
        val adapter = ArrayAdapter(requireContext(), R.layout.bg_spinner, spinnerData)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner1.adapter = adapter

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Lakukan sesuatu dengan item yang dipilih
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle jika tidak ada item yang dipilih
            }
        }

        // Atur listener untuk ImageView (pick_image)
        imageView.setOnClickListener {
            pickImageFromGallery()
        }

        // Atur listener untuk Button btn_next
        val buttonNext: Button = view.findViewById(R.id.btn_next)
        buttonNext.setOnClickListener {
            if (validateInputs()) {
                // Proceed with navigation only if validation passes
                val menuName = isimenu.editText?.text.toString()
                val calorieContent = isikalori.editText?.text.toString()
                val diseases = isipenyakit.editText?.text.toString()
                val menuType = spinner1.selectedItem.toString()

                val bundle = Bundle().apply {
                    putParcelable("filePath", filePath)
                    putString("menuName", menuName)
                    putString("calorieContent", calorieContent)
                    putString("diseases", diseases)
                    putString("menuType", menuType)
                }
                findNavController().navigate(R.id.action_upResepFragment1_to_upResepFragment2, bundle)
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Validate image separately
        if (filePath == null) {
            showToast("Silakan pilih gambar terlebih dahulu")
            return false
        }

        // Validate all other fields together
        if (isimenu.editText?.text.toString().trim().isEmpty() ||
            isikalori.editText?.text.toString().trim().isEmpty() ||
            isipenyakit.editText?.text.toString().trim().isEmpty()
        ) {
            showToast("Pastikan semua data terisi")
            return false
        }

        return true
    }

    /**
     * Metode untuk memilih gambar dari galeri.
     */
    private fun pickImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    /**
     * Metode yang dipanggil setelah pemilihan gambar dari galeri.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            // Mengambil nama file dari URI
            val fileName = getFileName(filePath)
            // Menampilkan nama file di TextView
            uploadDescTextView.text = fileName
        }
    }

    /**
     * Metode untuk mendapatkan nama file dari URI.
     */
    private fun getFileName(uri: Uri?): String {
        var result: String? = null
        if (uri?.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow("_display_name"))
                }
            }
        }
        if (result == null) {
            result = uri?.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: ""
    }

    /**
     * Metode untuk menampilkan pesan Toast.
     */
    private fun showToast(message: String) {
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(requireContext(), message, duration)
        toast.show()
    }
}
