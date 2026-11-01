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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClinicAccentCyan
import com.example.ui.theme.ClinicNavy
import com.example.ui.theme.ClinicNavyDark
import com.example.ui.theme.ClinicWhite

@Composable
fun ClinicTopBar(
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onBackupJson: () -> Unit,
    onRestoreJson: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ClinicNavyDark,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ClinicNavyDark, ClinicNavy)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title and Clinic Icon Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ClinicNavy)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ClinicAccentCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "شعار العيادة",
                                tint = ClinicWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "برنامج ديون عيادة الرحمن",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicWhite,
                                fontSize = 17.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "إدارة الحسابات وجدولة الرسائل التلقائية",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ClinicWhite.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Quick Action Buttons (PDF, Excel, More)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Export PDF Button
                    FilledTonalIconButton(
                        onClick = onExportPdf,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("export_pdf_button"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = ClinicWhite.copy(alpha = 0.15f),
                            contentColor = ClinicWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "تصدير PDF",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Export Excel Button
                    FilledTonalIconButton(
                        onClick = onExportCsv,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("export_excel_button"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = ClinicWhite.copy(alpha = 0.15f),
                            contentColor = ClinicWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "تصدير إكسل",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // More actions dropdown (Backup / Restore)
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("more_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "خيارات إضافية",
                                tint = ClinicWhite
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(ClinicWhite)
                        ) {
                            DropdownMenuItem(
                                text = { Text("نسخة احتياطية للهاتف (Backup)", color = ClinicNavyDark) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = ClinicNavy
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onBackupJson()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("استرجاع نسخة احتياطية (Restore)", color = ClinicNavyDark) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = ClinicNavy
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRestoreJson()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
