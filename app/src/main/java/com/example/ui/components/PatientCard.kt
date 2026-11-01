package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientDebt
import com.example.ui.theme.ClinicAccentCyan
import com.example.ui.theme.ClinicAccentGreen
import com.example.ui.theme.ClinicAccentRed
import com.example.ui.theme.ClinicBlue
import com.example.ui.theme.ClinicBlueLight
import com.example.ui.theme.ClinicCardBorder
import com.example.ui.theme.ClinicNavy
import com.example.ui.theme.ClinicNavyDark
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.ui.theme.ClinicWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientCard(
    patient: PatientDebt,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsPaid: () -> Unit,
    onPartialPaymentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    val isCleared = patient.isPaid || patient.remainingAmount <= 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("patient_card_${patient.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Patient Name & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isCleared) ClinicAccentGreen.copy(alpha = 0.15f) else ClinicNavy.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCleared) Icons.Default.CheckCircle else Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = if (isCleared) ClinicAccentGreen else ClinicNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = patient.patientName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicTextPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (patient.workType.isNotBlank()) {
                                Text(
                                    text = patient.workType,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ClinicBlue,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            if (patient.area.isNotBlank()) {
                                Text(
                                    text = "• ${patient.area}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ClinicTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCleared) ClinicAccentGreen.copy(alpha = 0.12f) else ClinicAccentRed.copy(alpha = 0.12f),
                    border = null
                ) {
                    Text(
                        text = if (isCleared) "مسدد بالكامل" else "متبقي دين",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCleared) ClinicAccentGreen else ClinicAccentRed,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Financial Summary Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ClinicBlueLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المبلغ الكلي",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "%,.0f".format(patient.amount),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ClinicTextPrimary
                        )
                    )
                }

                Column {
                    Text(
                        text = "المسدد",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "%,.0f".format(patient.paidAmount),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ClinicAccentGreen
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "المتبقي للسداد",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicAccentRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "%,.0f د.ع".format(patient.remainingAmount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCleared) ClinicAccentGreen else ClinicAccentRed,
                            fontSize = 15.sp
                        )
                    )
                }
            }

            // Scheduled Message Banner (if enabled)
            if (patient.hasScheduledReminder) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (patient.isSent) ClinicAccentGreen.copy(alpha = 0.08f) else ClinicAccentCyan.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (patient.isSent) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (patient.isSent) ClinicAccentGreen else ClinicAccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (patient.isSent) {
                            "تم إرسال التذكير تلقائياً"
                        } else {
                            "موعد إرسال التذكير: ${dateTimeFormat.format(Date(patient.scheduledDateTime))}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (patient.isSent) ClinicAccentGreen else ClinicNavyDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Call, SMS, WhatsApp, Pay, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Call Patient Button
                if (patient.phoneNumber.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${patient.phoneNumber}"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "اتصال",
                            tint = ClinicBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Direct SMS Button
                    OutlinedButton(
                        onClick = {
                            val smsText = patient.scheduledMessage.ifBlank {
                                "الأخ ${patient.patientName}، نود تذكيركم بمتبقي حساب عيادة الرحمن بمبلغ %,.0f د.ع.".format(patient.remainingAmount)
                            }
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${patient.phoneNumber}")).apply {
                                putExtra("sms_body", smsText)
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "رسالة SMS",
                            tint = ClinicNavy,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Direct WhatsApp Button
                    Button(
                        onClick = {
                            val waText = patient.scheduledMessage.ifBlank {
                                "الأخ ${patient.patientName}، نود تذكيركم بمتبقي حساب عيادة الرحمن بمبلغ %,.0f د.ع.".format(patient.remainingAmount)
                            }
                            openWhatsApp(context, patient.phoneNumber, waText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "واتساب",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ClinicWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Payment / Partial Payment Button
                if (!isCleared) {
                    FilledTonalButton(
                        onClick = onPartialPaymentClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ClinicAccentGreen.copy(alpha = 0.15f),
                            contentColor = ClinicAccentGreen
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تسديد",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = ClinicBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = ClinicAccentRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun openWhatsApp(context: Context, rawPhone: String, message: String) {
    val cleanPhone = rawPhone.filter { it.isDigit() || it == '+' }
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(browserIntent)
    }
}
