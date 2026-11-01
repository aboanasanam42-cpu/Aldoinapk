package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDebtDao {

    @Query("SELECT * FROM patients ORDER BY createdAt DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE isPaid = 0 AND (amount - paidAmount) > 0 ORDER BY createdAt DESC")
    fun getUnpaidDebts(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE isPaid = 1 OR (amount - paidAmount) <= 0 ORDER BY createdAt DESC")
    fun getPaidDebts(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE scheduledDateTime > 0 AND isSent = 0 ORDER BY scheduledDateTime ASC")
    fun getScheduledReminders(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatientById(id: Long): PatientEntity?

    @Query("SELECT * FROM patients WHERE scheduledDateTime > :currentTime AND isSent = 0")
    suspend fun getPendingAlarms(currentTime: Long): List<PatientEntity>

    @Query("SELECT * FROM patients")
    suspend fun getAllPatientsSnapshot(): List<PatientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<PatientEntity>)

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Delete
    suspend fun deletePatient(patient: PatientEntity)

    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE patients SET isSent = :isSent WHERE id = :id")
    suspend fun updateSentStatus(id: Long, isSent: Boolean)

    @Query("UPDATE patients SET isPaid = :isPaid, paidAmount = :paidAmount WHERE id = :id")
    suspend fun updatePaidStatus(id: Long, isPaid: Boolean, paidAmount: Double)

    @Query("DELETE FROM patients")
    suspend fun clearAll()
}
