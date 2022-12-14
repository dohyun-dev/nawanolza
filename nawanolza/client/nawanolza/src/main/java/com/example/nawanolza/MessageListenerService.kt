package com.example.nawanolza

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class MessageListenerService : WearableListenerService() {
    private val tag = "MessageListenerService"

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.i(tag, "Message received: ${messageEvent.toString()}")
        broadcastMessage(messageEvent)
    }

    private fun broadcastMessage(messageEvent: MessageEvent?) {
        if(messageEvent == null) return

        val intent = Intent()
        intent.action = MessageConstants.intentName
        intent.putExtra(MessageConstants.message, String(messageEvent.data))
        intent.putExtra(MessageConstants.path, messageEvent.path)
        Log.i(tag, "Message Broadcasts")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}