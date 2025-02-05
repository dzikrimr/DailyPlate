package com.myappproj.healthapp.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.myappproj.healthapp.R


class ThirdScreen : Fragment() {


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third_screen, container, false)

        val login = view.findViewById<Button>(R.id.btn_login)
        val daftar = view.findViewById<Button>(R.id.btn_daftar)


        login.setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingFragment_to_loginFragment)
        }

        daftar.setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingFragment_to_signUpFragment)
        }

        return view
    }


}
