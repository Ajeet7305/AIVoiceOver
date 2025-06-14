package com.aivoiceclassifier.domain.repository

import com.aivoiceclassifier.domain.model.Company
import kotlinx.coroutines.flow.Flow

interface CompanyRepository {
    fun getAllCompanies(): Flow<List<Company>>
    suspend fun getCompanyById(id: Long): Company?
    suspend fun insertCompany(company: Company): Long
    suspend fun deleteCompany(company: Company)
    suspend fun deleteCompanyById(id: Long)
} 