package com.adigium.androidrfb.rfb.test

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.Socket
import java.net.URI

class ClientWebSocket(val connectionAddress: String): WebSocketClient(URI(connectionAddress)) {
//    var isServerVersionHandled = false
//    var isServerSecurityHandled = false
//    var isAuthenticationHandled = false
//    var isClientInitHandled = false
//    var isServerInitHandled = false

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("ClientWebSocket", "Connection opened!")
    }

    override fun onMessage(message: String?) {
        Log.d("ClientWebSocket", "Received message: $message")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("ClientWebSocket", "Connection closed. Code - $code; reason - $reason; remote - $remote")
    }

    override fun onError(ex: Exception?) {
        Log.d("ClientWebSocket", "Error occurred: ${ex.toString()}")
    }
}