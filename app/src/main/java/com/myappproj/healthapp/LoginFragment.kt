package com.myappproj.healthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Fragment untuk proses masuk pengguna menggunakan email/sandi atau Google Sign-In.
 */
class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    /**
     * Metode untuk membuat tata letak fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    /**
     * Metode yang dipanggil setelah tampilan dibuat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Konfigurasi Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        // Inisialisasi view
        val btnLogin = view.findViewById<Button>(R.id.btn_login2)
        val btnForgotPass = view.findViewById<TextView>(R.id.forgot_pass)
        val btnGoogle = view.findViewById<Button>(R.id.btn_google)
        val btnDaftarAlt = view.findViewById<TextView>(R.id.btn_daftaralt)

        // Set listener untuk tombol "Lupa kata sandi?"
        btnForgotPass.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPassFragment)
        }

        // Set listener untuk tombol "Daftar"
        btnDaftarAlt.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        // Set listener untuk tombol "Login"
        btnLogin.setOnClickListener {
            loginWithEmailAndPassword(view)
        }

        // Set listener untuk tombol "Login dengan Google"
        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    /**
     * Metode untuk login menggunakan email dan password
     */
    private fun loginWithEmailAndPassword(view: View) {
        val emailLayout = view.findViewById<TextInputLayout>(R.id.email)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.sandi)

        val email = emailLayout?.editText?.text.toString().trim()
        val password = passwordLayout?.editText?.text.toString().trim()

        // Validasi input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Silakan isi email dan password",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Proses login
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Login berhasil
                    navigateToMainActivity()
                } else {
                    // Tangani error login
                    handleLoginError(task.exception)
                }
            }
    }

    /**
     * Metode untuk login menggunakan Google Sign-In
     */
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    /**
     * Metode untuk menangani hasil Google Sign-In
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In berhasil, autentikasi dengan Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign-In gagal
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(requireContext(), "Google Sign-In gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Metode untuk autentikasi Firebase dengan Google Sign-In
     */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Login berhasil
                    navigateToMainActivity()
                } else {
                    // Login gagal
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        requireContext(),
                        "Autentikasi dengan Google gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Metode untuk menangani error login
     */
    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                "Email atau password salah"
            }
            is FirebaseAuthInvalidUserException -> {
                "Akun tidak ditemukan"
            }
            else -> {
                "Login gagal: ${exception?.message}"
            }
        }

        // Log error untuk debugging
        Log.e(TAG, "Login Error", exception)

        // Tampilkan pesan error
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    /**
     * Metode untuk navigasi ke MainActivity
     */
    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    companion object {
        private const val TAG = "LoginFragment"
        const val RC_GOOGLE_SIGN_IN = 9001
    }
}