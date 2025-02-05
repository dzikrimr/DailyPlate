package com.myappproj.healthapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myappproj.healthapp.adapter.MyMenuView
import com.myappproj.healthapp.model.MenuModel

class SavedMenuFragment : Fragment(), MyMenuView.MenuClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myMenuView: MyMenuView
    private lateinit var menuList: MutableList<MenuModel>
    private lateinit var emptyStateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_menu, container, false)

        // Initialize RecyclerView and adapter
        recyclerView = view.findViewById(R.id.recyclerview_mymenu)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        menuList = mutableListOf()
        myMenuView = MyMenuView(this)
        recyclerView.adapter = myMenuView

        // Initialize the empty state TextView
        emptyStateTextView = view.findViewById(R.id.textview_empty_state)

        return view
    }

    override fun onMenuClicked(menuId: String) {
        navigateToMainResepFragment(menuId)
    }

    override fun onResume() {
        super.onResume()
        fetchSavedRecipes()
    }

    private fun fetchSavedRecipes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val savedRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("savedRecipes")
            savedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val savedRecipeIds = snapshot.children.mapNotNull { it.key } // Get menuId
                    fetchRecipesDetails(savedRecipeIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal memuat resep disimpan", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchRecipesDetails(savedRecipeIds: List<String>) {
        val recipesRef = FirebaseDatabase.getInstance().getReference("resep")
        recipesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuList.clear()
                for (recipeSnapshot in snapshot.children) {
                    val menu = recipeSnapshot.getValue(MenuModel::class.java)
                    if (menu != null && savedRecipeIds.contains(recipeSnapshot.key)) { // Check with menuId
                        menuList.add(menu)
                    }
                }
                myMenuView.setData(menuList)

                // Show or hide the empty state TextView
                if (menuList.isEmpty()) {
                    emptyStateTextView.visibility = View.VISIBLE
                } else {
                    emptyStateTextView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal memuat detail resep", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToMainResepFragment(menuId: String) {
        val bundle = Bundle().apply {
            putString("menuId", menuId)
        }
        findNavController().navigate(R.id.action_savedMenuFragment_to_mainResepFragment, bundle)
    }
}

