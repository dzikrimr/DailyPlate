package com.myappproj.healthapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.myappproj.healthapp.screens.FirstScreen
import com.myappproj.healthapp.screens.SecondScreen
import com.myappproj.healthapp.screens.ThirdScreen
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class OnBoardingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment ini
        val view = inflater.inflate(R.layout.fragment_on_boarding, container, false)

        // Daftar fragment untuk ditampilkan di ViewPager
        val fragmentList = arrayListOf<Fragment>(
            FirstScreen(),
            SecondScreen(),
            ThirdScreen()
        )

        // Buat adapter untuk ViewPager
        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        // Inisialisasi ViewPager dan atur adapter
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        viewPager.adapter = adapter

        // Inisialisasi indikator titik untuk ViewPager
        val indicator = view.findViewById<DotsIndicator>(R.id.dots_indicator)
        indicator.attachTo(viewPager)

        return view
    }
}
