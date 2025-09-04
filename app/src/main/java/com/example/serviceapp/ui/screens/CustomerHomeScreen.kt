package com.example.serviceapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.serviceapp.data.Order
import com.example.serviceapp.firebase.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@SuppressLint("MissingPermission")
@Composable
fun CustomerHomeScreen() {
    val ctx = LocalContext.current
    var service by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(0.0) }
    var lng by remember { mutableStateOf(0.0) }
    val repo = remember { FirebaseRepository() }
    val auth = Firebase.auth
    val uid = auth.currentUser?.uid ?: "ANON"

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            message = "Location permission granted — implement fused location retrieval in code."
        } else {
            message = "Location permission denied; order will be created without coords."
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Book a Service")
        OutlinedTextField(value = service, onValueChange = { service = it }, label = { Text("Service Type (e.g., Plumbing)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }) { Text("Allow location") }
            Button(onClick = {
                val order = Order(customerId = uid, serviceType = service, description = desc, address = address, lat = lat, lng = lng, status = "OPEN")
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    val res = repo.createOrder(order)
                    if (res.isSuccess) message = "Order created (id=${res.getOrNull()})"
                    else message = "Error: ${res.exceptionOrNull()?.localizedMessage}"
                }
            }, enabled = service.isNotBlank()) { Text("Book") }
        }
        if (message.isNotBlank()) Text(message)
        Spacer(Modifier.height(12.dp))
        Text("Note: Location capture is a placeholder. Add FusedLocationProviderClient to fully capture coordinates.")
    }
}