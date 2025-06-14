package com.aivoiceclassifier.domain.model

data class Company(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
) 