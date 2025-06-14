package com.csvparser.model

/**
 * Represents a trailer line in the CSV file (T|RecordCount)
 */
data class Trailer(
    val recordCount: Int
) 