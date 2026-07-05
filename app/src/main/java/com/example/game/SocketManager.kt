package com.example.game

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class SocketManager(private val onMessageReceived: (event: String, data: String) -> Unit) {
    private val client = OkHttpClient.Builder()
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun connect(serverUrl: String) {
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d("SocketManager", "WebSocket Connection Opened")
                // Emit join message
                sendEvent("connect_success", "{}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    // Expect format: { "event": "...", "data": "..." }
                    val parts = text.split("|", limit = 2)
                    if (parts.size == 2) {
                        onMessageReceived(parts[0], parts[1])
                    } else {
                        onMessageReceived("message", text)
                    }
                } catch (e: Exception) {
                    Log.e("SocketManager", "Error parsing socket message: ${e.message}")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("SocketManager", "WebSocket Connection Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("SocketManager", "WebSocket Connection Failed: ${t.message}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        isConnected = false
    }

    fun isCurrentlyConnected(): Boolean = isConnected

    fun sendEvent(event: String, payloadJson: String) {
        if (isConnected) {
            webSocket?.send("$event|$payloadJson")
        }
    }
}
