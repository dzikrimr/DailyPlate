package com.myappproj.healthapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*
import com.myappproj.healthapp.adapter.HorizontalBahan
import com.myappproj.healthapp.adapter.HorizontalRecyclerView
import com.myappproj.healthapp.adapter.MyMenuView
import com.myappproj.healthapp.model.BahanModel
import com.myappproj.healthapp.model.ItemModel
import com.myappproj.healthapp.model.MenuModel
import com.myappproj.healthapp.adapter.BannerAdapter
import render.animations.Bounce
import render.animations.Render
import render.animations.Slide

class BerandaFragment : Fragment(), HorizontalRecyclerView.ItemClickListener, MyMenuView.MenuClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewMenu: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var recyclerView3: RecyclerView
    private lateinit var adapter: HorizontalRecyclerView
    private lateinit var adapter2: HorizontalBahan
    private lateinit var adapter3: HorizontalBahan
    private lateinit var menuAdapter: MyMenuView
    private lateinit var database: DatabaseReference
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var bannerPager: ViewPager2
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Inisialisasi view
        shimmerFrameLayout = view.findViewById(R.id.shimmer_container)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerViewMenu = view.findViewById(R.id.recyclerViewMenu)
        recyclerView2 = view.findViewById(R.id.recyclerView2)
        recyclerView3 = view.findViewById(R.id.recyclerView3)
        database = FirebaseDatabase.getInstance().reference.child("keluhan")

        setupBannerPager(view)

        // Mulai efek shimmer
        shimmerFrameLayout.startShimmer()

        // Setting up Horizontal RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = HorizontalRecyclerView(emptyList())
        recyclerView.adapter = adapter
        adapter.setItemClickListener(this)

        recyclerView2.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter2 = HorizontalBahan(requireContext())
        recyclerView2.adapter = adapter2

        recyclerView3.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter3 = HorizontalBahan(requireContext())
        recyclerView3.adapter = adapter3

        // Setting up Menu RecyclerView
        recyclerViewMenu.layoutManager = LinearLayoutManager(requireContext())
        menuAdapter = MyMenuView(this)
        recyclerViewMenu.adapter = menuAdapter

        // Fetch data from Firebase
        fetchDataFromFirebase()
        retrieveDataFromFirebase()
        retrieveUnhealthyBahanDataFromFirebase()
        retrieveUnhealthyBahanDataFromFirebase2()

        // Set click listeners for text views
        val textBahan1 = view.findViewById<TextView>(R.id.all_1)
        val textBahan2 = view.findViewById<TextView>(R.id.all_2)

        textBahan1.setOnClickListener {
            findNavController().navigate(R.id.bahanFragment1)
        }

        textBahan2.setOnClickListener {
            findNavController().navigate(R.id.bahanFragment2)
        }

        // Navigasi ke UpResepFragment1
        val altUpresepButton = view.findViewById<Button>(R.id.alt_upresep)
        altUpresepButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("selectedTab", 1)  // Menentukan tab yang dipilih (0 untuk TabResep1)

            findNavController().navigate(R.id.resepFragment, bundle)
        }


        // Navigasi ke CategoryResepFragment
        val btnAll = view.findViewById<Button>(R.id.btn_all)
        btnAll.setOnClickListener {
            findNavController().navigate(R.id.categoryResepFragment)
        }

        // Navigasi ke HealthNewsFragment
        val cekFoodieFolio = view.findViewById<TextView>(R.id.cek_foodiefolio)
        cekFoodieFolio.setOnClickListener {
            findNavController().navigate(R.id.healthNewsFragment)
        }

        return view
    }

    private fun setupBannerPager(view: View) {
        bannerPager = view.findViewById(R.id.banner_pager)

        // Konfigurasi banner images
        val bannerImages = listOf(
            R.drawable.foodiefolio_home,
            R.drawable.foodiefolio_home2,
            R.drawable.foodiefolio_home3,
            R.drawable.foodiefolio_home4,
            R.drawable.foodiefolio_home5,
            R.drawable.foodiefolio_home6
        )

        // Setup adapter
        bannerAdapter = BannerAdapter(bannerImages)
        bannerPager.adapter = bannerAdapter

        // Setup auto-sliding
        setupAutoSliding(bannerImages)
    }

    private fun setupAutoSliding(bannerImages: List<Int>) {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val nextItem = if (bannerPager.currentItem < bannerImages.size - 1) {
                    bannerPager.currentItem + 1
                } else {
                    0
                }

                bannerPager.setCurrentItem(nextItem, true)

                // Jalankan animasi setelah pindah slide
                animateBanner(nextItem)

                handler.postDelayed(this, 6000)
            }
        }
        handler.postDelayed(runnable, 6000)
    }


    private fun animateBanner(position: Int) {
        val viewHolder = bannerPager.getChildAt(0) // Ambil view saat ini
        viewHolder?.let {
            val render = Render(it.context)
            render.setAnimation(Slide().InDown(it))
            render.setDuration(800)
            render.start()
        }
    }


    private fun fetchDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<ItemModel>()
                for (data in snapshot.children) {
                    val imageURL = data.child("imageURL").getValue(String::class.java) ?: ""
                    val diseases = data.child("diseases").getValue(String::class.java) ?: ""
                    val item = ItemModel(imageURL, diseases)
                    items.add(item)
                }
                // Update Horizontal RecyclerView adapter with fetched data
                adapter.setItems(items)
                stopShimmerAndShowContent()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                stopShimmerAndShowContent()
            }
        })
    }

    private fun retrieveDataFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("resep")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menuList = mutableListOf<MenuModel>()
                for (menuSnapshot in snapshot.children) {
                    val menu = menuSnapshot.getValue(MenuModel::class.java)
                    menu?.let {
                        menuList.add(it)
                    }
                }

                // Urutkan berdasarkan likeCount (dari yang terbanyak)
                val sortedMenuList = menuList.sortedByDescending { it.likeCount }

                // Ambil 3 item teratas
                val top5MenuList = sortedMenuList.take(5)

                // Update adapter RecyclerView
                menuAdapter.setData(top5MenuList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun retrieveUnhealthyBahanDataFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("bahan").orderByChild("jenis").equalTo("sehat")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bahanList = mutableListOf<BahanModel>()
                for (bahanSnapshot in snapshot.children) {
                    val bahan = bahanSnapshot.getValue(BahanModel::class.java)
                    bahan?.let {
                        bahanList.add(it)
                    }
                }
                // Update Unhealthy Bahan RecyclerView adapter with fetched data
                val bahanAdapter = recyclerView2.adapter as? HorizontalBahan
                bahanAdapter?.setData(bahanList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun retrieveUnhealthyBahanDataFromFirebase2() {
        val ref = FirebaseDatabase.getInstance().getReference("bahan").orderByChild("jenis").equalTo("tidak")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bahanList = mutableListOf<BahanModel>()
                for (bahanSnapshot in snapshot.children) {
                    val bahan = bahanSnapshot.getValue(BahanModel::class.java)
                    bahan?.let {
                        bahanList.add(it)
                    }
                }
                // Update Unhealthy Bahan RecyclerView adapter with fetched data
                val bahanAdapter = recyclerView3.adapter as? HorizontalBahan
                bahanAdapter?.setData(bahanList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun stopShimmerAndShowContent() {
        shimmerFrameLayout.stopShimmer()
        shimmerFrameLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        recyclerViewMenu.visibility = View.VISIBLE
        recyclerView2.visibility = View.VISIBLE
        recyclerView3.visibility = View.VISIBLE
    }

    override fun onMenuClicked(menuId: String) {
        // Redirect to MainResepFragment and pass the menu name
        val bundle = Bundle().apply {
            putString("menuId", menuId)
        }
        findNavController().navigate(R.id.action_berandaFragment_to_mainResepFragment, bundle)
    }

    override fun onItemClick(item: ItemModel) {
        // Handle item click event for Horizontal RecyclerView
        val bundle = Bundle().apply {
            putString("diseases", item.diseases)
        }
        findNavController().navigate(R.id.mainKeluhanFragment, bundle)
    }
}