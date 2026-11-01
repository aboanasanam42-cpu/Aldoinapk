package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * فئة كينونة المريض في قاعدة بيانات Room
 * تحتوي على الحقول المطلوبة:
 * @param id المعرف التلقائي
 * @param patientName اسم المريض
 * @param workType نوع العمل أو العلاج
 * @param amount المبلغ
 * @param phoneNumber رقم الهاتف
 * @param area منطقة السكن
 * @param scheduledMessage نص الرسالة المجدولة
 * @param scheduledDateTime تاريخ ووقت الجدولة بالملي ثانية
 * @param isSent حالة إرسال الرسالة (تم الإرسال أم لا)
 */
@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientName: String,
    val workType: String,
    val amount: Double,
    val phoneNumber: String,
    val area: String = "",
    val scheduledMessage: String = "",
    val scheduledDateTime: Long = 0L,
    val isSent: Boolean = false,
    val isPaid: Boolean = false,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Double
        get() = (amount - paidAmount).coerceAtLeast(0.0)

    val isPendingDebt: Boolean
        get() = !isPaid && remainingAmount > 0.0

    val hasScheduledReminder: Boolean
        get() = scheduledDateTime > 0L
}

/**
 * للتوافق مع واجهات التطبيق
 */
typealias PatientDebt = PatientEntity
