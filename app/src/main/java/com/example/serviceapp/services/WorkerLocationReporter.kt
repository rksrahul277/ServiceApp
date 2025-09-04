package com.example.serviceapp.services

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class WorkerLocationReporter(private val context: Context) {
    private val flp = LocationServices.getFusedLocationProviderClient(context)
    private var callback: LocationCallback? = null

    fun startReporting(uid: String, intervalMs: Long = 15_000L) {
        val req = LocationRequest.Builder(intervalMs).setPriority(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY).build()
        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location = result.lastLocation ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = Firebase.firestore
                        db.collection("users").document(uid).update(mapOf("lat" to loc.latitude, "lng" to loc.longitude)).await()
                    } catch (_: Exception) {}
                }
            }
        }
        flp.requestLocationUpdates(req, callback!!, null)
    }

    fun stopReporting() {
        callback?.let { flp.removeLocationUpdates(it) }
        callback = null
    }
}