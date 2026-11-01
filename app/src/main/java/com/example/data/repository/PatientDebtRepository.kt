package com.example.data.repository

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.local.PatientDebtDao
import com.example.data.model.PatientDebt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientDebtRepository(private val dao: PatientDebtDao) {

    val allPatients: Flow<List<PatientDebt>> = dao.getAllPatients()
    val unpaidDebts: Flow<List<PatientDebt>> = dao.getUnpaidDebts()
    val paidDebts: Flow<List<PatientDebt>> = dao.getPaidDebts()
    val scheduledReminders: Flow<List<PatientDebt>> = dao.getScheduledReminders()

    suspend fun getPatientById(id: Long): PatientDebt? = dao.getPatientById(id)

    suspend fun insert(patient: PatientDebt): Long = dao.insertPatient(patient)

    suspend fun update(patient: PatientDebt) = dao.updatePatient(patient)

    suspend fun delete(patient: PatientDebt) = dao.deletePatient(patient)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun updateSentStatus(id: Long, isSent: Boolean) = dao.updateSentStatus(id, isSent)

    suspend fun updatePaidStatus(id: Long, isPaid: Boolean, paidAmount: Double) =
        dao.updatePaidStatus(id, isPaid, paidAmount)

    suspend fun getAllSnapshot(): List<PatientDebt> = dao.getAllPatientsSnapshot()

    suspend fun getPendingAlarms(currentTime: Long): List<PatientDebt> =
        dao.getPendingAlarms(currentTime)

    // Export to JSON string
    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val list = dao.getAllPatientsSnapshot()
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("patientName", item.patientName)
            obj.put("workType", item.workType)
            obj.put("amount", item.amount)
            obj.put("phoneNumber", item.phoneNumber)
            obj.put("area", item.area)
            obj.put("scheduledMessage", item.scheduledMessage)
            obj.put("scheduledDateTime", item.scheduledDateTime)
            obj.put("isSent", item.isSent)
            obj.put("isPaid", item.isPaid)
            obj.put("paidAmount", item.paidAmount)
            obj.put("notes", item.notes)
            obj.put("createdAt", item.createdAt)
            jsonArray.put(obj)
        }
        val root = JSONObject()
        root.put("appName", "AlRahmanClinic")
        root.put("version", 1)
        root.put("backupDate", System.currentTimeMillis())
        root.put("patients", jsonArray)
        root.toString(2)
    }

    // Restore from JSON string
    suspend fun importFromJson(jsonString: String): Int = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonString)
        val array = root.optJSONArray("patients") ?: JSONArray()
        val list = mutableListOf<PatientDebt>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val item = PatientDebt(
                id = 0, // Auto-assign new IDs to avoid conflicts
                patientName = obj.optString("patientName", ""),
                workType = obj.optString("workType", ""),
                amount = obj.optDouble("amount", 0.0),
                phoneNumber = obj.optString("phoneNumber", ""),
                area = obj.optString("area", ""),
                scheduledMessage = obj.optString("scheduledMessage", ""),
                scheduledDateTime = obj.optLong("scheduledDateTime", 0L),
                isSent = obj.optBoolean("isSent", false),
                isPaid = obj.optBoolean("isPaid", false),
                paidAmount = obj.optDouble("paidAmount", 0.0),
                notes = obj.optString("notes", ""),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
            list.add(item)
        }
        if (list.isNotEmpty()) {
            dao.insertAll(list)
        }
        list.size
    }

    // Export to Excel-compatible CSV with UTF-8 BOM
    suspend fun exportToCsv(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val list = dao.getAllPatientsSnapshot()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            // Write UTF-8 Byte Order Mark for Excel Arabic support
            writer.write("\uFEFF")
            writer.write("اسم المريض,نوع العمل,المبلغ الكلي,المبلغ المسدد,المتبقي,رقم الهاتف,المنطقة,الحالة,تاريخ التسجيل,رسالة مجدولة\n")

            for (p in list) {
                val status = if (p.isPaid || p.remainingAmount <= 0) "مسدد" else "متبقي"
                val dateStr = dateFormat.format(Date(p.createdAt))
                val line = buildString {
                    append("\"${p.patientName.replace("\"", "\"\"")}\",")
                    append("\"${p.workType.replace("\"", "\"\"")}\",")
                    append("${p.amount},")
                    append("${p.paidAmount},")
                    append("${p.remainingAmount},")
                    append("\"${p.phoneNumber}\",")
                    append("\"${p.area.replace("\"", "\"\"")}\",")
                    append("\"$status\",")
                    append("\"$dateStr\",")
                    append("\"${p.scheduledMessage.replace("\"", "\"\"")}\"")
                    append("\n")
                }
                writer.write(line)
            }
            writer.flush()
        }
    }

    // Export to formatted PDF
    suspend fun exportToPdf(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val list = dao.getAllPatientsSnapshot()
        val document = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.rgb(13, 32, 64) // Clinic Navy
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(30, 86, 160)
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }

        val paintHeaderBox = Paint().apply {
            color = Color.rgb(19, 47, 92)
        }

        val paintHeaderBoxText = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val paintRowText = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }

        val paintRowAlt = Paint().apply {
            color = Color.rgb(241, 245, 249)
        }

        val paintFooter = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }

        val paintLine = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        var y = 45f

        // Header Title
        canvas.drawText("برنامج ديون عيادة الرحمن", pageWidth / 2f, y, paintTitle)
        y += 20f
        canvas.drawText("كشف حساب ديون وسجلات المرضى - تاريخ الإصدار: $todayStr", pageWidth / 2f, y, paintSubtitle)
        y += 25f

        // Summary Statistics Card
        val totalAmount = list.sumOf { it.amount }
        val totalPaid = list.sumOf { it.paidAmount }
        val totalRemaining = list.sumOf { it.remainingAmount }

        paintSubtitle.textSize = 10f
        val summaryText = "إجمالي الديون: %,.0f | إجمالي المسدد: %,.0f | المتبقي: %,.0f | عدد المرضى: %d".format(
            totalAmount, totalPaid, totalRemaining, list.size
        )
        canvas.drawText(summaryText, pageWidth / 2f, y, paintSubtitle)
        y += 25f

        // Table Column Definitions
        // Cols: Name(140), Work(80), Phone(85), Amount(65), Remaining(65), Status(60)
        val colWidths = floatArrayOf(135f, 85f, 90f, 75f, 75f, 55f)
        val startX = 40f
        val tableWidth = colWidths.sum()

        // Draw Table Header
        canvas.drawRect(startX, y, startX + tableWidth, y + 26f, paintHeaderBox)
        val headers = arrayOf("اسم المريض", "نوع العمل", "الهاتف", "المبلغ", "المتبقي", "الحالة")
        var curX = startX
        for (i in headers.indices) {
            val cellCenter = curX + (colWidths[i] / 2f)
            canvas.drawText(headers[i], cellCenter, y + 17f, paintHeaderBoxText)
            curX += colWidths[i]
        }
        y += 26f

        for ((index, item) in list.withIndex()) {
            if (y > pageHeight - 65f) {
                // Draw Footer for current page
                canvas.drawText("برمجة الدكتور / مالك الهمة - هاتف 771124103 | صفحة $pageNumber", pageWidth / 2f, pageHeight - 30f, paintFooter)
                document.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 45f

                // Re-draw table header on new page
                canvas.drawRect(startX, y, startX + tableWidth, y + 26f, paintHeaderBox)
                curX = startX
                for (i in headers.indices) {
                    val cellCenter = curX + (colWidths[i] / 2f)
                    canvas.drawText(headers[i], cellCenter, y + 17f, paintHeaderBoxText)
                    curX += colWidths[i]
                }
                y += 26f
            }

            // Alternating Row Background
            if (index % 2 == 1) {
                canvas.drawRect(startX, y, startX + tableWidth, y + 22f, paintRowAlt)
            }
            canvas.drawLine(startX, y + 22f, startX + tableWidth, y + 22f, paintLine)

            val statusText = if (item.isPaid || item.remainingAmount <= 0) "مسدد" else "متبقي"
            val rowValues = arrayOf(
                item.patientName.take(18),
                item.workType.take(12),
                item.phoneNumber.take(12),
                "%,.0f".format(item.amount),
                "%,.0f".format(item.remainingAmount),
                statusText
            )

            curX = startX
            for (i in rowValues.indices) {
                val cellCenter = curX + (colWidths[i] / 2f)
                canvas.drawText(rowValues[i], cellCenter, y + 15f, paintRowText)
                curX += colWidths[i]
            }
            y += 22f
        }

        // Final Page Footer
        canvas.drawText("برمجة الدكتور / مالك الهمة - هاتف 771124103 | صفحة $pageNumber", pageWidth / 2f, pageHeight - 30f, paintFooter)
        document.finishPage(page)

        document.writeTo(outputStream)
        document.close()
    }

    // Export single patient account statement to Excel-compatible CSV
    suspend fun exportSinglePatientToCsv(patient: PatientDebt, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("\uFEFF")
            writer.write("اسم المريض,نوع العمل,المبلغ الكلي,المبلغ المسدد,المتبقي,رقم الهاتف,المنطقة,الحالة,تاريخ التسجيل,رسالة التذكير\n")
            val status = if (patient.isPaid || patient.remainingAmount <= 0) "مسدد بالكامل" else "متبقي دين"
            val dateStr = dateFormat.format(Date(patient.createdAt))
            val line = buildString {
                append("\"${patient.patientName.replace("\"", "\"\"")}\",")
                append("\"${patient.workType.replace("\"", "\"\"")}\",")
                append("${patient.amount},")
                append("${patient.paidAmount},")
                append("${patient.remainingAmount},")
                append("\"${patient.phoneNumber}\",")
                append("\"${patient.area.replace("\"", "\"\"")}\",")
                append("\"$status\",")
                append("\"$dateStr\",")
                append("\"${patient.scheduledMessage.replace("\"", "\"\"")}\"")
                append("\n")
            }
            writer.write(line)
            writer.flush()
        }
    }

    // Export single patient account statement to a dedicated, official PDF
    suspend fun exportSinglePatientToPdf(patient: PatientDebt, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paintNavy = Paint().apply {
            color = Color.rgb(13, 32, 64)
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(30, 86, 160)
            textSize = 13f
            textAlign = Paint.Align.CENTER
        }

        val paintTextBold = Paint().apply {
            color = Color.rgb(13, 32, 64)
            textSize = 11f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val paintTextRegular = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }

        val paintBoxFill = Paint().apply {
            color = Color.rgb(241, 245, 249)
        }

        val paintBoxBorder = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        val paintGreen = Paint().apply {
            color = Color.rgb(16, 185, 129)
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val paintRed = Paint().apply {
            color = Color.rgb(239, 68, 68)
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val paintFooter = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault())
        val todayStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())

        var y = 55f

        // Clinic Header
        canvas.drawText("برنامج ديون عيادة الرحمن", pageWidth / 2f, y, paintNavy)
        y += 24f
        canvas.drawText("كشف حساب مريض وسند مديونية", pageWidth / 2f, y, paintSubtitle)
        y += 18f
        paintSubtitle.textSize = 10f
        canvas.drawText("تاريخ الإصدار: $todayStr", pageWidth / 2f, y, paintSubtitle)
        y += 20f

        // Separator Line
        canvas.drawLine(40f, y, pageWidth - 40f, y, paintBoxBorder)
        y += 25f

        // Patient Information Box
        val boxLeft = 40f
        val boxRight = pageWidth - 40f
        val infoBoxHeight = 115f
        canvas.drawRoundRect(boxLeft, y, boxRight, y + infoBoxHeight, 12f, 12f, paintBoxFill)
        canvas.drawRoundRect(boxLeft, y, boxRight, y + infoBoxHeight, 12f, 12f, paintBoxBorder)

        var rowY = y + 26f
        canvas.drawText("اسم المريض:", boxRight - 20f, rowY, paintTextBold)
        canvas.drawText(patient.patientName, boxRight - 110f, rowY, paintTextRegular)

        canvas.drawText("رقم الهاتف:", boxRight - 310f, rowY, paintTextBold)
        canvas.drawText(patient.phoneNumber.ifBlank { "غير مسجل" }, boxRight - 390f, rowY, paintTextRegular)

        rowY += 28f
        canvas.drawText("نوع العمل:", boxRight - 20f, rowY, paintTextBold)
        canvas.drawText(patient.workType.ifBlank { "علاج عام" }, boxRight - 110f, rowY, paintTextRegular)

        canvas.drawText("المنطقة:", boxRight - 310f, rowY, paintTextBold)
        canvas.drawText(patient.area.ifBlank { "غير محدد" }, boxRight - 390f, rowY, paintTextRegular)

        rowY += 28f
        canvas.drawText("تاريخ التسجيل:", boxRight - 20f, rowY, paintTextBold)
        canvas.drawText(dateFormat.format(Date(patient.createdAt)), boxRight - 110f, rowY, paintTextRegular)

        y += infoBoxHeight + 25f

        // Financial Amounts Box
        val financeBoxHeight = 135f
        canvas.drawRoundRect(boxLeft, y, boxRight, y + financeBoxHeight, 12f, 12f, paintBoxFill)
        canvas.drawRoundRect(boxLeft, y, boxRight, y + financeBoxHeight, 12f, 12f, paintBoxBorder)

        rowY = y + 30f
        canvas.drawText("المبلغ الإجمالي:", boxRight - 20f, rowY, paintTextBold)
        canvas.drawText("%,.0f د.ع/ريال".format(patient.amount), boxRight - 140f, rowY, paintTextRegular)

        rowY += 30f
        canvas.drawText("المبلغ المسدد:", boxRight - 20f, rowY, paintTextBold)
        canvas.drawText("%,.0f د.ع/ريال".format(patient.paidAmount), boxRight - 140f, rowY, paintGreen)

        rowY += 32f
        canvas.drawText("المتبقي للسداد:", boxRight - 20f, rowY, paintTextBold)
        val isCleared = patient.isPaid || patient.remainingAmount <= 0
        if (isCleared) {
            canvas.drawText("0 د.ع (مسدد بالكامل)", boxRight - 140f, rowY, paintGreen)
        } else {
            canvas.drawText("%,.0f د.ع/ريال (متبقي)".format(patient.remainingAmount), boxRight - 140f, rowY, paintRed)
        }

        y += financeBoxHeight + 25f

        // Scheduled Reminder (if present)
        if (patient.hasScheduledReminder && patient.scheduledMessage.isNotBlank()) {
            val messageBoxHeight = 85f
            canvas.drawRoundRect(boxLeft, y, boxRight, y + messageBoxHeight, 12f, 12f, paintBoxFill)
            canvas.drawRoundRect(boxLeft, y, boxRight, y + messageBoxHeight, 12f, 12f, paintBoxBorder)

            rowY = y + 25f
            canvas.drawText("تنبيه التذكير المجدول:", boxRight - 20f, rowY, paintTextBold)
            rowY += 22f
            paintTextRegular.textSize = 10f
            canvas.drawText(patient.scheduledMessage.take(80), boxRight - 20f, rowY, paintTextRegular)
            if (patient.scheduledMessage.length > 80) {
                rowY += 18f
                canvas.drawText(patient.scheduledMessage.substring(80).take(80), boxRight - 20f, rowY, paintTextRegular)
            }
            y += messageBoxHeight + 35f
        } else {
            y += 35f
        }

        // Signatures area
        rowY = y + 40f
        canvas.drawText("ختم وتوقيع العيادة", boxRight - 80f, rowY, paintTextBold)
        canvas.drawText("توقيع المريض المستلم", boxLeft + 150f, rowY, paintTextBold)

        // Footer
        canvas.drawText("برمجة وإشراف: الدكتور / مالك الهمة - هاتف 771124103", pageWidth / 2f, pageHeight - 35f, paintFooter)

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
