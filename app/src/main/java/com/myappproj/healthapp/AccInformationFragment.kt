package com.myappproj.healthapp

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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

class AccInformationFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var btnHapusAkun: Button
    private lateinit var profileImage: ImageView
    private lateinit var backButton: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var isFragmentActive = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_acc_information, container, false)

        // Inisialisasi Firebase Auth dan Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users") // Ganti dengan path yang sesuai

        // Inisialisasi UI
        tvUsername = view.findViewById(R.id.tv_username)
        tvEmail = view.findViewById(R.id.tv_email)
        tvPhone = view.findViewById(R.id.tv_phone)
        btnHapusAkun = view.findViewById(R.id.btn_hapus)
        profileImage = view.findViewById(R.id.profile_def)
        backButton = view.findViewById(R.id.back_arrow)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFragmentActive = true // Menandai fragment aktif

        // Ambil data pengguna dari Firebase
        loadUserData()

        // Set OnClickListener pada tombol hapus akun
        btnHapusAkun.setOnClickListener {
            deleteAccount()
        }
    }

    private fun loadUserData() {
        val currentUser  = auth.currentUser
        if (currentUser  != null) {
            val userId = currentUser .uid
            val userRef = database.child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isFragmentActive) { // Periksa apakah fragment masih aktif
                        if (snapshot.exists()) {
                            val username = snapshot.child("username").value.toString()
                            val email = currentUser .email ?: ""
                            val phone = snapshot.child("nomorTelepon").value.toString()
                            val photoUrl = snapshot.child("profileImage").value?.toString() ?: ""

                            // Set data ke TextView
                            tvUsername.text = username
                            tvEmail.text = email
                            tvPhone.text = if (phone.isNotEmpty()) phone else "-"

                            // Tampilkan foto profil jika ada
                            if (photoUrl.isNotEmpty()) {
                                Glide.with(requireContext())
                                    .load(photoUrl)
                                    .placeholder(R.drawable.profile_def) // Placeholder jika gambar belum ada
                                    .error(R.drawable.profile_def) // Gambar default jika gagal
                                    .circleCrop()
                                    .into(profileImage)
                            } else {
                                // Jika tidak ada foto, gunakan foto default
                                profileImage.setImageResource(R.drawable.profile_def)
                            }
                        } else {
                            // Jika data tidak ditemukan di database
                            tvUsername.text = currentUser .displayName ?: "Pengguna"
                            tvEmail.text = currentUser .email ?: "Email tidak tersedia"
                            tvPhone.text = "-"
                            profileImage.setImageResource(R.drawable.profile_def)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isFragmentActive) { // Periksa apakah fragment masih aktif
                        Toast.makeText(requireContext(), "Gagal mengambil data profil", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun deleteAccount() {
        // Inflate layout dialog kustom
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)

        // Buat dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set pesan konfirmasi
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        tvMessage.text = getString(R.string.msg_deleteakun)

        // Handle tombol "Yes"
        val btnYes = dialogView.findViewById<Button>(R.id.btn_yes)
        btnYes.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Hapus akun dari Firebase
                currentUser.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Hapus data pengguna dari database
                        database.child(currentUser.uid).removeValue()
                        // Logout
                        auth.signOut()
                        Toast.makeText(requireContext(), "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                        // Navigasi kembali ke login atau halaman utama
                        findNavController().navigate(R.id.action_accInformationFragment_to_thirdScreen)
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus akun: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.dismiss() // Tutup dialog
        }

        // Handle tombol "No"
        val btnNo = dialogView.findViewById<Button>(R.id.btn_no)
        btnNo.setOnClickListener {
            dialog.dismiss() // Tutup dialog
        }

        // Tampilkan dialog
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}