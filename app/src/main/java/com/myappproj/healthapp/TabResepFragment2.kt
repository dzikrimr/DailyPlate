package com.myappproj.healthapp

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myappproj.healthapp.adapter.MyMenuView
import com.myappproj.healthapp.model.MenuModel

class TabResepFragment2 : Fragment(), MyMenuView.MenuClickListener {

    private lateinit var adapter: MyMenuView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyresep1: TextView
    private lateinit var emptyresep2: TextView
    private lateinit var emptyresep3: ImageView
    private lateinit var deleteIcon: Drawable
    private lateinit var extendFab: FloatingActionButton
    private lateinit var fabOption1: FloatingActionButton
    private lateinit var fabOption2: FloatingActionButton
    private lateinit var fabOption3: FloatingActionButton
    private lateinit var fabLabel1: TextView
    private lateinit var fabLabel2: TextView
    private lateinit var fabLabel3: TextView
    private var isFabOpen = false
    private var valueEventListener: ValueEventListener? = null
    private var databaseRef: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab_resep2, container, false)

        // Konfigurasi tombol kembali
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (findNavController().currentDestination?.id == R.id.tabResepFragment2) {
                    findNavController().popBackStack()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })

        // Inisialisasi view
        recyclerView = view.findViewById(R.id.recyclerview_mymenu)
        emptyresep1 = view.findViewById(R.id.empty_titleresep)
        emptyresep2 = view.findViewById(R.id.empty_titleresep2)
        emptyresep3 = view.findViewById(R.id.empty_resepbg)
        extendFab = view.findViewById(R.id.extend_resep)
        fabOption1 = view.findViewById(R.id.fab_option1)
        fabOption2 = view.findViewById(R.id.fab_option2)
        fabOption3 = view.findViewById(R.id.fab_option3)
        fabLabel1 = view.findViewById(R.id.fab_label1)
        fabLabel2 = view.findViewById(R.id.fab_label2)
        fabLabel3 = view.findViewById(R.id.fab_label3)

        // Konfigurasi adapter dan RecyclerView
        adapter = MyMenuView(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Ambil data dari Firebase
        retrieveDataFromFirebase()

        // Setup tombol tambah resep
        val addResepButton: FloatingActionButton = view.findViewById(R.id.extend_resep)
        addResepButton.setOnClickListener {
            findNavController().navigate(R.id.action_resepFragment_to_upResepFragment1)
        }

        // Inisialisasi ikon delete
        deleteIcon = resources.getDrawable(R.drawable.ic_delete, null)

        // Tambahkan swipe to delete
        setupSwipeToDelete()

        // Set listener untuk FAB utama
        extendFab.setOnClickListener {
            toggleFab()
        }

        // Set listener untuk opsi FAB
        fabOption1.setOnClickListener {
            showPopup()
        }

        fabOption2.setOnClickListener {
            findNavController().navigate(R.id.action_resepFragment_to_upResepFragment1)
        }

        fabOption3.setOnClickListener {
            findNavController().navigate(R.id.savedMenuFragment)
        }

        return view
    }

    private fun toggleFab() {
        if (isFabOpen) {
            // Kembalikan ikon ke "+"
            animateFabIcon(0f) // Rotasi kembali ke 0 derajat
            hideFabOption(fabOption1, fabLabel1)
            hideFabOption(fabOption2, fabLabel2)
            hideFabOption(fabOption3, fabLabel3)
            isFabOpen = false
        } else {
            // Ubah ikon menjadi "X"
            animateFabIcon(45f) // Rotasi 45 derajat untuk efek "X"
            showFabOption(fabOption1, fabLabel1, -resources.getDimension(R.dimen.fab_margin))
            showFabOption(fabOption2, fabLabel2, -resources.getDimension(R.dimen.fab_margin) * 2)
            showFabOption(fabOption3, fabLabel3, -resources.getDimension(R.dimen.fab_margin) * 3)
            isFabOpen = true
        }
    }

    private fun animateFabIcon(rotation: Float) {
        extendFab.animate()
            .rotation(rotation)
            .setDuration(200)
            .start()
    }

    private fun showFabOption(fab: FloatingActionButton, label: TextView, translationY: Float) {
        fab.visibility = View.VISIBLE
        label.visibility = View.VISIBLE

        fab.animate().translationY(translationY).alpha(1f).setDuration(200).start()
        label.animate().alpha(1f).setDuration(200).start()

        fab.post {
            // Get the current position of the FAB after translation
            val fabX = fab.x
            val fabY = fab.y + translationY // Use the translated Y position

            // Position the label to the left of the FAB
            label.x = fabX - label.width - resources.getDimension(R.dimen.label_margin)
            label.y = fabY + (fab.height / 2) - (label.height / 2) // Center the label vertically
        }
    }

    private fun hideFabOption(fab: FloatingActionButton, label: TextView) {
        fab.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
            fab.visibility = View.GONE
        }.start()
        label.animate().alpha(0f).setDuration(200).withEndAction {
            label.visibility = View.GONE
        }.start()
    }

    private fun showPopup() {
        // Buat TextView secara programatis
        val textView = TextView(requireContext()).apply {
            text = getString(R.string.info_msg)
            textSize = 16f
            val textColor = ContextCompat.getColor(requireContext(), R.color.green)
            setTextColor(textColor)
            gravity = Gravity.CENTER
            setPadding(
                resources.getDimensionPixelSize(R.dimen.popup_padding), // 16dp
                resources.getDimensionPixelSize(R.dimen.popup_padding), // 16dp
                resources.getDimensionPixelSize(R.dimen.popup_padding), // 16dp
                resources.getDimensionPixelSize(R.dimen.popup_padding) // 16dp
            )
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_popup)
            alpha = 0f // Set alpha awal ke 0 untuk animasi fade in
        }

        // Buat PopupWindow
        val popupWindow = PopupWindow(
            textView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            // Set background agar popup bisa dismiss saat di klik di luar
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 10f
        }

        // Tampilkan popup di tengah layar
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0)

        // Animasi fade in
        textView.animate().alpha(1f).setDuration(300).start()

        // Dismiss popup saat di klik
        textView.setOnClickListener {
            // Animasi fade out sebelum dismiss
            textView.animate().alpha(0f).setDuration(300).withEndAction {
                popupWindow.dismiss()
            }.start()
        }
    }

    // Konfigurasi swipe to delete
    private fun setupSwipeToDelete() {
        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val backgroundPaint = Paint().apply {
                color = Color.RED
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(position)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                // Batasi dX agar tidak lebih dari seperempat lebar item
                val limit = itemView.width / 4
                val newDx = if (dX < -limit) -limit.toFloat() else dX

                // Gambar background merah dengan sudut melengkung hanya di sisi kanan
                if (newDx < 0) { // Hanya untuk swipe ke kiri
                    val backgroundRect = RectF(
                        itemView.right + newDx,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    c.drawRoundRect(backgroundRect, 50f, 50f, backgroundPaint) // 30f adalah radius

                    // Gambar ikon delete
                    val deleteIconTop = itemView.top + (itemHeight - deleteIcon.intrinsicHeight) / 2
                    val deleteIconLeft = itemView.right + newDx / 2 - deleteIcon.intrinsicWidth / 2
                    val deleteIconRight = itemView.right + newDx / 2 + deleteIcon.intrinsicWidth / 2
                    val deleteIconBottom = deleteIconTop + deleteIcon.intrinsicHeight

                    deleteIcon.setBounds(deleteIconLeft.toInt(), deleteIconTop, deleteIconRight.toInt(), deleteIconBottom)
                    deleteIcon.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive)
            }
        }

        // Attach swipe callback to RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)  // Prevent dismissal while deleting
            .create()

        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        tvMessage.text = getString(R.string.msg_deleteresep)

        val btnYes = dialogView.findViewById<Button>(R.id.btn_yes)
        btnYes.setOnClickListener {
            adapter.deleteItem(position)
            dialog.dismiss()
        }

        val btnNo = dialogView.findViewById<Button>(R.id.btn_no)
        btnNo.setOnClickListener {
            dialog.dismiss()
            adapter.notifyItemChanged(position)  // Reset the swipe state
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    // Navigasi ke halaman detail resep saat item diklik
    override fun onMenuClicked(menuId: String) {
        val bundle = Bundle().apply {
            putString("menuId", menuId)
        }
        findNavController().navigate(R.id.action_resepFragment_to_mainResepFragment, bundle)
    }

    private fun retrieveDataFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val ref = FirebaseDatabase.getInstance().getReference("resep")

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isAdded && view != null) {
                        val menuList = mutableListOf<MenuModel>()

                        for (menuSnapshot in snapshot.children) {
                            val menu = menuSnapshot.getValue(MenuModel::class.java)
                            menu?.let {
                                // Pastikan ini resep dari user yang sedang login
                                if (it.userId == uid) {
                                    // Pastikan menuId sesuai dengan key di Firebase
                                    it.menuId = menuSnapshot.key!!
                                    menuList.add(it)
                                }
                            }
                        }

                        // Sort untuk konsistensi tampilan
                        menuList.sortByDescending { it.menuId }

                        // Update UI di main thread
                        activity?.runOnUiThread {
                            updateViewVisibility(menuList)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Failed to read value.", error.toException())
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener when fragment is destroyed
        valueEventListener?.let { listener ->
            databaseRef?.removeEventListener(listener)
        }
    }

    // Perbarui tampilan RecyclerView
    private fun updateViewVisibility(menuList: List<MenuModel>) {
        try {
            if (menuList.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyresep1.visibility = View.VISIBLE
                emptyresep2.visibility = View.VISIBLE
                emptyresep3.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyresep1.visibility = View.GONE
                emptyresep2.visibility = View.GONE
                emptyresep3.visibility = View.GONE
                adapter.setData(menuList)
            }
        } catch (e: Exception) {
            Log.e("UpdateViewError", "Error updating view visibility", e)
        }
    }
}