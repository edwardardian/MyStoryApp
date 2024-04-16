package com.example.mystoryapp

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.paging.PagingConfig
import com.example.mystoryapp.data.response.StoryPagingSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mystoryapp.databinding.ActivityMapsBinding
import com.example.mystoryapp.model.MapsStoryViewModel
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.database.StoryRepository
import com.example.mystoryapp.model.MapsStoryViewModelFactory
import com.example.mystoryapp.preferences.UserPreference
import com.example.mystoryapp.preferences.datastore
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val TOKEN = "auth_token"
        const val TAG = "MapsActivity"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsStoryViewModel: MapsStoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val userPreference = UserPreference.getInstance(applicationContext.datastore)

        userPreference.getAuthToken().asLiveData().observe(this) { token ->
            if (token != null) {
                val storyRepository = StoryRepository(ApiConfig.getApiService(token.toString()), PagingConfig(StoryPagingSource.INITIAL_PAGE_INDEX))

                val viewModelFactory = MapsStoryViewModelFactory(storyRepository)

                mapsStoryViewModel =
                    ViewModelProvider(this, viewModelFactory).get(MapsStoryViewModel::class.java)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        showAllMarkers()
        setMapStyle()
    }

    private fun showAllMarkers() {
        val builder = LatLngBounds.Builder()

        mapsStoryViewModel.storiesWithLocation.observe(this) { stories ->
            stories?.forEach { story ->
                val lat = story.lat ?: 0.0
                val lon = story.lon ?: 0.0

                val location = if (lat != 0.0 && lon != 0.0) {
                    LatLng(lat, lon)
                } else {
                    LatLng(0.0, 0.0)
                }

                mMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(story.name)
                        .snippet(story.description)
                )

                builder.include(location)
            }

            val bounds = builder.build()

            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels,
                    300
                )
            )
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }
}
