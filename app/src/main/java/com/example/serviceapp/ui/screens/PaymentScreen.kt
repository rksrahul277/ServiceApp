package com.example.serviceapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter

@Composable
fun PaymentScreen() {
    var amount by remember { mutableStateOf("") } // in rupees for UI; converted to paise
    var message by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (INR)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val serverBase = "http://YOUR_SERVER_DOMAIN" // replace with your deployed server URL
                    // fetch config
                    val cfgUrl = URL(serverBase + "/config")
                    val cfgConn = cfgUrl.openConnection() as HttpURLConnection
                    cfgConn.requestMethod = "GET"
                    if (cfgConn.responseCode == 200) {
                        val cfg = cfgConn.inputStream.bufferedReader().readText()
                        val cfgJson = JSONObject(cfg)
                        val keyId = cfgJson.optString("key_id", "")
                        val amtPaise = (amount.toDouble() * 100).toLong()
                        val req = JSONObject()
                        req.put("amount", amtPaise)
                        val url = URL(serverBase + "/create-order")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.doOutput = true
                        conn.setRequestProperty("Content-Type", "application/json")
                        val out = OutputStreamWriter(conn.outputStream)
                        out.write(req.toString())
                        out.flush()
                        out.close()
                        val code = conn.responseCode
                        if (code == 200) {
                            val resp = conn.inputStream.bufferedReader().readText()
                            val json = JSONObject(resp)
                            // start PaymentActivity with order json and keyId
                            val intent = Intent(ctx, Class.forName("com.example.serviceapp.PaymentActivity"))
                            intent.putExtra("order_json", json.toString())
                            intent.putExtra("key_id", keyId)
                            ctx.startActivity(intent)
                        } else { message = "Server error: $code" }
                    } else { message = "Could not fetch config from server" }
                } catch (e: Exception) { message = "Payment failed: " + e.localizedMessage }
            }
        }) { Text("Pay") }
        if (message.isNotBlank()) Text(message)
    }
}