package com.aivoiceclassifier.di

import android.content.Context
import androidx.room.Room
import com.aivoiceclassifier.data.local.dao.CompanyDao
import com.aivoiceclassifier.data.local.dao.InterviewSessionDao
import com.aivoiceclassifier.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun provideCompanyDao(database: AppDatabase): CompanyDao {
        return database.companyDao()
    }
    
    @Provides
    fun provideInterviewSessionDao(database: AppDatabase): InterviewSessionDao {
        return database.interviewSessionDao()
    }
} 