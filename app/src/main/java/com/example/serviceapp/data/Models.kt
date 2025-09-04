package com.example.serviceapp.data

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val role: String = "customer", // or "worker"
    val skills: List<String> = emptyList(), // for workers
    val available: Boolean = false, // worker availability
    val fcmToken: String? = null // for push notifications
)

data class Order(
    val customerId: String = "",
    val serviceType: String = "",
    val description: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val status: String = "OPEN", // OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELED
    val workerId: String? = null,
    val price: Double? = null,
    val scheduledTimeEpoch: Long? = null // optional scheduled time in epoch millis
)

data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)