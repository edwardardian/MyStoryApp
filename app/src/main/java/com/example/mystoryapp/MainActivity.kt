package com.example.mystoryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.DetailActivity.Companion.KEY_DETAIL
import com.example.mystoryapp.data.response.StoryPagingSource
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.database.StoryRepository
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.model.StoryViewModelFactory
import com.example.mystoryapp.preferences.UserPreference
import com.example.mystoryapp.preferences.datastore
import com.example.mystoryapp.ui.StoryAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var storyViewModel: StoryViewModel

    private val uploadLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            storyViewModel.getStories()
            binding.rvStory.smoothScrollToPosition(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = intent.getStringExtra(KEY_TOKEN)
        val storyRepository = StoryRepository(ApiConfig.getApiService(token!!), PagingConfig(StoryPagingSource.INITIAL_PAGE_INDEX))


        val viewModelFactory = StoryViewModelFactory(
            storyRepository,
            UserPreference.getInstance(applicationContext.datastore)
        )

        storyViewModel = ViewModelProvider(this, viewModelFactory).get(StoryViewModel::class.java)

        val rv = binding.rvStory
        storyAdapter = StoryAdapter()

        storyAdapter.onStoryClick = { story, view ->
            val detailContent = view.findViewById<CardView>(R.id.card_view)

            val iDetail = Intent(this@MainActivity, DetailActivity::class.java)
            iDetail.putExtra(KEY_DETAIL, story)

            val optionsCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@MainActivity,
                    androidx.core.util.Pair(detailContent, "detailContent"),
                )

            startActivity(iDetail, optionsCompat.toBundle())
        }

        observeViewModel()

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = storyAdapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, UploadActivity::class.java)
            intent.putExtra(KEY_TOKEN, token)
            uploadLauncher.launch(intent)
        }

        binding.toolbar.apply {
            inflateMenu(R.menu.main_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_refresh -> {
                        storyViewModel.getStories()
                        rv.smoothScrollToPosition(0)
                    }

                    R.id.menu_logout -> {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        storyViewModel.clearPreferences()
                        finishAffinity()
                        startActivity(intent)
                    }

                    R.id.map_menu -> {
                        val intent = Intent(this@MainActivity, MapsActivity::class.java)
                        startActivity(intent)
                    }
                }

                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun observeViewModel() {
        storyViewModel.apply {
            isLoading.observe(this@MainActivity) {
                showLoading(it)
            }

            storiesResponse.observe(this@MainActivity) { pagingData ->
                // Menggunakan submitData untuk mengirim PagingData ke adapter
                storyAdapter.submitData(lifecycle, pagingData)
            }

            errorMessage.observe(this@MainActivity) {
                showToast(it)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressbar.isVisible = isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val KEY_TOKEN = "key_token"
    }
}
