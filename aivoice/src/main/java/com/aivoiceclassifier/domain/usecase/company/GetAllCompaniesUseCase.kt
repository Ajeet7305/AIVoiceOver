package com.aivoiceclassifier.domain.usecase.company

import com.aivoiceclassifier.domain.model.Company
import com.aivoiceclassifier.domain.repository.CompanyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCompaniesUseCase @Inject constructor(
    private val companyRepository: CompanyRepository
) {
    operator fun invoke(): Flow<List<Company>> {
        return companyRepository.getAllCompanies()
    }
} 