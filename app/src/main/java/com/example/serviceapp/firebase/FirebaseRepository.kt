package com.example.serviceapp.firebase

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.example.serviceapp.data.Order
import com.example.serviceapp.data.UserProfile
import com.example.serviceapp.data.ChatMessage

class FirebaseRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    suspend fun signUp(email: String, password: String, role: String): Result<String> {
        return try {
            val res = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = res.user?.uid ?: throw Exception("No UID")
            val profile = UserProfile(uid = uid, email = email, role = role)
            db.collection("users").document(uid).set(profile).await()
            Result.success(uid)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val res = auth.signInWithEmailAndPassword(email, password).await()
            val uid = res.user?.uid ?: throw Exception("No UID")
            Result.success(uid)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val docRef = db.collection("orders").add(order).await()
            Result.success(docRef.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun listenOpenOrders(onChange: (List<OrderWithId>) -> Unit) {
        db.collection("orders").whereEqualTo("status", "OPEN")
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                val list = snap?.documents?.mapNotNull { d ->
                    d.toObject(Order::class.java)?.let { o -> OrderWithId(id = d.id, order = o) }
                } ?: emptyList()
                onChange(list)
            }
    }

    suspend fun acceptOrder(orderId: String, workerId: String) : Result<Unit> {
        return try {
            db.collection("orders").document(orderId).update(mapOf(
                "status" to "ASSIGNED",
                "workerId" to workerId
            )).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String) : Result<Unit> {
        return try {
            db.collection("orders").document(orderId).update(mapOf(
                "status" to newStatus
            )).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun saveFcmToken(uid: String, token: String) : Result<Unit> {
        return try {
            db.collection("users").document(uid).update(mapOf("fcmToken" to token)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendMessage(orderId: String, msg: ChatMessage): Result<String> {
        return try {
            val ref = db.collection("orders").document(orderId).collection("messages").add(msg).await()
            Result.success(ref.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun listenMessages(orderId: String, onChange: (List<ChatWithId>) -> Unit) {
        db.collection("orders").document(orderId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                val list = snap?.documents?.mapNotNull { d ->
                    d.toObject(ChatMessage::class.java)?.let { m -> ChatWithId(id = d.id, message = m) }
                } ?: emptyList()
                onChange(list)
            }
    }

    // Placeholder: sending push notifications should be done via a trusted server or Cloud Function.
    suspend fun sendNotificationToToken(token: String, title: String, body: String) : Result<Unit> {
        return try {
            Result.failure(Exception("Client cannot send server-side notifications. Use Cloud Function or your server."))
        } catch (e: Exception) { Result.failure(e) }
    }
}

data class OrderWithId(val id: String, val order: Order)
data class ChatWithId(val id: String, val message: ChatMessage)