package com.example.a509lablearnandroid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorTracker = SensorTracker(application)
    private val locationTracker = LocationTracker(application)

    // Accelerometer Data (X, Y, Z)
    private val _sensorData = MutableStateFlow(FloatArray(3) { 0f })
    val sensorData: StateFlow<FloatArray> = _sensorData.asStateFlow()

    // Location Data (Lat, Lng)
    private val _locationData = MutableStateFlow(Pair(0.0, 0.0))
    val locationData: StateFlow<Pair<Double, Double>> = _locationData.asStateFlow()

    fun startListening() {
        sensorTracker.startTracking { newValues ->
            _sensorData.value = newValues
        }
    }

    fun startLocationTracking() {
        locationTracker.startTracking { lat, lng ->
            _locationData.value = Pair(lat, lng)
        }
    }

    fun stopListening() {
        sensorTracker.stopTracking()
        locationTracker.stopTracking()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
