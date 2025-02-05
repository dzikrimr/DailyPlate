package com.myappproj.healthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment pendaftaran
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi instance Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Ambil referensi dari tombol daftar dan tombol untuk login alternatif
        val btnDaftar = view.findViewById<Button>(R.id.btn_daftar2)
        val btnLoginAlt = view.findViewById<TextView>(R.id.btn_loginalt)

        // Set OnClickListener pada tombol daftar
        btnDaftar.setOnClickListener {
            // Panggil fungsi untuk melakukan pendaftaran pengguna baru
            registerUser()
        }

        // Set OnClickListener pada tombol untuk login alternatif
        btnLoginAlt.setOnClickListener {
            // Navigasi ke fragment login menggunakan NavController
            val navController = findNavController()
            navController.navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    // Fungsi untuk melakukan pendaftaran pengguna baru menggunakan Firebase Authentication
    private fun registerUser () {
        // Ambil nilai dari field input nama, email, nomor telepon, sandi, dan konfirmasi sandi
        val nama = view?.findViewById<TextInputLayout>(R.id.nama)?.editText?.text.toString()
        val email = view?.findViewById<TextInputLayout>(R.id.email)?.editText?.text.toString()
        val nomorTelepon = view?.findViewById<TextInputLayout>(R.id.no_telepon)?.editText?.text.toString()
        val sandi = view?.findViewById<TextInputLayout>(R.id.sandi)?.editText?.text.toString()
        val konfirmasiSandi = view?.findViewById<TextInputLayout>(R.id.konf_sandi)?.editText?.text.toString()

        // Validasi input
        if (nama.isEmpty() || email.isEmpty() || sandi.isEmpty() || konfirmasiSandi.isEmpty()) {
            showToast("Semua kolom harus diisi")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Format email tidak valid")
            return
        }

        if (sandi.length < 8) {
            showToast("Kata sandi harus memiliki setidaknya 8 karakter")
            return
        }

        // Lakukan pendaftaran ke Firebase menggunakan createUser WithEmailAndPassword
        auth.createUserWithEmailAndPassword(email, sandi)
        .addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                // Simpan username ke database
                val userId = auth.currentUser ?.uid
                val database = FirebaseDatabase.getInstance()
                val ref = database.getReference("users").child(userId!!)

                // Simpan data tambahan
                val userData = hashMapOf(
                    "username" to nama,
                    "email" to email
                )

                // Hanya tambahkan nomor telepon jika diisi
                if (nomorTelepon.isNotEmpty()) {
                    userData["nomorTelepon"] = nomorTelepon
                }

                ref.setValue(userData)
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_signUpFragment_to_berandaFragment)
                        showToast("Pendaftaran berhasil")
                    }
                    .addOnFailureListener {
                        showToast("Gagal menyimpan data tambahan")
                    }
            } else {
                showToast("Email telah terdaftar")
            }
        }
    }

    // Fungsi untuk menampilkan pesan toast
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
