package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientDebt
import com.example.ui.theme.ClinicAccentGreen
import com.example.ui.theme.ClinicAccentRed
import com.example.ui.theme.ClinicNavy
import com.example.ui.theme.ClinicNavyDark
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.ui.theme.ClinicWhite

@Composable
fun PartialPaymentDialog(
    patient: PatientDebt,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double) -> Unit,
    onMarkFullPaid: () -> Unit
) {
    var paymentInput by remember { mutableStateOf("%.0f".format(patient.remainingAmount)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسديد حساب: ${patient.patientName}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicNavyDark
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "المبلغ المتبقي الحالي: %,.0f د.ع/ريال".format(patient.remainingAmount),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ClinicAccentRed,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = paymentInput,
                    onValueChange = { paymentInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("المبلغ المدفوع الآن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ClinicAccentGreen)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partial_payment_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = paymentInput.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirmPayment(amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ClinicNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("تسجيل الدفعة", color = ClinicWhite)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onMarkFullPaid,
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicAccentGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ClinicWhite)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("سداد كامل", color = ClinicWhite)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إلغاء")
                }
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    patient: PatientDebt,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تأكيد حذف السجل",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicAccentRed
                )
            )
        },
        text = {
            Text(
                text = "هل أنت متأكد من حذف سجل المريض \"${patient.patientName}\" نهائياً وإلغاء أي رسائل مجدولة له؟",
                style = MaterialTheme.typography.bodyMedium.copy(color = ClinicTextPrimary)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = ClinicAccentRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ClinicWhite)
                Spacer(modifier = Modifier.width(4.dp))
                Text("حذف نهائي", color = ClinicWhite)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("إلغاء")
            }
        }
    )
}
