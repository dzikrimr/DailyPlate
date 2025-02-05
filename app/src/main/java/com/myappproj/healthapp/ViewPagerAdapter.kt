package com.myappproj.healthapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(list: ArrayList<Fragment>, fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {
    // Daftar fragmen yang akan ditampilkan dalam ViewPager2
    private val fragmentList = list

    /**
     * Mengembalikan jumlah total fragmen dalam adapter.
     *
     * @return Jumlah total fragmen.
     */
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    /**
     * Membuat fragmen sesuai dengan posisi tertentu dalam ViewPager2.
     *
     * @param position Posisi fragmen dalam daftar.
     * @return Fragment yang dibuat.
     */
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}