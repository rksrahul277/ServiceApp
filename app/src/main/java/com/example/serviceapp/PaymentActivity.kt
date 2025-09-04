package com.example.serviceapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Toast

class PaymentActivity : ComponentActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Read intent extras
        val orderJson = intent.getStringExtra("order_json")
        val keyId = intent.getStringExtra("key_id") ?: ""
        if (orderJson == null) {
            finish(); return
        }
        val order = JSONObject(orderJson)
        val options = JSONObject()
        options.put("name", "ServiceApp")
        options.put("description", "Service Payment")
        options.put("order_id", order.optString("id"))
        options.put("currency", order.optString("currency", "INR"))
        options.put("amount", order.optLong("amount", 0))
        // You can pass prefill etc.
        try {
            val checkout = Checkout()
            checkout.setKeyID(keyId)
            checkout.open(this, options)
        } catch (e: Exception) {
            Log.e("PaymentActivity", "Error in starting Razorpay Checkout", e)
            finish()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String) {
        // Called when payment is successful. Notify server to verify signature if desired.
        Toast.makeText(this, "Payment Success: $razorpayPaymentID", Toast.LENGTH_LONG).show()
        // Optionally, call server /verify with order/payment/signature (client should send signature too - requires Checkout callbacks)
        finish()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment failed: $code $response", Toast.LENGTH_LONG).show()
        finish()
    }
}