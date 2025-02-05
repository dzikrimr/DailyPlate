package com.myappproj.healthapp

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.myappproj.healthapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var isFragmentActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout menggunakan data binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFragmentActive = true

        // Aktifkan shimmer loading
        startShimmer()

        // Ambil data pengguna dari Firebase
        loadUserData()

        // Tombol logout
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Navigasi ke halaman lain
        binding.gantiSandi.setOnClickListener { navigateToGantiSandi() }
        binding.infoAkun.setOnClickListener { navigateToInfoAkun() }
        binding.editProfil.setOnClickListener { navigateToEditAkun() }
        binding.bahasa.setOnClickListener { navigateToUbahBahasa() }
        binding.pusatBantuan.setOnClickListener { navigateToHelpCenter() }
        binding.pengaturan.setOnClickListener { navigateToSettings() }
    }

    private fun startShimmer() {
        binding.shimmerProfilePic.startShimmer()
        binding.shimmerName.startShimmer()
        binding.shimmerEmail.startShimmer()
    }

    private fun stopShimmer() {
        binding.shimmerProfilePic.stopShimmer()
        binding.shimmerName.stopShimmer()
        binding.shimmerEmail.stopShimmer()

        // Sembunyikan shimmer dan tampilkan data
        binding.shimmerProfilePic.visibility = View.GONE
        binding.shimmerName.visibility = View.GONE
        binding.shimmerEmail.visibility = View.GONE
        binding.ppProfil2.visibility = View.VISIBLE
        binding.tvName.visibility = View.VISIBLE
        binding.tvEmail.visibility = View.VISIBLE
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                if (isFragmentActive) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").value.toString()
                        val email = currentUser.email ?: ""
                        val photoUrl = snapshot.child("profileImage").value?.toString() ?: ""

                        // Set data ke TextView
                        binding.tvName.text = username
                        binding.tvEmail.text = email

                        // Tampilkan foto profil jika ada
                        if (photoUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(photoUrl)
                                .placeholder(R.drawable.profile_def)
                                .error(R.drawable.profile_def)
                                .circleCrop()
                                .into(binding.ppProfil2)
                        } else {
                            binding.ppProfil2.setImageResource(R.drawable.profile_def)
                        }
                    } else {
                        binding.tvName.text = currentUser.displayName ?: "Pengguna"
                        binding.tvEmail.text = currentUser.email ?: "Email tidak tersedia"
                        binding.ppProfil2.setImageResource(R.drawable.profile_def)
                    }
                    stopShimmer()
                }
            }.addOnFailureListener {
                if (isFragmentActive) {
                    Toast.makeText(requireContext(), "Gagal mengambil data profil", Toast.LENGTH_SHORT).show()
                    stopShimmer()
                }
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popup_logout, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnYes = dialogView.findViewById<Button>(R.id.btn_yes)
        val btnNo = dialogView.findViewById<Button>(R.id.btn_no)

        btnYes.setOnClickListener {
            alertDialog.dismiss()
            (activity as MainActivity).logout()
        }
        btnNo.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun navigateToGantiSandi() {
        findNavController().navigate(R.id.gantiSandiFragment)
    }

    private fun navigateToInfoAkun() {
        findNavController().navigate(R.id.accInformationFragment)
    }

    private fun navigateToEditAkun() {
        findNavController().navigate(R.id.profileEditFragment)
    }

    private fun navigateToUbahBahasa() {
        findNavController().navigate(R.id.bahasaFragment)
    }

    private fun navigateToSettings() {
        findNavController().navigate(R.id.settingsFragment)
    }

    private fun navigateToHelpCenter() {
        findNavController().navigate(R.id.helpCenterFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isFragmentActive = false
    }
}
