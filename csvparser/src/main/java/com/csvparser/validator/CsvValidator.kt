package com.csvparser.validator

import com.csvparser.exception.TrailerCountMismatchException
import com.csvparser.exception.ValidationException
import com.csvparser.model.DeviceLine
import com.csvparser.parser.ParseResult

/**
 * Validator for parsed CSV data
 */
class CsvValidator {
    
    /**
     * Validates the parsed CSV data
     * @param parseResult The parsed data to validate
     * @throws ValidationException if validation fails
     * @throws TrailerCountMismatchException if record count doesn't match trailer
     */
    fun validate(parseResult: ParseResult) {
        validateDataIntegrity(parseResult)
        validateRecordCount(parseResult)
    }
    
    private fun validateRecordCount(parseResult: ParseResult) {
        val actualCount = parseResult.deviceLines.size
        val expectedCount = parseResult.trailer.recordCount
        
        if (actualCount != expectedCount) {
            throw TrailerCountMismatchException(expectedCount, actualCount)
        }
    }
    
    private fun validateDataIntegrity(parseResult: ParseResult) {
        // Validate header
        if (parseResult.header.serverID.isBlank()) {
            throw ValidationException("Server ID cannot be blank")
        }
        
        // Validate trailer
        if (parseResult.trailer.recordCount < 0) {
            throw ValidationException("Record count cannot be negative")
        }
        
        // Validate device lines
        parseResult.deviceLines.forEachIndexed { index, deviceLine ->
            try {
                validateDeviceLine(deviceLine)
            } catch (e: Exception) {
                throw ValidationException("Invalid device line at position ${index + 1}: ${e.message}")
            }
        }
    }
    
    private fun validateDeviceLine(deviceLine: DeviceLine) {
        if (deviceLine.imei1.isBlank()) {
            throw ValidationException("IMEI1 cannot be blank")
        }
        if (deviceLine.imei2.isBlank()) {
            throw ValidationException("IMEI2 cannot be blank")
        }
        if (deviceLine.serialNumber.isBlank()) {
            throw ValidationException("Serial number cannot be blank")
        }
        if (deviceLine.deviceName.isBlank()) {
            throw ValidationException("Device name cannot be blank")
        }
        
        // Additional IMEI validation (basic format check)
        if (!isValidImei(deviceLine.imei1)) {
            throw ValidationException("Invalid IMEI1 format: ${deviceLine.imei1}")
        }
        if (!isValidImei(deviceLine.imei2)) {
            throw ValidationException("Invalid IMEI2 format: ${deviceLine.imei2}")
        }
    }
    
    private fun isValidImei(imei: String): Boolean {
        // Basic IMEI validation: should be 15 digits
        return imei.matches(Regex("\\d{15}"))
    }
} 