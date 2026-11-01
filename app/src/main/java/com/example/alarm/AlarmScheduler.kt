package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.PatientDebt
import com.example.receiver.SmsAlarmReceiver

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleDebtReminder(context: Context, debt: PatientDebt) {
        if (debt.scheduledDateTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Scheduled time is in the past, skipping alarm.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, SmsAlarmReceiver::class.java).apply {
            action = SmsAlarmReceiver.ACTION_SEND_SCHEDULED_SMS
            putExtra(SmsAlarmReceiver.EXTRA_DEBT_ID, debt.id)
            putExtra(SmsAlarmReceiver.EXTRA_PATIENT_NAME, debt.patientName)
            putExtra(SmsAlarmReceiver.EXTRA_PHONE_NUMBER, debt.phoneNumber)
            putExtra(SmsAlarmReceiver.EXTRA_MESSAGE, debt.scheduledMessage)
            putExtra(SmsAlarmReceiver.EXTRA_AMOUNT, debt.remainingAmount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            debt.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        debt.scheduledDateTime,
                        pendingIntent
                    )
                } else {
                    // Fallback to non-exact if permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        debt.scheduledDateTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    debt.scheduledDateTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for debt id ${debt.id} at ${debt.scheduledDateTime}")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm: ${e.message}")
        }
    }

    fun cancelDebtReminder(context: Context, debtId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SmsAlarmReceiver::class.java).apply {
            action = SmsAlarmReceiver.ACTION_SEND_SCHEDULED_SMS
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            debtId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for debt id $debtId")
        }
    }
}
