package com.aivoiceclassifier.data.repository

import com.aivoiceclassifier.data.local.dao.CompanyDao
import com.aivoiceclassifier.data.local.entity.CompanyEntity
import com.aivoiceclassifier.domain.model.Company
import com.aivoiceclassifier.domain.repository.CompanyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRepositoryImpl @Inject constructor(
    private val companyDao: CompanyDao
) : CompanyRepository {
    
    override fun getAllCompanies(): Flow<List<Company>> {
        return companyDao.getAllCompanies().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getCompanyById(id: Long): Company? {
        return companyDao.getCompanyById(id)?.toDomain()
    }
    
    override suspend fun insertCompany(company: Company): Long {
        return companyDao.insertCompany(company.toEntity())
    }
    
    override suspend fun deleteCompany(company: Company) {
        companyDao.deleteCompany(company.toEntity())
    }
    
    override suspend fun deleteCompanyById(id: Long) {
        companyDao.deleteCompanyById(id)
    }
}

// Extension functions for mapping
private fun CompanyEntity.toDomain(): Company {
    return Company(
        id = id,
        name = name,
        createdAt = createdAt
    )
}

private fun Company.toEntity(): CompanyEntity {
    return CompanyEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )
} 