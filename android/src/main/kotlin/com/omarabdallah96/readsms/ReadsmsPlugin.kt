package com.omarabdallah96.readsms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import android.provider.Telephony
// IntentFilter
import android.content.IntentFilter

class ReadsmsPlugin : FlutterPlugin, EventChannel.StreamHandler, BroadcastReceiver(), ActivityAware {
    private var channel: EventChannel? = null
    private var eventSink: EventChannel.EventSink? = null
    private lateinit var context: Context
    private lateinit var activity: Activity

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        context.registerReceiver(this, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
        channel = EventChannel(flutterPluginBinding.binaryMessenger, "readsms")
        channel?.setStreamHandler(this)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val bundle: Bundle? = intent.extras

            if (bundle != null) {
                for (sms in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    val serviceCenterAddress = getServiceCenterAddress(sms.pdu)

                    var data = listOf(
                        sms.displayMessageBody,
                        sms.originatingAddress.toString(),
                        sms.timestampMillis.toString(),
                        serviceCenterAddress
                    )
                    eventSink?.success(data)
                }
            }
            else {
                for (sms in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    val serviceCenterAddress = "no"

                    var data = listOf(
                        sms.displayMessageBody,
                        sms.originatingAddress.toString(),
                        sms.timestampMillis.toString(),
                        serviceCenterAddress
                    )
                    eventSink?.success(data)
                }
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = null
        eventSink = null
    }

    override fun onAttachedToActivity(p0: ActivityPluginBinding) {
        activity = p0.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // Handle as needed
    }

    override fun onReattachedToActivityForConfigChanges(p0: ActivityPluginBinding) {
        // Handle as needed
    }

    override fun onDetachedFromActivity() {
        // Handle as needed
    }

    private fun getServiceCenterAddress(pdu: ByteArray): String {
        val smsMessage = SmsMessage.createFromPdu(pdu)
        return smsMessage.serviceCenterAddress ?: "no"
    }
}
