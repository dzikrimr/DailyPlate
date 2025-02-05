package com.myappproj.healthapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

class ForgotPassFragment : Fragment() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var btnKirim: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment ini
        val view = inflater.inflate(R.layout.fragment_forgot_pass, container, false)

        // Inisialisasi view
        emailInputLayout = view.findViewById(R.id.email)
        btnKirim = view.findViewById(R.id.btn_kirim)

        // Atur listener klik untuk tombol "Kirim"
        btnKirim.setOnClickListener {
            // Dapatkan input email
            val email = emailInputLayout.editText?.text.toString()

            // Periksa apakah email kosong
            if (email.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Silakan masukkan alamat email Anda!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Kirim email reset kata sandi menggunakan Firebase
            sendPasswordResetEmail(email)
        }

        return view
    }

    /**
     * Mengirim email reset kata sandi ke alamat email yang diberikan.
     */
    private fun sendPasswordResetEmail(email: String) {
        val auth = FirebaseAuth.getInstance()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email reset kata sandi berhasil dikirim, tampilkan pop-up kustom
                    showSuccessPopup()
                } else {
                    // Tangani kegagalan jika diperlukan
                    // Misalnya, tampilkan toast yang menunjukkan kegagalan
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengirim email reset kata sandi. Pastikan alamat email Anda benar.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Menampilkan dialog kustom untuk menunjukkan bahwa email reset kata sandi telah berhasil dikirim.
     */
    private fun showSuccessPopup() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.popup_forgotpass)

        val closeButton = dialog.findViewById<Button>(R.id.btn_oke)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Atur latar belakang dialog menjadi transparan
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}
