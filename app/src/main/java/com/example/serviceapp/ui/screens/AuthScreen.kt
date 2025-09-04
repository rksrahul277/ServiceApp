package com.example.serviceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.serviceapp.firebase.FirebaseRepository

@Composable
fun AuthScreen(role: String = "customer", onSignedIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val repo = remember { FirebaseRepository() }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Sign up / Sign in as $role")
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                loading = true
                CoroutineScope(Dispatchers.IO).launch {
                    val res = repo.signUp(email, password, role)
                    loading = false
                    if (res.isSuccess) onSignedIn()
                }
            }) { Text("Sign Up") }
            Button(onClick = {
                loading = true
                CoroutineScope(Dispatchers.IO).launch {
                    val res = repo.signIn(email, password)
                    loading = false
                    if (res.isSuccess) onSignedIn()
                }
            }) { Text("Sign In") }
        }
    }
}