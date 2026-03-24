package com.example.a509lablearnandroid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class SensorActivity : ComponentActivity() {
    private val viewModel: SensorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SensorScreen(viewModel)
        }
    }
}

@Composable
fun SensorScreen(viewModel: SensorViewModel) {
    val context = LocalContext.current
    val sensorValues by viewModel.sensorData.collectAsState()
    val locationValues by viewModel.locationData.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                              permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            viewModel.startLocationTracking()
        } else {
            Toast.makeText(context, "Location Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        viewModel.startListening()
        if (hasLocationPermission) {
            viewModel.startLocationTracking()
        }
        onDispose {
            viewModel.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Accelerometer
        Text(text = "Accelerometer Data", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "X Axis: ${"%.2f".format(sensorValues[0])}", fontSize = 20.sp)
        Text(text = "Y Axis: ${"%.2f".format(sensorValues[1])}", fontSize = 20.sp)
        Text(text = "Z Axis: ${"%.2f".format(sensorValues[2])}", fontSize = 20.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Location
        Text(text = "GPS Location Data", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (hasLocationPermission) {
            Text(text = "Latitude: ${locationValues.first}", fontSize = 20.sp)
            Text(text = "Longitude: ${locationValues.second}", fontSize = 20.sp)
        } else {
            Text(text = "No Permission", fontSize = 20.sp, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }) {
                Text("Request Location Permission")
            }
        }
    }
}
