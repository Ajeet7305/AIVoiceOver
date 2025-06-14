package com.aivoiceclassifier.domain.model

data class DashboardItem(
    val id: String,
    val name: String,
    val type: DashboardItemType,
    val isDefault: Boolean = false
)

enum class DashboardItemType {
    INTERVIEW,
    NATIVE_LANGUAGE_SPEAKER,
    REAL_LANGUAGE_TRANSLATOR,
    CUSTOM
}

data class InterviewCompany(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
) 