package com.csvparser.exception

/**
 * Base exception for CSV parsing errors
 */
sealed class CsvParsingException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Thrown when the file format is invalid
 */
class InvalidFileFormatException(message: String) : CsvParsingException(message)

/**
 * Thrown when validation fails
 */
class ValidationException(message: String) : CsvParsingException(message)

/**
 * Thrown when required fields are missing
 */
class MissingFieldException(message: String) : CsvParsingException(message)

/**
 * Thrown when trailer count doesn't match record count
 */
class TrailerCountMismatchException(expected: Int, actual: Int) : 
    CsvParsingException("Trailer count mismatch: expected $expected, but found $actual records") 