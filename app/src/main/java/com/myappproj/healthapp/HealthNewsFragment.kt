package com.myappproj.healthapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.myappproj.healthapp.adapter.NewsAdapter
import com.myappproj.healthapp.model.Article
import com.myappproj.healthapp.model.News
import com.myappproj.healthapp.util.NewsApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HealthNewsFragment : Fragment(), NewsItemClickListener {

    private lateinit var adapter: NewsAdapter
    private val newsList = mutableListOf<Article>()
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_health_news, container, false)

        // Inisialisasi ShimmerLayout dan RecyclerView
        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        recyclerView = view.findViewById(R.id.rvNews)

        // Setup RecyclerView
        adapter = NewsAdapter(requireContext(), newsList)
        recyclerView.adapter = adapter

        // Set LayoutManager
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        // Mulai shimmer
        shimmerLayout.startShimmer()

        // Mengambil data dari API
        fetchNewsData()

        // Set up tombol kembali
        val backButton = view.findViewById<ImageView>(R.id.back_arrow)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun fetchNewsData() {
        NewsApiClient.newsApi.getTopHeadlines("id", "health", "pub_66352250133b150d75e51a98d59ae7ebcb30a")
            .enqueue(object : Callback<News> {
                override fun onResponse(call: Call<News>, response: Response<News>) {
                    if (response.isSuccessful) {
                        val news = response.body()
                        if (news != null && news.results != null) {
                            newsList.clear()
                            newsList.addAll(news.results) // Tambahkan hasil artikel ke `newsList`
                            adapter.notifyDataSetChanged()
                        } else {
                            Log.e("HealthNewsFragment", "Results kosong atau null")
                        }
                    } else {
                        Log.e("HealthNewsFragment", "Response gagal: ${response.errorBody()?.string()}")
                    }

                    // Hentikan shimmer dan tampilkan RecyclerView
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }

                override fun onFailure(call: Call<News>, t: Throwable) {
                    Log.e("HealthNewsFragment", "Gagal mengambil data", t)

                    // Hentikan shimmer dan tampilkan RecyclerView
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            })
    }

    // Implementasi metode onNewsItemClicked sesuai yang dibutuhkan oleh interfacenya
    override fun onNewsItemClicked(url: String?) {
        if (url != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            requireActivity().startActivity(intent)  // Gunakan konteks aktivitas fragmen
        }
    }
}
// Definisi antarmuka (jika belum didefinisikan di tempat lain)
interface NewsItemClickListener {
    fun onNewsItemClicked(url: String?)
}
