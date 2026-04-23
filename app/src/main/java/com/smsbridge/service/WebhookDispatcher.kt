package com.smsbridge.service

import android.content.Context
import android.util.Log
import com.smsbridge.SMSBridgeApp
import com.smsbridge.data.db.entity.DeliveryLogEntity
import com.smsbridge.data.db.entity.DeliveryStatus
import com.smsbridge.data.repository.SmsRepository
import com.smsbridge.data.repository.WebhookRepository
import com.smsbridge.util.FilterMatcher
import com.smsbridge.util.NotificationHelper
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Core forwarding engine. Called from SmsForegroundService on each incoming SMS.
 * For every enabled webhook whose filters match, it:
 *   1. Creates a delivery log entry
 *   2. Attempts HTTP POST with configured headers
 *   3. Retries up to webhook.retryCount times with exponential back-off
 *   4. Updates log status (SUCCESS / FAILED) and shows a notification on final failure
 */
class WebhookDispatcher(private val context: Context) {

    private val app = context.applicationContext as SMSBridgeApp
    private val db = app.database
    private val webhookRepo = WebhookRepository(
        db.webhookDao(), db.filterRuleDao(), db.webhookHeaderDao(),
        com.smsbridge.util.SecureStorage(context)
    )
    private val smsRepo = SmsRepository(db.messageDao(), db.deliveryLogDao())

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun dispatch(sender: String, body: String) {
        val webhooks = webhookRepo.getEnabledWebhooks()
        for (webhook in webhooks) {
            val rules = webhookRepo.getFilters(webhook.id)
            if (!FilterMatcher.matches(webhook, rules, sender, body)) continue

            val logId = smsRepo.insertLog(
                DeliveryLogEntity(
                    webhookId = webhook.id,
                    webhookName = webhook.name,
                    sender = sender,
                    bodyPreview = body.take(120)
                )
            )

            // Construct with the DB-assigned ID directly — no round-trip query needed
            var log = DeliveryLogEntity(
                id = logId,
                webhookId = webhook.id,
                webhookName = webhook.name,
                sender = sender,
                bodyPreview = body.take(120)
            )

            var success = false
            var lastCode = 0
            var lastError: String? = null

            repeat(webhook.retryCount) { attempt ->
                if (success) return@repeat
                val attemptNum = attempt + 1

                log = log.copy(status = DeliveryStatus.RETRYING, attempts = attemptNum, lastAttemptAt = System.currentTimeMillis())
                smsRepo.updateLog(log)

                try {
                    val headers = webhookRepo.getHeaders(webhook.id)
                    val payload = buildPayload(sender, body)

                    val requestBuilder = Request.Builder().url(webhook.url)
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))

                    headers.forEach { header ->
                        val value = webhookRepo.decryptHeaderValue(header.encryptedValue)
                        if (header.key.isNotBlank() && value.isNotBlank()) {
                            requestBuilder.addHeader(header.key, value)
                        }
                    }

                    client.newCall(requestBuilder.build()).execute().use { response ->
                        lastCode = response.code
                        if (response.isSuccessful) {
                            success = true
                        } else {
                            lastError = "HTTP ${response.code}"
                        }
                    }
                } catch (e: Exception) {
                    lastError = e.message ?: "Unknown error"
                    Log.w("WebhookDispatcher", "Attempt $attemptNum failed for ${webhook.name}: $lastError")
                }

                if (!success && attempt < webhook.retryCount - 1) {
                    delay(webhook.retryDelayMs * (attempt + 1))
                }
            }

            val finalStatus = if (success) DeliveryStatus.SUCCESS else DeliveryStatus.FAILED
            smsRepo.updateLog(log.copy(status = finalStatus, responseCode = lastCode, errorMessage = lastError))

            if (!success) {
                NotificationHelper.showForwardFailure(
                    context, webhook.name, sender,
                    NotificationHelper.NOTIF_FAILURE_BASE_ID + webhook.id.toInt()
                )
            }
        }
    }

    private fun buildPayload(sender: String, body: String): JSONObject = JSONObject().apply {
        put("sender", sender)
        put("body", body)
        put("timestamp", System.currentTimeMillis())
        put("device_id", android.provider.Settings.Secure.getString(
            context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        ))
    }
}
