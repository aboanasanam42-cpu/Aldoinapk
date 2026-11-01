package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClinicAccentCyan
import com.example.ui.theme.ClinicAccentGreen
import com.example.ui.theme.ClinicBlue
import com.example.ui.theme.ClinicBlueLight
import com.example.ui.theme.ClinicCardBorder
import com.example.ui.theme.ClinicNavy
import com.example.ui.theme.ClinicNavyDark
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.ui.theme.ClinicWhite
import com.example.ui.viewmodel.DebtFormState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PatientFormSection(
    formState: DebtFormState,
    onNameChanged: (String) -> Unit,
    onWorkTypeChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onAddAmountPreset: (Double) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAreaChanged: (String) -> Unit,
    onScheduleToggled: (Boolean) -> Unit,
    onScheduleDateTimeChanged: (Long) -> Unit,
    onMessageChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault())

    val workTypeChips = listOf(
        "حشوة أسنان",
        "علاج عصب",
        "قلع ضرس",
        "تنظيف وتبييض",
        "تركيب تقويم",
        "زراعة أسنان",
        "فحص عام"
    )

    val amountPresets = listOf(
        5000.0 to "+5,000",
        10000.0 to "+10,000",
        20000.0 to "+20,000",
        50000.0 to "+50,000",
        100000.0 to "+100,000"
    )

    val areaChips = listOf(
        "صنعاء", "حدة", "الستين", "الحصبة", "شميلة", "مذبح", "تعز", "إب", "عدن"
    )

    fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance().apply {
            if (formState.scheduledDateTime > 0) {
                timeInMillis = formState.scheduledDateTime
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE))
                }

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        chosenCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        chosenCalendar.set(Calendar.MINUTE, minute)
                        chosenCalendar.set(Calendar.SECOND, 0)
                        onScheduleDateTimeChanged(chosenCalendar.timeInMillis)
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Form Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (formState.editingId != null) "تعديل بيانات الدين والمريض" else "إضافة مريض ودين جديد",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavyDark,
                        fontSize = 16.sp
                    )
                )

                if (formState.editingId != null) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إلغاء التعديل", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Patient Name Field
            OutlinedTextField(
                value = formState.patientName,
                onValueChange = onNameChanged,
                label = { Text("اسم المريض") },
                placeholder = { Text("أدخل اسم المريض كاملاً") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = ClinicNavy)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("patient_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClinicNavy,
                    unfocusedBorderColor = ClinicCardBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Work Type Field + Quick Chips
            OutlinedTextField(
                value = formState.workType,
                onValueChange = onWorkTypeChanged,
                label = { Text("نوع العمل / العلاج") },
                placeholder = { Text("مثال: حشوة تجميلية، زراعة، علاج عصب...") },
                leadingIcon = {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = ClinicBlue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("work_type_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClinicNavy,
                    unfocusedBorderColor = ClinicCardBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick work type chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (work in workTypeChips) {
                    val isSelected = formState.workType == work
                    FilterChip(
                        selected = isSelected,
                        onClick = { onWorkTypeChanged(work) },
                        label = { Text(work, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ClinicNavy,
                            selectedLabelColor = ClinicWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Amount Field + Quick Add Presets
            OutlinedTextField(
                value = formState.amount,
                onValueChange = onAmountChanged,
                label = { Text("مبلغ الدين / الإجمالي (د.ع/ريال)") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ClinicAccentGreen)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClinicNavy,
                    unfocusedBorderColor = ClinicCardBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Amount Presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for ((delta, label) in amountPresets) {
                    SuggestionChip(
                        onClick = { onAddAmountPreset(delta) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = ClinicBlueLight,
                            labelColor = ClinicNavyDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Phone Number Field
            OutlinedTextField(
                value = formState.phoneNumber,
                onValueChange = onPhoneChanged,
                label = { Text("رقم هاتف المريض (SMS / واتساب)") },
                placeholder = { Text("مثال: 771234567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = ClinicBlue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClinicNavy,
                    unfocusedBorderColor = ClinicCardBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Area Field + Quick Chips
            OutlinedTextField(
                value = formState.area,
                onValueChange = onAreaChanged,
                label = { Text("منطقة السكن / العنوان") },
                placeholder = { Text("مثال: صنعاء - حدة") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = ClinicNavy)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("area_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClinicNavy,
                    unfocusedBorderColor = ClinicCardBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (areaName in areaChips) {
                    val isSelected = formState.area == areaName
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAreaChanged(areaName) },
                        label = { Text(areaName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ClinicBlue,
                            selectedLabelColor = ClinicWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Scheduled Reminder Toggle and Settings
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicBlueLight.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = ClinicNavy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "جدولة رسالة تذكير تلقائية",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ClinicNavyDark
                                    )
                                )
                                Text(
                                    text = "إرسال SMS أو إشعار مباشر في الموعد المحدد",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ClinicTextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = formState.isScheduleEnabled,
                            onCheckedChange = onScheduleToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ClinicWhite,
                                checkedTrackColor = ClinicNavy
                            ),
                            modifier = Modifier.testTag("schedule_switch")
                        )
                    }

                    AnimatedVisibility(visible = formState.isScheduleEnabled) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            // Date & Time Picker Button
                            Button(
                                onClick = { showDateTimePicker() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicNavy),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pick_date_time_button")
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تاريخ ووقت الإرسال: ${if (formState.scheduledDateTime > 0) dateTimeFormat.format(Date(formState.scheduledDateTime)) else "اضغط للتحديد"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Message Input
                            OutlinedTextField(
                                value = formState.scheduledMessage,
                                onValueChange = onMessageChanged,
                                label = { Text("نص رسالة التذكير") },
                                minLines = 2,
                                maxLines = 4,
                                leadingIcon = {
                                    Icon(Icons.Default.Message, contentDescription = null, tint = ClinicNavy)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scheduled_message_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ClinicNavy,
                                    unfocusedBorderColor = ClinicCardBorder
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save / Submit Button
            Button(
                onClick = onSaveClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicNavyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_debt_button")
            ) {
                Icon(
                    imageVector = if (formState.editingId != null) Icons.Default.Edit else Icons.Default.Save,
                    contentDescription = null,
                    tint = ClinicWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (formState.editingId != null) "تحديث بيانات المريض والدين" else "حفظ المريض وجدولة التذكير",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicWhite,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}
