package com.example.serviceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.serviceapp.firebase.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.serviceapp.firebase.ChatWithId
import com.example.serviceapp.data.ChatMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun OrderDetailScreen(orderId: String) {
    val repo = remember { FirebaseRepository() }
    var messages by remember { mutableStateOf(listOf<ChatWithId>()) }
    var text by remember { mutableStateOf("") }
    val auth = Firebase.auth
    val uid = auth.currentUser?.uid ?: "ANON"

    LaunchedEffect(orderId) {
        repo.listenMessages(orderId) { list -> messages = list }
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Chat & Actions", style = MaterialTheme.typography.titleMedium)
        LazyColumn(Modifier.weight(1f)) {
            items(messages) { m ->
                Card(Modifier.fillMaxWidth().padding(4.dp)) {
                    Column(Modifier.padding(8.dp)) {
                        Text(m.message.senderId + ": " + m.message.text)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f))
            Button(onClick = {
                val msg = ChatMessage(senderId = uid, text = text, timestamp = System.currentTimeMillis())
                CoroutineScope(Dispatchers.IO).launch {
                    repo.sendMessage(orderId, msg)
                }
                text = ""
            }) { Text("Send") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { CoroutineScope(Dispatchers.IO).launch { repo.updateOrderStatus(orderId, "IN_PROGRESS") } } ) { Text("Start") }
            Button(onClick = { CoroutineScope(Dispatchers.IO).launch { repo.updateOrderStatus(orderId, "COMPLETED") } } ) { Text("Complete") }
            Button(onClick = { CoroutineScope(Dispatchers.IO).launch { repo.updateOrderStatus(orderId, "CANCELED") } } ) { Text("Cancel") }
        }
    }
}