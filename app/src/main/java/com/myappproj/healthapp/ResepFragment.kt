package com.myappproj.healthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class ResepFragment : Fragment() {

    interface OnTabChangedListener {
        fun onTabChanged(position: Int)
    }

    private var tabChangedListener: OnTabChangedListener? = null

    fun setOnTabChangedListener(listener: OnTabChangedListener) {
        tabChangedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_resep, container, false)

        val viewPager: ViewPager2 = view.findViewById(R.id.viewpager)

        // Atur adapter untuk ViewPager
        viewPager.adapter = ViewPagerAdapter(requireActivity())

        val selectedTab = arguments?.getInt("selectedTab", 0) ?: 0 // Default ke tab 0 (TabResep1)
        viewPager.setCurrentItem(selectedTab, false)

        // Atur listener untuk button 1
        val button1: Button = view.findViewById(R.id.button1)
        button1.setOnClickListener {
            viewPager.setCurrentItem(0, true)  // Ganti ke fragment 1
        }

        // Atur listener untuk button 2
        val button2: Button = view.findViewById(R.id.button2)
        button2.setOnClickListener {
            viewPager.setCurrentItem(1, true)  // Ganti ke fragment 2
        }

        updateHeader(view, selectedTab)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabChangedListener?.onTabChanged(position)
                updateHeader(view, position)  // Update header berdasarkan tab yang aktif
            }
        })

        return view
    }

    private fun updateHeader(view: View, position: Int) {
            val button1: Button = view.findViewById(R.id.button1)
            val button2: Button = view.findViewById(R.id.button2)

            when (position) {
                0 -> {
                    button1.setBackgroundResource(R.drawable.bg_tabselected)
                    button2.setBackgroundResource(R.drawable.bg_tab_no_selected)

                    button1.typeface = resources.getFont(R.font.sf_medium)
                    button2.typeface = resources.getFont(R.font.sf_regular)

                    // Ubah teks TextView menjadi "Resep Makanan"
                    view.findViewById<TextView>(R.id.page_resep).text = getString(R.string.resep_makanan)

                    // Tampilkan BottomNavigationView
                    (activity as? MainActivity)?.showBottomNavigation()
                }
                1 -> {
                    button2.setBackgroundResource(R.drawable.bg_tabselected)
                    button1.setBackgroundResource(R.drawable.bg_tab_no_selected)

                    button2.typeface = resources.getFont(R.font.sf_medium)
                    button1.typeface = resources.getFont(R.font.sf_regular)

                    // Ubah teks TextView menjadi "Resep Saya"
                    view.findViewById<TextView>(R.id.page_resep).text = getString(R.string.resep_saya)

                    // Sembunyikan BottomNavigationView saat TabResepFragment2 ditampilkan
                    (activity as? MainActivity)?.hideBottomNavigation()
                }
            }
        }

    // Adapter untuk ViewPager
    private class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TabResepFragment1()
                1 -> TabResepFragment2()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}