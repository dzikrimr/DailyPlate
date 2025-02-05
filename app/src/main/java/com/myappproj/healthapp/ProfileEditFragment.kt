package com.myappproj.healthapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileEditFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private lateinit var tvUsername: TextInputLayout
    private lateinit var tvEmail: TextInputLayout
    private lateinit var tvPhone: TextInputLayout
    private lateinit var btnBatal: Button
    private lateinit var btnSimpan: Button
    private lateinit var profileImage: ImageView
    private lateinit var backButton: ImageView
    private lateinit var loadingOverlay: RelativeLayout
    private lateinit var progressBar: View

    private var selectedImageUri: Uri? = null
    private val GALLERY_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment edit profil
        return inflater.inflate(R.layout.fragment_profile_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase Auth, Database, dan Storage
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        // Inisialisasi UI
        tvUsername = view.findViewById(R.id.tl_username)
        tvEmail = view.findViewById(R.id.tl_email)
        tvPhone = view.findViewById(R.id.tl_phone)
        btnBatal = view.findViewById(R.id.btn_batal)
        btnSimpan = view.findViewById(R.id.btn_simpan)
        profileImage = view.findViewById(R.id.profile_def)
        backButton = view.findViewById(R.id.back_arrow)
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        progressBar = view.findViewById(R.id.progress_bar)

        // Ambil data pengguna
        loadUserData()

        // Set listener untuk tombol batal
        btnBatal.setOnClickListener {
            findNavController().navigate(R.id.action_profileEditFragment_to_accInformationFragment)
        }

        // Set listener untuk tombol simpan
        btnSimpan.setOnClickListener {
            updateUserData()
        }

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Set listener untuk mengedit foto profil
        view.findViewById<View>(R.id.edit_photo).setOnClickListener {
            openGallery()
        }
    }

    private fun loadUserData() {
        val currentUser  = auth.currentUser
        if (currentUser  != null) {
            val userId = currentUser .uid
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Pastikan menggunakan null safety dan default value
                        val username = snapshot.child("username").value?.toString() ?: ""
                        val email = currentUser .email ?: ""
                        val phone = snapshot.child("nomorTelepon").value?.toString() ?: ""

                        // Set text pada EditText di dalam TextInputLayout
                        tvUsername.editText?.setText(username)
                        tvEmail.editText?.setText(email)
                        tvPhone.editText?.setText(phone)

                        // Ambil URL foto profil
                        val photoUrl = snapshot.child("profileImage").value?.toString() ?: ""
                        if (photoUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(photoUrl)
                                .placeholder(R.drawable.profile_def) // Tambahkan placeholder
                                .error(R.drawable.profile_def) // Tambahkan error image
                                .circleCrop()
                                .into(profileImage)
                        } else {
                            // Jika tidak ada foto, gunakan foto default
                            profileImage.setImageResource(R.drawable.profile_def)
                        }
                    } else {
                        // Jika data tidak ditemukan, set default
                        tvUsername.editText?.setText(currentUser .displayName ?: "")
                        tvEmail.editText?.setText(currentUser .email ?: "")
                        profileImage.setImageResource(R.drawable.profile_def)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateUserData() {
        val currentUser  = auth.currentUser
        if (currentUser  != null) {
            val userId = currentUser .uid

            // Tampilkan overlay loading
            loadingOverlay.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE

            // Ambil nilai dari EditText
            val username = tvUsername.editText?.text.toString().trim()
            val email = tvEmail.editText?.text.toString().trim()
            val phone = tvPhone.editText?.text.toString().trim()

            // Validasi input
            if (username.isEmpty()) {
                tvUsername.error = "Nama tidak boleh kosong"
                loadingOverlay.visibility = View.GONE
                progressBar.visibility = View.GONE
                return
            }

            if (email.isEmpty()) {
                tvEmail.error = "Email tidak boleh kosong"
                loadingOverlay.visibility = View.GONE
                progressBar.visibility = View.GONE
                return
            }

            // Update data di Firebase
            val userData = hashMapOf<String, Any>(
                "username" to username,
                "email" to email,
                "nomorTelepon" to phone
            )

            database.child(userId).updateChildren(userData)
                .addOnSuccessListener {
                    // Update email di Authentication
                    currentUser .updateEmail(email)
                        .addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {
                                Toast.makeText(requireContext(), "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Gagal memperbarui email", Toast.LENGTH_SHORT).show()
                            }
                            loadingOverlay.visibility = View.GONE
                            progressBar.visibility = View.GONE
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                    loadingOverlay.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }

            // Jika ada gambar baru, upload ke Firebase Storage
            selectedImageUri?.let { uri ->
                uploadImageToFirebase(uri)
            }
        }
    }

    private fun openGallery() {
        // Intent untuk membuka galeri
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            // Tampilkan gambar yang dipilih di ImageView
            selectedImageUri?.let { uri ->
                Glide.with(requireContext())
                    .load(uri)
                    .circleCrop()
                    .into(profileImage)
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val currentUser  = auth.currentUser
        if (currentUser  != null) {
            val userId = currentUser .uid
            val filePath = storageRef.child("profile_images").child("$userId.jpg")

            filePath.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    filePath.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Update foto profil di database
                        database.child(userId).child("profileImage").setValue(downloadUri.toString())
                            .addOnSuccessListener {
                                // Tampilkan gambar yang baru diupload
                                Glide.with(requireContext())
                                    .load(downloadUri)
                                    .circleCrop()
                                    .into(profileImage)

                                Toast.makeText(requireContext(), "Foto profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Gagal menyimpan URL foto profil", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal mengupload foto profil", Toast.LENGTH_SHORT).show()
                }

        }
    }
}