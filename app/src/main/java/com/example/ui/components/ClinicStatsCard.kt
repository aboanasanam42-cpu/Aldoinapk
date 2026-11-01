package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClinicAccentCyan
import com.example.ui.theme.ClinicAccentGreen
import com.example.ui.theme.ClinicAccentRed
import com.example.ui.theme.ClinicBlue
import com.example.ui.theme.ClinicCardBorder
import com.example.ui.theme.ClinicNavy
import com.example.ui.theme.ClinicNavyDark
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.ui.theme.ClinicWhite

@Composable
fun ClinicStatsCard(
    totalRemaining: Double,
    totalPaid: Double,
    patientCount: Int,
    scheduledCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // First Row: Remaining Debts & Paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatItem(
                    title = "متبقي الديون",
                    value = "%,.0f".format(totalRemaining),
                    unit = "د.ع/ريال",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = ClinicAccentRed,
                    backgroundColor = ClinicAccentRed.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    title = "إجمالي المسدد",
                    value = "%,.0f".format(totalPaid),
                    unit = "د.ع/ريال",
                    icon = Icons.Default.CheckCircle,
                    iconTint = ClinicAccentGreen,
                    backgroundColor = ClinicAccentGreen.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Second Row: Patients Count & Scheduled Reminders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatItem(
                    title = "سجلات المرضى",
                    value = patientCount.toString(),
                    unit = "مريض",
                    icon = Icons.Default.People,
                    iconTint = ClinicBlue,
                    backgroundColor = ClinicBlue.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    title = "الرسائل المجدولة",
                    value = scheduledCount.toString(),
                    unit = "تذكير نشط",
                    icon = Icons.Default.Schedule,
                    iconTint = ClinicAccentCyan,
                    backgroundColor = ClinicAccentCyan.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(ClinicWhite, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ClinicTextSecondary,
                        fontSize = 11.sp
                    )
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicTextPrimary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.size(3.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}
