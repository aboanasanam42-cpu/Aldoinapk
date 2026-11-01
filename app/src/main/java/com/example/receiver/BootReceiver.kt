package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted. Rescheduling pending alarms...")
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val now = System.currentTimeMillis()
                val pendingAlarms = db.patientDebtDao().getPendingAlarms(now)
                for (debt in pendingAlarms) {
                    AlarmScheduler.scheduleDebtReminder(context, debt)
                }
                Log.d(TAG, "Rescheduled ${pendingAlarms.size} alarms successfully.")
            }
        }
    }
}
