package com.myappproj.healthapp

import BahanInputView
import LangkahInputView
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

/**
 * Fragment untuk mengunggah resep bagian kedua dengan detail resep, bahan, langkah, dan gambar.
 */
class UpResepFragment2 : Fragment() {

    private lateinit var spinner1: Spinner
    private lateinit var isialat: TextInputLayout
    private lateinit var inputBahanRecyclerView: RecyclerView
    private lateinit var inputLangkahRecyclerView: RecyclerView
    private lateinit var btnUpload: Button
    private lateinit var btnAddBahan: TextView
    private lateinit var btnAddLangkah: TextView
    private lateinit var bahanAdapter: BahanInputView
    private lateinit var langkahAdapter: LangkahInputView
    private lateinit var auth: FirebaseAuth
    private val bahanList: MutableList<String> = mutableListOf()
    private val langkahList: MutableList<String> = mutableListOf()
    private var filePath: Uri? = null
    private var menuName: String = ""
    private var calorieContent: String = ""
    private var diseases: String = ""
    private var menuType: String = "Makanan"
    private lateinit var loadingOverlay: RelativeLayout
    private lateinit var progressBar: View

    /**
     * Metode untuk membuat tampilan fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_up_resep2, container, false)
        auth = Firebase.auth
        spinner1 = view.findViewById(R.id.spinner1)
        isialat = view.findViewById(R.id.isialat)
        inputBahanRecyclerView = view.findViewById(R.id.recyclerView)
        inputLangkahRecyclerView = view.findViewById(R.id.recyclerView2)
        btnUpload = view.findViewById(R.id.btn_next2)
        btnAddBahan = view.findViewById(R.id.btn_bahan)
        btnAddLangkah = view.findViewById(R.id.btn_step)
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        progressBar = view.findViewById(R.id.progress_bar)
        val textUpresep = view.findViewById<ImageView>(R.id.back_arrow2)

        // Inisialisasi RecyclerView dan Adapter untuk input bahan dan langkah
        initRecyclerViews()

        // Inisialisasi Spinner
        initSpinner()

        // entri default untuk bahan dan langkah
        bahanList.add("")
        langkahList.add("")

        // Set listener untuk tombol tambah bahan
        btnAddBahan.setOnClickListener {
            val newItem = ""
            bahanList.add(newItem)
            bahanAdapter.notifyItemInserted(bahanList.size - 1)
        }

        // Set listener untuk tombol tambah langkah
        btnAddLangkah.setOnClickListener {
            val newItem = ""
            langkahList.add(newItem)
            langkahAdapter.notifyItemInserted(langkahList.size - 1)
        }
        textUpresep.setOnClickListener {
            // Navigasi ke HomeFragment2 saat tombol diklik
            findNavController().navigate(R.id.action_upResepFragment2_to_upResepFragment1)
        }

        return view
    }

    /**
     * Metode yang dipanggil setelah tampilan dibuat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mendapatkan data dari Bundle jika ada
        arguments?.let {
            filePath = it.getParcelable("filePath")
            menuName = it.getString("menuName", "")
            calorieContent = it.getString("calorieContent", "")
            diseases = it.getString("diseases", "")
            menuType = it.getString("menuType", "Makanan")
        }

        btnUpload.setOnClickListener {
            if (validateInputs()) {
                uploadDataToFirebase()
            }
        }
    }

    /**
     * Metode untuk menginisialisasi RecyclerView untuk input bahan dan langkah.
     */
    private fun initRecyclerViews() {
        // Inisialisasi RecyclerView dan Adapter untuk input bahan
        bahanAdapter = BahanInputView(bahanList)
        inputBahanRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bahanAdapter
        }

        // Inisialisasi RecyclerView dan Adapter untuk input langkah
        langkahAdapter = LangkahInputView(langkahList)
        inputLangkahRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = langkahAdapter
        }
    }

    /**
     * Metode untuk menginisialisasi Spinner.
     */
    private fun initSpinner() {
        val spinnerData = resources.getStringArray(R.array.spinner_data)
        val adapter = ArrayAdapter(requireContext(), R.layout.bg_spinner, spinnerData)
        spinner1.adapter = adapter

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                menuType = spinnerData[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle jika tidak ada item yang dipilih
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Check all inputs together
        val alatKosong = isialat.editText?.text.toString().trim().isEmpty()
        val bahanKosong = checkEmptyBahan()
        val langkahKosong = checkEmptyLangkah()

        if (alatKosong || bahanKosong || langkahKosong) {
            showToast("Pastikan semua data terisi")
            return false
        }

        return true
    }

    private fun checkEmptyBahan(): Boolean {
        // Check if no ingredients or any ingredient is empty
        if (bahanList.isEmpty()) return true

        for (i in 0 until inputBahanRecyclerView.childCount) {
            val viewHolder = inputBahanRecyclerView.findViewHolderForAdapterPosition(i) as? BahanInputView.ViewHolder
            val inputBahan = viewHolder?.itemView?.findViewById<TextInputLayout>(R.id.input_bahan)?.editText?.text.toString().trim()
            if (inputBahan.isEmpty()) return true
        }
        return false
    }

    private fun checkEmptyLangkah(): Boolean {
        // Check if no steps or any step is empty
        if (langkahList.isEmpty()) return true

        for (i in 0 until inputLangkahRecyclerView.childCount) {
            val viewHolder = inputLangkahRecyclerView.findViewHolderForAdapterPosition(i) as? LangkahInputView.ViewHolder
            val inputLangkah = viewHolder?.itemView?.findViewById<TextInputLayout>(R.id.input_langkah)?.editText?.text.toString().trim()
            if (inputLangkah.isEmpty()) return true
        }
        return false
    }


    /**
     * Metode untuk mengunggah data resep ke Firebase Storage dan Realtime Database.
     */
    private fun uploadDataToFirebase() {
        val alat = isialat.editText?.text.toString()
        val bahan = getBahanListFromAdapter()
        val langkah = getLangkahListFromAdapter()
        val userId = auth.currentUser?.uid

        // Show loading overlay first
        loadingOverlay.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        if (filePath == null) {
            showToast("Silakan pilih gambar terlebih dahulu")
            hideLoading()
            return
        }

        // Upload to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        imageRef.putFile(filePath!!)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { imageUrl ->
                val database = FirebaseDatabase.getInstance()
                val userRef = database.getReference("users").child(userId!!)

                userRef.get().addOnSuccessListener { snapshot ->
                    val username = snapshot.child("username").getValue(String::class.java)
                        ?: auth.currentUser?.displayName
                        ?: "Pengguna Anonim"

                    val newMenuRef = database.getReference("resep").push()
                    val menuId = newMenuRef.key ?: throw IllegalStateException("Gagal membuat ID unik")

                    val resepData = hashMapOf(
                        "menuId" to menuId,
                        "menuName" to menuName,
                        "calorieContent" to calorieContent,
                        "diseases" to diseases,
                        "menuType" to menuType,
                        "imageURL" to imageUrl.toString(),
                        "alat" to alat,
                        "bahan" to bahan,
                        "langkah" to langkah,
                        "userId" to userId,
                        "username" to username
                    )

                    newMenuRef.setValue(resepData)
                        .addOnSuccessListener {
                            hideLoading()
                            showToast("Data berhasil diunggah")
                            showSuccessDialog()
                        }
                        .addOnFailureListener { exception ->
                            hideLoading()
                            showToast("Gagal mengunggah data: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                hideLoading()
                showToast("Gagal mengunggah gambar: ${exception.message}")
            }
    }

    private fun hideLoading() {
        loadingOverlay.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.popup_upresep)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnOk: Button = dialog.findViewById(R.id.btn_oke)
        btnOk.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.action_upResepFragment2_to_resepFragment)
        }

        // Show dialog if not already showing
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    /**
     * Metode untuk menampilkan pesan Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Metode untuk mendapatkan daftar bahan dari adapter.
     */
    private fun getBahanListFromAdapter(): List<String> {
        val bahanList: MutableList<String> = mutableListOf()
        for (i in 0 until inputBahanRecyclerView.childCount) {
            val viewHolder = inputBahanRecyclerView.findViewHolderForAdapterPosition(i) as BahanInputView.ViewHolder
            val newItem = viewHolder.itemView.findViewById<TextInputLayout>(R.id.input_bahan).editText?.text.toString()
            if (newItem.isNotBlank()) {
                bahanList.add(newItem)
            }
        }
        return bahanList
    }

    /**
     * Metode untuk mendapatkan daftar langkah dari adapter.
     */
    private fun getLangkahListFromAdapter(): List<String> {
        val langkahList: MutableList<String> = mutableListOf()
        for (i in 0 until inputLangkahRecyclerView.childCount) {
            val viewHolder = inputLangkahRecyclerView.findViewHolderForAdapterPosition(i) as LangkahInputView.ViewHolder
            val newItem = viewHolder.itemView.findViewById<TextInputLayout>(R.id.input_langkah).editText?.text.toString()
            if (newItem.isNotBlank()) {
                langkahList.add(newItem)
            }
        }
        return langkahList
    }
}
