package com.aivoiceclassifier.domain.usecase.company

import com.aivoiceclassifier.domain.model.Company
import com.aivoiceclassifier.domain.repository.CompanyRepository
import javax.inject.Inject

class AddCompanyUseCase @Inject constructor(
    private val companyRepository: CompanyRepository
) {
    suspend operator fun invoke(companyName: String): Result<Long> {
        return try {
            if (companyName.isBlank()) {
                Result.failure(IllegalArgumentException("Company name cannot be empty"))
            } else {
                val company = Company(name = companyName.trim())
                val id = companyRepository.insertCompany(company)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 