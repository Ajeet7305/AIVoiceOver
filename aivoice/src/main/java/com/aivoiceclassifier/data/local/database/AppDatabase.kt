package com.aivoiceclassifier.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.aivoiceclassifier.data.local.dao.CompanyDao
import com.aivoiceclassifier.data.local.dao.InterviewSessionDao
import com.aivoiceclassifier.data.local.entity.CompanyEntity
import com.aivoiceclassifier.data.local.entity.InterviewSessionEntity

@Database(
    entities = [
        CompanyEntity::class,
        InterviewSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun companyDao(): CompanyDao
    abstract fun interviewSessionDao(): InterviewSessionDao
    
    companion object {
        const val DATABASE_NAME = "aivoice_database"
    }
} 