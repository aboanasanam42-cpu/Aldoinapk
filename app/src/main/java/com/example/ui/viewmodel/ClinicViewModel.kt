package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import com.example.data.model.PatientDebt
import com.example.data.repository.PatientDebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FilterTab(val title: String) {
    ALL("الكل"),
    UNPAID("متبقي"),
    PAID("مسدد"),
    SCHEDULED("مجدول")
}

data class ClinicUiState(
    val patients: List<PatientDebt> = emptyList(),
    val filteredPatients: List<PatientDebt> = emptyList(),
    val currentFilter: FilterTab = FilterTab.ALL,
    val searchQuery: String = "",
    val totalDebts: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val patientCount: Int = 0,
    val scheduledCount: Int = 0,
    val userMessage: String? = null,
    val isExporting: Boolean = false
)

data class DebtFormState(
    val editingId: Long? = null,
    val patientName: String = "",
    val workType: String = "",
    val amount: String = "",
    val phoneNumber: String = "",
    val area: String = "",
    val isScheduleEnabled: Boolean = false,
    val scheduledDateTime: Long = 0L,
    val scheduledMessage: String = "",
    val isMessageManuallyEdited: Boolean = false
)

class ClinicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PatientDebtRepository
    private val _uiState = MutableStateFlow(ClinicUiState())
    val uiState: StateFlow<ClinicUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(DebtFormState())
    val formState: StateFlow<DebtFormState> = _formState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PatientDebtRepository(database.patientDebtDao())

        // Default scheduled time: tomorrow at 10:00 AM
        val defaultCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        _formState.update { it.copy(scheduledDateTime = defaultCal.timeInMillis) }

        observePatients()
    }

    private fun observePatients() {
        viewModelScope.launch {
            repository.allPatients.collect { list ->
                updateListAndStats(list, _uiState.value.searchQuery, _uiState.value.currentFilter)
            }
        }
    }

    private fun updateListAndStats(
        list: List<PatientDebt>,
        query: String,
        filter: FilterTab
    ) {
        val filtered = list.filter { patient ->
            val matchesFilter = when (filter) {
                FilterTab.ALL -> true
                FilterTab.UNPAID -> patient.isPendingDebt
                FilterTab.PAID -> !patient.isPendingDebt
                FilterTab.SCHEDULED -> patient.hasScheduledReminder && !patient.isSent
            }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                patient.patientName.contains(query, ignoreCase = true) ||
                        patient.phoneNumber.contains(query) ||
                        patient.workType.contains(query, ignoreCase = true) ||
                        patient.area.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }

        val totalAmount = list.sumOf { it.amount }
        val totalPaid = list.sumOf { it.paidAmount }
        val totalRemaining = list.sumOf { it.remainingAmount }
        val scheduled = list.count { it.hasScheduledReminder && !it.isSent }

        _uiState.update {
            it.copy(
                patients = list,
                filteredPatients = filtered,
                totalDebts = totalAmount,
                totalPaid = totalPaid,
                totalRemaining = totalRemaining,
                patientCount = list.size,
                scheduledCount = scheduled
            )
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        updateListAndStats(_uiState.value.patients, newQuery, _uiState.value.currentFilter)
    }

    fun onFilterSelected(filter: FilterTab) {
        _uiState.update { it.copy(currentFilter = filter) }
        updateListAndStats(_uiState.value.patients, _uiState.value.searchQuery, filter)
    }

    // Form field update methods
    fun onNameChanged(name: String) {
        _formState.update { it.copy(patientName = name) }
        autoUpdateTemplateMessage()
    }

    fun onWorkTypeChanged(work: String) {
        _formState.update { it.copy(workType = work) }
        autoUpdateTemplateMessage()
    }

    fun onAmountChanged(amount: String) {
        val clean = amount.filter { it.isDigit() || it == '.' }
        _formState.update { it.copy(amount = clean) }
        autoUpdateTemplateMessage()
    }

    fun addAmountPreset(delta: Double) {
        val current = _formState.value.amount.toDoubleOrNull() ?: 0.0
        val newAmount = current + delta
        _formState.update { it.copy(amount = "%.0f".format(newAmount)) }
        autoUpdateTemplateMessage()
    }

    fun onPhoneChanged(phone: String) {
        _formState.update { it.copy(phoneNumber = phone) }
    }

    fun onAreaChanged(area: String) {
        _formState.update { it.copy(area = area) }
    }

    fun onScheduleToggled(enabled: Boolean) {
        _formState.update { it.copy(isScheduleEnabled = enabled) }
        if (enabled && _formState.value.scheduledMessage.isBlank()) {
            autoUpdateTemplateMessage(force = true)
        }
    }

    fun onScheduleDateTimeChanged(timeMs: Long) {
        _formState.update { it.copy(scheduledDateTime = timeMs) }
    }

    fun onMessageChanged(message: String) {
        _formState.update {
            it.copy(
                scheduledMessage = message,
                isMessageManuallyEdited = true
            )
        }
    }

    private fun autoUpdateTemplateMessage(force: Boolean = false) {
        val form = _formState.value
        if (!form.isMessageManuallyEdited || force) {
            val name = form.patientName.ifBlank { "المريض المحترم" }
            val amount = form.amount.ifBlank { "0" }
            val work = if (form.workType.isNotBlank()) " لنوع العمل (${form.workType})" else ""
            val template = "تذكير من عيادة الرحمن: الأخ الكريم $name، نود تذكيركم بمتبقي الحساب بمبلغ $amount د.ع/ريال$work. نرجو التكرم بالسداد في أقرب وقت. شاكرين تعاونكم وثقتكم بنا."
            _formState.update { it.copy(scheduledMessage = template) }
        }
    }

    fun savePatient(context: Context, onSuccess: () -> Unit = {}) {
        val form = _formState.value
        if (form.patientName.isBlank()) {
            _uiState.update { it.copy(userMessage = "يرجى كتابة اسم المريض أولاً") }
            return
        }

        val amountVal = form.amount.toDoubleOrNull() ?: 0.0
        if (amountVal <= 0.0 && form.editingId == null) {
            _uiState.update { it.copy(userMessage = "يرجى إدخال مبلغ صحيح للعمل أو الدين") }
            return
        }

        val scheduledTime = if (form.isScheduleEnabled) form.scheduledDateTime else 0L

        viewModelScope.launch {
            val isEditing = form.editingId != null
            val existing = if (isEditing) repository.getPatientById(form.editingId!!) else null

            val debt = PatientDebt(
                id = form.editingId ?: 0L,
                patientName = form.patientName.trim(),
                workType = form.workType.trim(),
                amount = amountVal,
                phoneNumber = form.phoneNumber.trim(),
                area = form.area.trim(),
                scheduledMessage = form.scheduledMessage.trim(),
                scheduledDateTime = scheduledTime,
                isSent = if (isEditing) existing?.isSent ?: false else false,
                isPaid = existing?.isPaid ?: false,
                paidAmount = existing?.paidAmount ?: 0.0,
                notes = existing?.notes ?: "",
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )

            val newId = if (isEditing) {
                repository.update(debt)
                debt.id
            } else {
                repository.insert(debt)
            }

            // Schedule or cancel alarm
            if (form.isScheduleEnabled && scheduledTime > System.currentTimeMillis()) {
                AlarmScheduler.scheduleDebtReminder(context, debt.copy(id = newId))
            } else if (isEditing) {
                AlarmScheduler.cancelDebtReminder(context, newId)
            }

            resetForm()
            _uiState.update {
                it.copy(userMessage = if (isEditing) "تم تعديل بيانات المريض بنجاح" else "تم حفظ المريض وجدولة الموعد بنجاح")
            }
            onSuccess()
        }
    }

    fun startEditing(patient: PatientDebt) {
        _formState.update {
            DebtFormState(
                editingId = patient.id,
                patientName = patient.patientName,
                workType = patient.workType,
                amount = "%.0f".format(patient.amount),
                phoneNumber = patient.phoneNumber,
                area = patient.area,
                isScheduleEnabled = patient.scheduledDateTime > 0L,
                scheduledDateTime = if (patient.scheduledDateTime > 0L) patient.scheduledDateTime else System.currentTimeMillis() + 86400000L,
                scheduledMessage = patient.scheduledMessage,
                isMessageManuallyEdited = patient.scheduledMessage.isNotBlank()
            )
        }
    }

    fun resetForm() {
        val defaultCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        _formState.value = DebtFormState(
            scheduledDateTime = defaultCal.timeInMillis
        )
    }

    fun deletePatient(context: Context, patient: PatientDebt) {
        viewModelScope.launch {
            AlarmScheduler.cancelDebtReminder(context, patient.id)
            repository.delete(patient)
            _uiState.update { it.copy(userMessage = "تم حذف سجل المريض ${patient.patientName}") }
        }
    }

    fun markAsPaid(patient: PatientDebt) {
        viewModelScope.launch {
            val updated = patient.copy(
                isPaid = true,
                paidAmount = patient.amount
            )
            repository.update(updated)
            _uiState.update { it.copy(userMessage = "تم تسديد حساب المريض ${patient.patientName} بالكامل") }
        }
    }

    fun recordPartialPayment(patient: PatientDebt, additionalPayment: Double) {
        viewModelScope.launch {
            val newPaid = (patient.paidAmount + additionalPayment).coerceAtMost(patient.amount)
            val isNowPaid = newPaid >= patient.amount
            val updated = patient.copy(
                paidAmount = newPaid,
                isPaid = isNowPaid
            )
            repository.update(updated)
            _uiState.update { it.copy(userMessage = "تم تسجيل دفعة بقيمة %.0f للمريض ${patient.patientName}".format(additionalPayment)) }
        }
    }

    fun exportToPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    repository.exportToPdf(os)
                }
                _uiState.update { it.copy(isExporting = false, userMessage = "تم تصدير كشف حساب PDF بنجاح") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, userMessage = "فشل تصدير PDF: ${e.message}") }
            }
        }
    }

    fun exportToCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    repository.exportToCsv(os)
                }
                _uiState.update { it.copy(isExporting = false, userMessage = "تم تصدير ملف الإكسل (CSV) بنجاح") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, userMessage = "فشل تصدير الإكسل: ${e.message}") }
            }
        }
    }

    fun exportBackupJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val json = repository.exportToJson()
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
                _uiState.update { it.copy(isExporting = false, userMessage = "تم إنشاء النسخة الاحتياطية بنجاح") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, userMessage = "فشل حفظ النسخة الاحتياطية: ${e.message}") }
            }
        }
    }

    fun restoreBackupJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: ""
                val count = repository.importFromJson(json)
                _uiState.update { it.copy(isExporting = false, userMessage = "تم استعادة $count سجل مريض بنجاح!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, userMessage = "فشل استعادة النسخة الاحتياطية: ملف غير متوافق") }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
