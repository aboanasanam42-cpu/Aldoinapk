package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PatientDebt
import com.example.ui.components.ClinicStatsCard
import com.example.ui.components.ClinicTopBar
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.FooterDoctorMalik
import com.example.ui.components.PartialPaymentDialog
import com.example.ui.components.PatientCard
import com.example.ui.components.PatientFormSection
import com.example.ui.theme.ClinicAccentCyan
import com.example.ui.theme.ClinicBlue
import com.example.ui.theme.ClinicBlueLight
import com.example.ui.theme.ClinicBlueSurface
import com.example.ui.theme.ClinicCardBorder
import com.example.ui.theme.ClinicNavy
import com.example.ui.theme.ClinicNavyDark
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.ui.theme.ClinicWhite
import com.example.ui.viewmodel.ClinicViewModel
import com.example.ui.viewmodel.FilterTab
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainClinicScreen(
    viewModel: ClinicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: قائمة الديون, 1: إضافة دين
    var patientForPayment by remember { mutableStateOf<PatientDebt?>(null) }
    var patientForDelete by remember { mutableStateOf<PatientDebt?>(null) }

    // Request permissions for SMS and notifications on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(Manifest.permission.SEND_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Snackbars
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Storage SAF Activity Result Launchers
    val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())

    val exportPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToPdf(context, uri)
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToCsv(context, uri)
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupJson(context, uri)
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreBackupJson(context, uri)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ClinicBlueSurface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ClinicTopBar(
                onExportPdf = {
                    exportPdfLauncher.launch("كشف_ديون_عيادة_الرحمن_$dateStamp.pdf")
                },
                onExportCsv = {
                    exportCsvLauncher.launch("ديون_عيادة_الرحمن_$dateStamp.csv")
                },
                onBackupJson = {
                    exportBackupLauncher.launch("نسخة_احتياطية_عيادة_الرحمن_$dateStamp.json")
                },
                onRestoreJson = {
                    restoreBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                }
            )
        },
        bottomBar = {
            FooterDoctorMalik()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTabIndex == 1) {
                        // In form -> scroll or trigger save
                        viewModel.savePatient(context) {
                            selectedTabIndex = 0
                        }
                    } else {
                        // Switch to form tab
                        viewModel.resetForm()
                        selectedTabIndex = 1
                    }
                },
                containerColor = ClinicNavy,
                contentColor = ClinicWhite,
                shape = CircleShape,
                modifier = Modifier.testTag("main_fab")
            ) {
                Icon(
                    imageVector = if (selectedTabIndex == 1) Icons.Default.Add else Icons.Default.PersonAdd,
                    contentDescription = if (selectedTabIndex == 1) "حفظ" else "إضافة مريض جديد"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Navigation Tabs: "سجل الديون والمرضى" & "تسجيل دين جديد"
            androidx.compose.material3.TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = ClinicWhite,
                contentColor = ClinicNavy
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "سجل الديون (${uiState.patientCount})",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = ClinicNavy,
                    unselectedContentColor = ClinicTextSecondary,
                    modifier = Modifier.testTag("tab_patients_list")
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (formState.editingId != null) "تعديل المريض" else "إضافة دين جديد",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = ClinicNavy,
                    unselectedContentColor = ClinicTextSecondary,
                    modifier = Modifier.testTag("tab_add_patient")
                )
            }

            // Main Content Area
            if (selectedTabIndex == 1) {
                // Form Tab: Add / Edit
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        PatientFormSection(
                            formState = formState,
                            onNameChanged = viewModel::onNameChanged,
                            onWorkTypeChanged = viewModel::onWorkTypeChanged,
                            onAmountChanged = viewModel::onAmountChanged,
                            onAddAmountPreset = viewModel::addAmountPreset,
                            onPhoneChanged = viewModel::onPhoneChanged,
                            onAreaChanged = viewModel::onAreaChanged,
                            onScheduleToggled = viewModel::onScheduleToggled,
                            onScheduleDateTimeChanged = viewModel::onScheduleDateTimeChanged,
                            onMessageChanged = viewModel::onMessageChanged,
                            onSaveClick = {
                                viewModel.savePatient(context) {
                                    selectedTabIndex = 0
                                }
                            },
                            onCancelEdit = {
                                viewModel.resetForm()
                                selectedTabIndex = 0
                            }
                        )
                    }
                }
            } else {
                // List Tab: Statistics, Search, Filter Tabs, Patients List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Statistics Overview Card
                    item {
                        ClinicStatsCard(
                            totalRemaining = uiState.totalRemaining,
                            totalPaid = uiState.totalPaid,
                            patientCount = uiState.patientCount,
                            scheduledCount = uiState.scheduledCount
                        )
                    }

                    // Search Field
                    item {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = { Text("بحث باسم المريض، رقم الهاتف، أو نوع العمل...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = ClinicNavy)
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ClinicWhite,
                                unfocusedContainerColor = ClinicWhite,
                                focusedBorderColor = ClinicNavy,
                                unfocusedBorderColor = ClinicCardBorder
                            )
                        )
                    }

                    // Filter Status Row: الكل | متبقي | مسدد | مجدول
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (tab in FilterTab.values()) {
                                val isSelected = uiState.currentFilter == tab
                                androidx.compose.material3.FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onFilterSelected(tab) },
                                    label = {
                                        Text(
                                            text = when (tab) {
                                                FilterTab.ALL -> "الكل (${uiState.patientCount})"
                                                FilterTab.UNPAID -> "متبقي (${uiState.patients.count { it.isPendingDebt }})"
                                                FilterTab.PAID -> "مسدد (${uiState.patients.count { !it.isPendingDebt }})"
                                                FilterTab.SCHEDULED -> "مجدول (${uiState.scheduledCount})"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ClinicNavy,
                                        selectedLabelColor = ClinicWhite,
                                        containerColor = ClinicWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // Patients List
                    if (uiState.filteredPatients.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = ClinicTextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (uiState.searchQuery.isNotBlank()) "لا توجد نتائج مطابقة للبحث" else "لا توجد سجلات ديون حالياً",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = ClinicTextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "اضغط على تبويب \"إضافة دين جديد\" لإضافة أول مريض",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ClinicTextSecondary.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.filteredPatients,
                            key = { it.id }
                        ) { patient ->
                            PatientCard(
                                patient = patient,
                                onEdit = {
                                    viewModel.startEditing(patient)
                                    selectedTabIndex = 1
                                },
                                onDelete = {
                                    patientForDelete = patient
                                },
                                onMarkAsPaid = {
                                    viewModel.markAsPaid(patient)
                                },
                                onPartialPaymentClick = {
                                    patientForPayment = patient
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Partial Payment Recording Dialog
    patientForPayment?.let { patient ->
        PartialPaymentDialog(
            patient = patient,
            onDismiss = { patientForPayment = null },
            onConfirmPayment = { amount ->
                viewModel.recordPartialPayment(patient, amount)
                patientForPayment = null
            },
            onMarkFullPaid = {
                viewModel.markAsPaid(patient)
                patientForPayment = null
            }
        )
    }

    // Delete Confirmation Dialog
    patientForDelete?.let { patient ->
        DeleteConfirmDialog(
            patient = patient,
            onDismiss = { patientForDelete = null },
            onConfirmDelete = {
                viewModel.deletePatient(context, patient)
                patientForDelete = null
            }
        )
    }
}
