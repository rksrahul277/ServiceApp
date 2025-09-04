package com.example.serviceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onContinue: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Service Booking App", modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { onContinue("customer") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Text("I am a Customer") }
        Button(onClick = { onContinue("worker") }, modifier = Modifier.fillMaxWidth()) { Text("I am a Worker") }
    }
}