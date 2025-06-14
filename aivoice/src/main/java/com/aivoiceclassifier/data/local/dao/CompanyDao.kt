package com.aivoiceclassifier.data.local.dao

import androidx.room.*
import com.aivoiceclassifier.data.local.entity.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<CompanyEntity>>
    
    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: Long): CompanyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity): Long
    
    @Delete
    suspend fun deleteCompany(company: CompanyEntity)
    
    @Query("DELETE FROM companies WHERE id = :id")
    suspend fun deleteCompanyById(id: Long)
} 