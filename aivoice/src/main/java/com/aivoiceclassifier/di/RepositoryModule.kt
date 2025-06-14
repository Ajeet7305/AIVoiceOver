package com.aivoiceclassifier.di

import com.aivoiceclassifier.data.repository.AIAssistantRepositoryImpl
import com.aivoiceclassifier.data.repository.CompanyRepositoryImpl
import com.aivoiceclassifier.data.repository.InterviewRepositoryImpl
import com.aivoiceclassifier.data.service.AudioClassificationServiceImpl
import com.aivoiceclassifier.domain.repository.AIAssistantRepository
import com.aivoiceclassifier.domain.repository.CompanyRepository
import com.aivoiceclassifier.domain.repository.InterviewRepository
import com.aivoiceclassifier.domain.service.AudioClassificationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindCompanyRepository(
        companyRepositoryImpl: CompanyRepositoryImpl
    ): CompanyRepository
    
    @Binds
    @Singleton
    abstract fun bindInterviewRepository(
        interviewRepositoryImpl: InterviewRepositoryImpl
    ): InterviewRepository
    
    @Binds
    @Singleton
    abstract fun bindAIAssistantRepository(
        aiAssistantRepositoryImpl: AIAssistantRepositoryImpl
    ): AIAssistantRepository
    
    @Binds
    @Singleton
    abstract fun bindAudioClassificationService(
        audioClassificationServiceImpl: AudioClassificationServiceImpl
    ): AudioClassificationService
} 