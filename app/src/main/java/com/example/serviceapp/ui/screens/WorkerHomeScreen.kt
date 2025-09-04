package com.example.serviceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.serviceapp.firebase.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.serviceapp.firebase.OrderWithId
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch

@Composable
fun WorkerHomeScreen() {
    val repo = remember { FirebaseRepository() }
    var orders by remember { mutableStateOf(listOf<OrderWithId>()) }
    var filterSkill by remember { mutableStateOf("") }
    var skillsText by remember { mutableStateOf("") }
    var available by remember { mutableStateOf(false) }
    val auth = Firebase.auth
    val uid = auth.currentUser?.uid ?: "WORKER_ANON"

    LaunchedEffect(Unit) {
        repo.listenOpenOrders { list -> orders = list }
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Open Orders", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Available")
                Switch(checked = available, onCheckedChange = { checked ->
                    available = checked
                    CoroutineScope(Dispatchers.IO).launch {
                        val skills = if (skillsText.isBlank()) emptyList<String>() else skillsText.split(",").map { it.trim() }
                        try {
                            val db = com.google.firebase.firestore.ktx.firestore
                            db.collection("users").document(uid).update(mapOf("available" to available, "skills" to skills)).await()
                        } catch (_: Exception) {}
                    }
                    // start/stop location reporting
                    try {
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        val reporter = com.example.serviceapp.services.WorkerLocationReporter(ctx)
                        if (available) reporter.startReporting(uid) else reporter.stopReporting()
                    } catch (_: Exception) {}
                } )
            }
        }
        OutlinedTextField(value = skillsText, onValueChange = { skillsText = it }, label = { Text("Your skills (comma separated)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = filterSkill, onValueChange = { filterSkill = it }, label = { Text("Filter orders by skill") }, modifier = Modifier.fillMaxWidth())

        Divider()
        if (orders.isEmpty()) Text("No open orders")
        else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(orders) { o ->
                    if (filterSkill.isBlank() || o.order.serviceType.contains(filterSkill, ignoreCase = true)) {
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Service: ${o.order.serviceType}")
                                Text("Address: ${o.order.address}")
                                Text("Desc: ${o.order.description}")
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val res = repo.acceptOrder(o.id, uid)
                                        }
                                    }) { Text("Accept") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}