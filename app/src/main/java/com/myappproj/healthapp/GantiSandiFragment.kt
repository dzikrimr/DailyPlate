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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class GantiSandiFragment : Fragment() {

    private lateinit var sandiLamaInputLayout: TextInputLayout
    private lateinit var sandiBaruInputLayout: TextInputLayout
    private lateinit var sandiBaruKonfInputLayout: TextInputLayout
    private lateinit var simpanButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment ini
        val view = inflater.inflate(R.layout.fragment_ganti_sandi, container, false)
        val textSandi = view.findViewById<ImageView>(R.id.back_arrow)

        // Inisialisasi Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Inisialisasi view
        sandiLamaInputLayout = view.findViewById(R.id.sandi_lama)
        sandiBaruInputLayout = view.findViewById(R.id.sandi_baru)
        sandiBaruKonfInputLayout = view.findViewById(R.id.sandibaru_konf)
        simpanButton = view.findViewById(R.id.btn_save)

        // Atur listener klik untuk tombol simpan
        simpanButton.setOnClickListener {
            gantiSandi()
        }
        textSandi.setOnClickListener {
            // Navigasi ke HomeFragment2 saat tombol diklik
            findNavController().navigate(R.id.action_gantiSandiFragment_to_profileFragment)
        }

        return view
    }

    /**
     * Fungsi untuk mengganti sandi pengguna.
     */
    private fun gantiSandi() {
        val user: FirebaseUser? = auth.currentUser
        val sandiLama = sandiLamaInputLayout.editText?.text.toString()
        val sandiBaru = sandiBaruInputLayout.editText?.text.toString()
        val sandiBaruKonf = sandiBaruKonfInputLayout.editText?.text.toString()

        // Validasi input
        if (user != null && sandiLama.isNotEmpty() && sandiBaru.isNotEmpty() && sandiBaru == sandiBaruKonf) {
            // Re-autentikasi pengguna
            val credential = EmailAuthProvider.getCredential(user.email!!, sandiLama)
            user.reauthenticate(credential)
                .addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // Ubah sandi
                        user.updatePassword(sandiBaru)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Sandi berhasil diubah
                                    sandiLamaInputLayout.error = null
                                    sandiBaruInputLayout.error = null
                                    sandiBaruKonfInputLayout.error = null
                                    // Tampilkan dialog untuk pengubahan sandi berhasil
                                    showPasswordChangeDialog()
                                } else {
                                    // Gagal mengubah sandi
                                    Toast.makeText(requireContext(), "Gagal mengubah sandi", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // Gagal melakukan re-autentikasi pengguna
                        Toast.makeText(requireContext(), "Sandi lama salah", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Input tidak valid
            if (sandiLama.isEmpty()) {
                Toast.makeText(requireContext(), "Masukkan sandi lama", Toast.LENGTH_SHORT).show()
            }
            if (sandiBaru.isEmpty()) {
                Toast.makeText(requireContext(), "Masukkan sandi baru", Toast.LENGTH_SHORT).show()
            }
            if (sandiBaruKonf.isEmpty()) {
                Toast.makeText(requireContext(), "Konfirmasi sandi baru", Toast.LENGTH_SHORT).show()
            }
            if (sandiBaru != sandiBaruKonf) {
                Toast.makeText(requireContext(), "Sandi baru tidak cocok", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Menampilkan dialog kustom untuk menunjukkan bahwa sandi telah berhasil diubah.
     */
    private fun showPasswordChangeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_gantisandi, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
        val dialog = dialogBuilder.create()

        // Atur latar belakang dialog menjadi transparan
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btn_oke).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
