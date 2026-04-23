package com.smsbridge.util

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsUtils {

    fun sendSms(context: Context, address: String, body: String) {
        val smsManager = context.getSystemService(SmsManager::class.java)
        val parts = smsManager.divideMessage(body)
        if (parts.size == 1) {
            smsManager.sendTextMessage(address, null, body, null, null)
        } else {
            smsManager.sendMultipartTextMessage(address, null, parts, null, null)
        }
    }

    fun getContactName(context: Context, address: String): String {
        return try {
            val uri = android.net.Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(address)
            )
            val cursor: Cursor? = context.contentResolver.query(
                uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) it.getString(0) else address
            } ?: address
        } catch (e: Exception) {
            address
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
