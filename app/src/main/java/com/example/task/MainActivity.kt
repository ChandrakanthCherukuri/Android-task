package com.example.task

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.task.adapter.PlacesAdapter
import com.example.task.api.DirectionsApiService
import com.example.task.databinding.ActivityMainBinding
import com.example.task.utils.PolylineDecoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var directionsApiService: DirectionsApiService

    private var currentRoute: Polyline? = null
    private var sourceMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var sourceLocation: LatLng? = null
    private var destinationLocation: LatLng? = null

    // Replace with your actual Google Maps API key
    private val apiKey = "AIzaSyDemoKey-Replace-With-Your-Actual-API-Key"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializePlaces()
        initializeMap()
        setupUI()
        setupDirectionsApi()
    }

    private fun initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupDirectionsApi() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        directionsApiService = retrofit.create(DirectionsApiService::class.java)
    }

    private fun setupUI() {
        // Setup places adapter
        placesAdapter = PlacesAdapter { place ->
            fetchPlaceDetails(place)
            binding.rvSuggestions.visibility = android.view.View.GONE
        }

        binding.rvSuggestions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = placesAdapter
        }

        // Setup search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 > 2) {
                    searchPlaces(s.toString())
                } else {
                    binding.rvSuggestions.visibility = android.view.View.GONE
                }
            }
        })

        // Clear route button
        binding.btnClearRoute.setOnClickListener {
            clearRoute()
            binding.etSearch.text?.clear()
        }

        // My location button
        binding.fabMyLocation.setOnClickListener {
            getCurrentLocation()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Enable location if permission granted
        if (checkLocationPermission()) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }

        // Set map click listener for setting source and destination
        googleMap.setOnMapClickListener { latLng ->
            if (sourceLocation == null) {
                setSourceLocation(latLng)
            } else if (destinationLocation == null) {
                setDestinationLocation(latLng)
                drawRoute()
            } else {
                // Reset and set new source
                clearRoute()
                setSourceLocation(latLng)
            }
        }

        // Move camera to a default location (New York City)
        val defaultLocation = LatLng(40.7128, -74.0060)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }

    private fun searchPlaces(query: String) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                placesAdapter.updatePlaces(response.autocompletePredictions)
                binding.rvSuggestions.visibility = android.view.View.VISIBLE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error searching places: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchPlaceDetails(prediction: AutocompletePrediction) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let { latLng ->
                    moveToLocation(latLng, place.name ?: "Selected Place")
                }
                binding.etSearch.setText(place.name)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching place details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun moveToLocation(latLng: LatLng, title: String) {
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun setSourceLocation(latLng: LatLng) {
        sourceLocation = latLng
        sourceMarker?.remove()
        sourceMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Source")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        Toast.makeText(this, "Source set. Tap another location for destination.", Toast.LENGTH_SHORT).show()
    }

    private fun setDestinationLocation(latLng: LatLng) {
        destinationLocation = latLng
        destinationMarker?.remove()
        destinationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

    private fun drawRoute() {
        val source = sourceLocation
        val destination = destinationLocation

        if (source == null || destination == null) return

        lifecycleScope.launch {
            try {
                val originStr = "${source.latitude},${source.longitude}"
                val destinationStr = "${destination.latitude},${destination.longitude}"

                val response = directionsApiService.getDirections(
                    origin = originStr,
                    destination = destinationStr,
                    apiKey = apiKey
                )

                if (response.isSuccessful && response.body()?.status == "OK") {
                    val directionsResponse = response.body()!!
                    if (directionsResponse.routes.isNotEmpty()) {
                        val route = directionsResponse.routes[0]
                        val polylinePoints = PolylineDecoder.decode(route.overviewPolyline.points)

                        // Remove existing route
                        currentRoute?.remove()

                        // Draw new route
                        currentRoute = googleMap.addPolyline(
                            PolylineOptions()
                                .addAll(polylinePoints)
                                .color(Color.BLUE)
                                .width(8f)
                        )

                        // Show route info
                        val leg = route.legs[0]
                        Toast.makeText(
                            this@MainActivity,
                            "Distance: ${leg.distance.text}, Duration: ${leg.duration.text}",
                            Toast.LENGTH_LONG
                        ).show()

                        // Adjust camera to show entire route
                        val boundsBuilder = LatLngBounds.Builder()
                        polylinePoints.forEach { boundsBuilder.include(it) }
                        val bounds = boundsBuilder.build()
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to get directions", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearRoute() {
        currentRoute?.remove()
        sourceMarker?.remove()
        destinationMarker?.remove()
        currentRoute = null
        sourceMarker = null
        destinationMarker = null
        sourceLocation = null
        destinationLocation = null
        googleMap.clear()
    }

    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(currentLatLng)
                                .title("My Location")
                        )
                    }
                }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun enableMyLocation() {
        if (checkLocationPermission()) {
            googleMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
        }
    }
}