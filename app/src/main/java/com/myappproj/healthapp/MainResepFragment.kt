package com.myappproj.healthapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.myappproj.healthapp.adapter.ChatAdapter
import com.myappproj.healthapp.model.ChatMessage
import com.myappproj.healthapp.model.MenuModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainResepFragment : Fragment() {
    // Variabel UI Resep
    private lateinit var namaMakanan: TextView
    private lateinit var jmlKalori: TextView
    private lateinit var penyakit: TextView
    private lateinit var listAlat: TextView
    private lateinit var listBahan: TextView
    private lateinit var listLangkah: TextView
    private lateinit var imgResep: ImageView
    private lateinit var backArrow: ImageView
    private lateinit var fabAiChat: ImageView
    private lateinit var tanyaAiText: TextView

    // Variabel AI Chat
    private lateinit var generativeModel: GenerativeModel
    private lateinit var currentMenuName: String
    private lateinit var currentMenuDetails: MenuModel
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    // Variabel UI Like
    private lateinit var btnLike: ImageView
    private lateinit var likeIcon: ImageView
    private lateinit var tvLiked: TextView
    private var isLiked = false
    private var likeCount = 0

    // Variabel Bottom Sheet
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var etChatInput: EditText? = null
    private var rvChat: RecyclerView? = null
    private var savedChatMessages: List<ChatMessage>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_resep, container, false)

        // Inisialisasi view
        initializeViews(view)

        // Setup listeners
        setupListeners()

        // Inisialisasi Gemini Model
        initializeGeminiModel()

        // Ambil dan tampilkan data resep
        fetchRecipeDetails()

        return view
    }

    private fun initializeViews(view: View) {
        namaMakanan = view.findViewById(R.id.nama_makanan)
        jmlKalori = view.findViewById(R.id.jml_kalori)
        penyakit = view.findViewById(R.id.penyakit)
        listAlat = view.findViewById(R.id.listalat)
        listBahan = view.findViewById(R.id.listbahan)
        listLangkah = view.findViewById(R.id.listlangkah)
        imgResep = view.findViewById(R.id.img_resep)
        backArrow = view.findViewById(R.id.back_arrow)
        fabAiChat = view.findViewById(R.id.fab_ai_chat)
        tanyaAiText = view.findViewById(R.id.tanya_ai_text)
        btnLike = view.findViewById(R.id.btn_like)
        tvLiked = view.findViewById(R.id.liked)
    }

    private fun setupListeners() {
        backArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        setupFloatingAnimation()

        btnLike.setOnClickListener {
            toggleLike()
        }

        // Setup listener klik
        fabAiChat.setOnClickListener {
            showAIChatBottomSheet()
        }
    }

    private fun toggleLike() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        isLiked = !isLiked
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_like_fill)
            likeCount++
            showLoveBubbles()
            saveRecipeToUserSavedList(userId) // Simpan resep ke daftar saved recipes
        } else {
            btnLike.setImageResource(R.drawable.ic_like)
            likeCount--
            removeRecipeFromUserSavedList(userId) // Hapus resep dari daftar saved recipes
        }

        updateLikedTextView()
        updateLikeCountInFirebase()
        updateUserLikeStatusInFirebase(userId)
    }

    private fun saveRecipeToUserSavedList(userId: String) {
        val menuId = arguments?.getString("menuId") // Ambil menuId dari argumen
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("savedRecipes")
        ref.child(menuId ?: "").setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Resep disimpan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan resep", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeRecipeFromUserSavedList(userId: String) {
        val menuId = arguments?.getString("menuId") // Ambil menuId dari argumen
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("savedRecipes")
        ref.child(menuId ?: "").removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Resep dihapus dari disimpan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Gagal menghapus resep", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLikeCountInFirebase() {
        val menuId = arguments?.getString("menuId")
        val ref = FirebaseDatabase.getInstance().getReference("resep")

        // Update the likeCount within the specific recipe ID
        menuId?.let { id ->
            ref.child(id).child("likeCount").setValue(likeCount).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "likeCount updated successfully for $id")
                } else {
                    Log.e("FirebaseError", "Failed to update likeCount for $id: ${task.exception?.message}")
                }
            }
        }
    }

    private fun updateUserLikeStatusInFirebase(userId: String) {
        val menuId = arguments?.getString("menuId")
        val ref = FirebaseDatabase.getInstance().getReference("resep")

        menuId?.let { id ->
            val likesRef = ref.child(id).child("likedByUsers")
            if (isLiked) {
                likesRef.child(userId).setValue(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "User $userId liked recipe $id")
                    } else {
                        Log.e("FirebaseError", "Failed to update like status for $userId on recipe $id: ${task.exception?.message}")
                    }
                }
            } else {
                likesRef.child(userId).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "User $userId unliked recipe $id")
                    } else {
                        Log.e("FirebaseError", "Failed to remove like status for $userId on recipe $id: ${task.exception?.message}")
                    }
                }
            }
        }
    }

    private fun updateLikedTextView() {
        tvLiked.text = "$likeCount"
    }

    private fun checkUserLikeStatus() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            btnLike.isEnabled = false
            return
        }

        val menuId = arguments?.getString("menuId")
        val ref = FirebaseDatabase.getInstance().getReference("resep").child(menuId ?: "")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Update status like
                    isLiked = snapshot.child("likedByUsers").hasChild(userId)

                    // Update tampilan
                    btnLike.setImageResource(
                        if (isLiked) R.drawable.ic_like_fill
                        else R.drawable.ic_like
                    )

                    // Update jumlah like
                    likeCount = snapshot.child("likeCount").getValue(Int::class.java) ?: 0
                    updateLikedTextView()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal memuat status like", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoveBubbles() {
        val loveCount = 3 // Jumlah love
        val delay = 200L // Jeda waktu antar keluaran dalam milidetik (0,2 detik)
        val parentLayout = btnLike.parent as ViewGroup

        for (i in 0 until loveCount) {
            Handler(Looper.getMainLooper()).postDelayed({
                val loveBubble = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.ic_like_fill)
                    layoutParams = ViewGroup.LayoutParams(50, 50)
                    x = btnLike.x + btnLike.width / 2 - 25 + (i * 5) - 10
                    y = btnLike.y + btnLike.height / 2 - 25 - (i * 30)
                    visibility = View.VISIBLE
                }

                parentLayout.addView(loveBubble)

                val translationY = ObjectAnimator.ofFloat(loveBubble, "translationY", -5f + (i * 3))
                val fadeOut = ObjectAnimator.ofFloat(loveBubble, "alpha", 1f, 0f)

                val animatorSet = AnimatorSet()
                animatorSet.playTogether(translationY, fadeOut)
                animatorSet.duration = 1000
                animatorSet.interpolator = AccelerateDecelerateInterpolator()

                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        parentLayout.removeView(loveBubble)
                    }
                })
                animatorSet.start()
            }, i * delay)
        }
    }

    private fun setupFloatingAnimation() {
        // Animasi naik-turun untuk ImageView
        val floatAnimator = ObjectAnimator.ofFloat(fabAiChat, "translationY", 0f, -20f, 0f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Animasi naik-turun untuk TextView dengan offset sedikit
        val floatTextAnimator = ObjectAnimator.ofFloat(tanyaAiText, "translationY", 0f, -15f, 0f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            // Tambahkan sedikit delay untuk efek berbeda
            startDelay = 100
        }

        // Jalankan animasi
        floatAnimator.start()
        floatTextAnimator.start()
    }

    private fun initializeGeminiModel() {
        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.apiKey
        )
    }

    private fun fetchRecipeDetails() {
        val menuId = arguments?.getString("menuId")
        val ref = FirebaseDatabase.getInstance().getReference("resep").child(menuId ?: "")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val menu = snapshot.getValue(MenuModel::class.java)
                    menu?.let {
                        currentMenuDetails = it.copy(menuId = menuId ?: "")
                        displayRecipeDetails(currentMenuDetails)
                    }
                } else {
                    Toast.makeText(requireContext(), "Resep tidak ditemukan", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Gagal mengambil data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayRecipeDetails(menu: MenuModel) {
        // Simpan detail menu untuk keperluan chat AI
        currentMenuName = menu.menuName
        currentMenuDetails = menu

        // Set data ke UI
        namaMakanan.text = menu.menuName
        jmlKalori.text = "${menu.calorieContent} cal"
        penyakit.text = getString(R.string.cocok_untuk_penyakit) + " ${menu.diseases}"
        listAlat.text = "• ${menu.alat}"

        // Format bahan
        val bahanText = menu.bahan.joinToString("\n") { "• $it" }
        listBahan.text = bahanText

        // Format langkah
        val langkahText = menu.langkah.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n\n")
        listLangkah.text = langkahText

        likeCount = menu.likeCount ?: 0
        updateLikedTextView()

        checkUserLikeStatus()

        // Tampilkan username yang mengupload
        val posting: TextView = requireView().findViewById(R.id.posting)

        // Cek apakah username sudah tersimpan di data resep
        val username = menu.username

        // Jika username kosong, coba ambil dari database
        if (username.isNullOrEmpty()) {
            val userId = menu.userId // Pastikan Anda menyimpan userId saat upload resep
            if (!userId.isNullOrEmpty()) {
                // Ambil username dari database
                FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(userId)
                    .child("username")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val fetchedUsername = snapshot.value?.toString() ?: "User"
                        posting.text = "${getString(R.string.posted)} $fetchedUsername"
                    }
                    .addOnFailureListener {
                        posting.text = "${getString(R.string.posted)} User"
                    }
            } else {
                // Jika tidak ada userId, tampilkan default
                posting.text = "${getString(R.string.posted)} User"
            }
        } else {
            // Gunakan username yang sudah tersimpan di data resep
            posting.text = "${getString(R.string.posted)} $username"
        }

        // Load gambar
        Glide.with(requireContext())
            .load(menu.imageURL)
            .placeholder(R.drawable.imgview_resep)
            .centerCrop()
            .into(imgResep)
    }

    private fun showAIChatBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_ai_chat, null)

        rvChat = bottomSheetView.findViewById(R.id.rv_chat)
        etChatInput = bottomSheetView.findViewById(R.id.et_chat_input)
        val btnSend: ImageButton = bottomSheetView.findViewById(R.id.btn_send)
        val tvResepTitle: TextView = bottomSheetView.findViewById(R.id.tv_resep_title)

        // Set judul resep
        val chatTentang = getString(R.string.chat_ai)
        tvResepTitle.text = "$chatTentang $currentMenuName"

        // Setup RecyclerView
        // Gunakan chat history yang tersimpan atau buat baru
        chatMessages.clear()
        if (savedChatMessages != null) {
            chatMessages.addAll(savedChatMessages!!)
        }

        chatAdapter = ChatAdapter(chatMessages)
        rvChat?.layoutManager = LinearLayoutManager(requireContext())
        rvChat?.adapter = chatAdapter

        // Scroll ke posisi terakhir jika ada pesan
        if (chatMessages.isNotEmpty()) {
            rvChat?.scrollToPosition(chatMessages.size - 1)
        }

        btnSend.setOnClickListener {
            val userMessage = etChatInput?.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addUserMessage(userMessage)
                sendMessageToAI(userMessage)
            }
        }

        // Tambahkan listener untuk menyimpan chat saat bottom sheet ditutup
        bottomSheetDialog?.setOnDismissListener {
            // Simpan chat history
            savedChatMessages = chatMessages.toList()
        }

        // Set OnShowListener untuk mengatur background dengan drawable
        bottomSheetDialog?.setOnShowListener {
            val bottomSheet = (it as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.background = ContextCompat.getDrawable(requireContext(), R.drawable.bottom_sheet_background)
        }

        bottomSheetDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheetDialog?.setContentView(bottomSheetView)
        bottomSheetDialog?.show()
    }


    private fun addUserMessage(userMessage: String) {
        chatMessages.add(ChatMessage(userMessage, true))
        etChatInput?.text?.clear()
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        rvChat?.scrollToPosition(chatMessages.size - 1)
    }

    private fun sendMessageToAI(userQuestion: String) {
        // Tampilkan titik loading
        val loadingMessage = ChatMessage("...", false)
        chatMessages.add(loadingMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        rvChat?.scrollToPosition(chatMessages.size - 1)

        // Mulai animasi titik
        val handler = Handler(Looper.getMainLooper())
        val dots = arrayOf("...", "....", ".....", "......")
        var dotIndex = 0

        val loadingRunnable = object : Runnable {
            override fun run() {
                val currentIndex = chatMessages.indexOf(loadingMessage)
                if (currentIndex != -1) {
                    loadingMessage.text = dots[dotIndex]
                    chatAdapter.notifyItemChanged(currentIndex)
                    dotIndex = (dotIndex + 1) % dots.size
                    handler.postDelayed(this, 400)
                }
            }
        }
        handler.post(loadingRunnable)

        // Buat prompt yang mencakup detail resep
        val prompt = """
    Saya sedang melihat resep ${currentMenuName}. 
    Detail resep:
    - Bahan: ${currentMenuDetails.bahan.joinToString(", ")}
    - Alat: ${currentMenuDetails.alat}
    - Langkah: ${currentMenuDetails.langkah.joinToString(" ")}
    
    Pertanyaan pengguna: $userQuestion
    
    Tolong jawab dengan singkat, jelas, ramah (jika pengguna menanya menggunakan english maka jawab dengan english).
    """.trimIndent()

        // Kirim pertanyaan ke Gemini
        lifecycleScope.launch {
            try {
                // Generate konten dari Gemini
                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }

                // Hentikan animasi titik
                handler.removeCallbacksAndMessages(null)

                // Hapus pesan loading
                val loadingIndex = chatMessages.indexOf(loadingMessage)
                if (loadingIndex != -1) {
                    chatMessages.removeAt(loadingIndex)
                    chatAdapter.notifyItemRemoved(loadingIndex)
                }

                // Dapatkan teks respon
                val aiResponse = response.text
                    ?: "Maaf, saya tidak dapat menjawab pertanyaan saat ini."

                // Tambahkan respon AI ke daftar pesan
                chatMessages.add(ChatMessage(aiResponse, false))
                chatAdapter.notifyItemInserted(chatMessages.size - 1)

                // Gulir ke posisi terakhir
                rvChat?.scrollToPosition(chatMessages.size - 1)
            } catch (e: Exception) {
                // Hentikan animasi titik
                handler.removeCallbacksAndMessages(null)

                // Hapus pesan loading
                val loadingIndex = chatMessages.indexOf(loadingMessage)
                if (loadingIndex != -1) {
                    chatMessages.removeAt(loadingIndex)
                    chatAdapter.notifyItemRemoved(loadingIndex)
                }

                // Tambahkan pesan error
                val errorMessage = "Terjadi kesalahan: ${e.localizedMessage}"
                chatMessages.add(ChatMessage(errorMessage, false))
                chatAdapter.notifyItemInserted(chatMessages.size - 1)
                rvChat?.scrollToPosition(chatMessages.size - 1)
            }
        }
    }

    // Bersihkan referensi saat view dihancurkan
    private fun clearChatHistory() {
        savedChatMessages = null
    }

    // Modifikasi method yang sesuai
    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetDialog = null
        etChatInput = null
        rvChat = null

        // Hapus chat history jika fragment dihancurkan
        clearChatHistory()
    }
}