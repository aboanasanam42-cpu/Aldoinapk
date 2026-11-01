package com.example.receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SEND_SCHEDULED_SMS = "com.example.ACTION_SEND_SCHEDULED_SMS"
        const val EXTRA_DEBT_ID = "debt_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_AMOUNT = "amount"
        private const val CHANNEL_ID = "clinic_debts_notifications"
        private const val TAG = "SmsAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val debtId = intent.getLongExtra(EXTRA_DEBT_ID, -1L)
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME) ?: "المريض"
        val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
        val messageText = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)

        createNotificationChannel(context)

        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            val dao = database.patientDebtDao()

            var sentSuccessfully = false
            var failureReason: String? = null

            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            if (phoneNumber.isNotBlank() && messageText.isNotBlank()) {
                if (hasSmsPermission) {
                    try {
                        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.getSystemService(SmsManager::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            SmsManager.getDefault()
                        }

                        val parts = smsManager.divideMessage(messageText)
                        if (parts.size > 1) {
                            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                        } else {
                            smsManager.sendTextMessage(phoneNumber, null, messageText, null, null)
                        }
                        sentSuccessfully = true
                        Log.d(TAG, "SMS successfully dispatched to $phoneNumber")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending SMS: ${e.message}", e)
                        failureReason = e.message
                    }
                } else {
                    failureReason = "إذن إرسال الرسائل (SEND_SMS) غير مفعّل"
                }
            } else {
                failureReason = "رقم الهاتف أو نص الرسالة غير متوفر"
            }

            if (sentSuccessfully && debtId > 0) {
                dao.updateSentStatus(debtId, true)
            }

            // Show Notification to doctor
            showNotification(
                context = context,
                debtId = debtId,
                patientName = patientName,
                phoneNumber = phoneNumber,
                messageText = messageText,
                sentSuccessfully = sentSuccessfully,
                failureReason = failureReason
            )
        }
    }

    private fun showNotification(
        context: Context,
        debtId: Long,
        patientName: String,
        phoneNumber: String,
        messageText: String,
        sentSuccessfully: Boolean,
        failureReason: String?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            (debtId * 10).toInt(),
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (sentSuccessfully) {
            "تم إرسال تذكير عيادة الرحمن بنجاح"
        } else {
            "موعد تذكير دين عيادة الرحمن: $patientName"
        }

        val body = if (sentSuccessfully) {
            "تم إرسال الرسالة إلى $patientName ($phoneNumber)"
        } else {
            "لم يتم الإرسال التلقائي: ${failureReason ?: "يرجى الإرسال يدوياً عبر واتساب أو SMS"}"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\nنص الرسالة: $messageText"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)

        // Add Quick WhatsApp Action button if not sent or fallback
        if (phoneNumber.isNotBlank()) {
            val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
            val waUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(messageText)}")
            val waIntent = Intent(Intent.ACTION_VIEW, waUri)
            val waPendingIntent = PendingIntent.getActivity(
                context,
                (debtId * 10 + 1).toInt(),
                waIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.sym_action_chat, "فتح واتساب", waPendingIntent)
        }

        notificationManager.notify((debtId.takeIf { it > 0 } ?: System.currentTimeMillis()).toInt(), builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تنبيهات ديون عيادة الرحمن"
            val descriptionText = "إشعارات تذكير المرضى بمواعيد وسداد الديون"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
