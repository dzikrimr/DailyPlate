package com.myappproj.healthapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myappproj.healthapp.adapter.HistoryAdapter
import com.myappproj.healthapp.databinding.FragmentSearchBinding

/**
 * Fragment untuk menampilkan tampilan pencarian dan histori pencarian.
 */
class SearchFragment : Fragment(), HistoryAdapter.OnItemClickListener {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var searchHistoryList: MutableList<String>
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var recyclerViewHistory: RecyclerView

    /**
     * Metode untuk membuat tampilan fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        searchHistoryList = mutableListOf()
        return binding.root
    }

    /**
     * Metode yang dipanggil setelah tampilan dibuat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi RecyclerView dan adapter untuk menampilkan histori pencarian
        recyclerViewHistory = binding.listviewHistory
        recyclerViewHistory.layoutManager = LinearLayoutManager(context)
        recyclerViewHistory.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        // Inisialisasi SharedPreferences untuk menyimpan histori pencarian
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        searchHistoryList = sharedPreferences.getStringSet(KEY_SEARCH_HISTORY, mutableSetOf())?.toMutableList() ?: mutableListOf()

        // Mengatur RecyclerView untuk menampilkan histori pencarian
        setupRecyclerView()

        // Mendengarkan inputan dari SearchView
        binding.searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        performSearch(it)
                        return true
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    /**
     * Metode untuk menyiapkan RecyclerView.
     */
    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        recyclerViewHistory.layoutManager = layoutManager
        historyAdapter = HistoryAdapter(requireContext(), searchHistoryList, this)
        recyclerViewHistory.adapter = historyAdapter
    }

    /**
     * Metode yang dipanggil ketika item histori pencarian diklik.
     */
    override fun onItemClick(item: String) {
        performSearch(item)
    }

    /**
     * Metode untuk menambahkan query pencarian ke histori pencarian.
     */
    private fun addToSearchHistory(query: String) {
        searchHistoryList.remove(query)
        searchHistoryList.add(0, query)

        if (searchHistoryList.size > MAX_HISTORY_ITEMS) {
            searchHistoryList.removeAt(MAX_HISTORY_ITEMS)
        }

        val editor = sharedPreferences.edit()
        editor.putStringSet(KEY_SEARCH_HISTORY, searchHistoryList.toSet())
        editor.apply()

        historyAdapter.notifyDataSetChanged()
    }

    /**
     * Metode untuk melakukan pencarian.
     */
    private fun performSearch(query: String) {
        addToSearchHistory(query)

        val action = R.id.action_searchFragment_to_searchHasilFragment
        val bundle = Bundle().apply {
            putString("query", query)
        }
        findNavController().navigate(action, bundle)
    }

    companion object {
        private const val PREFS_NAME = "SearchHistoryPrefs"
        private const val KEY_SEARCH_HISTORY = "searchHistory"
        private const val MAX_HISTORY_ITEMS = 10
    }
}
