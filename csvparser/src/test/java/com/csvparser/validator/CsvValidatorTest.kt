package com.csvparser.validator

import com.csvparser.exception.TrailerCountMismatchException
import com.csvparser.exception.ValidationException
import com.csvparser.model.DeviceLine
import com.csvparser.model.Header
import com.csvparser.model.Trailer
import com.csvparser.parser.ParseResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CsvValidatorTest {
    
    private val validator = CsvValidator()
    
    @Test
    fun `should validate correct data successfully`() {
        val parseResult = createValidParseResult()
        
        assertDoesNotThrow {
            validator.validate(parseResult)
        }
    }
    
    @Test
    fun `should throw exception for trailer count mismatch - more records than expected`() {
        val parseResult = ParseResult(
            header = Header("SERVER123"),
            deviceLines = listOf(
                DeviceLine("123400000000000", "123400000000060", "A5123456700000250", "UNIPOS A5"),
                DeviceLine("123400000000001", "123400000000061", "A5123456700000251", "UNIPOS A5")
            ),
            trailer = Trailer(1) // Expected 1 but got 2
        )
        
        val exception = assertThrows(TrailerCountMismatchException::class.java) {
            validator.validate(parseResult)
        }
        
        assertTrue(exception.message!!.contains("expected 1, but found 2 records"))
    }
    
    @Test
    fun `should throw exception for trailer count mismatch - fewer records than expected`() {
        val parseResult = ParseResult(
            header = Header("SERVER123"),
            deviceLines = listOf(
                DeviceLine("123400000000000", "123400000000060", "A5123456700000250", "UNIPOS A5")
            ),
            trailer = Trailer(3) // Expected 3 but got 1
        )
        
        val exception = assertThrows(TrailerCountMismatchException::class.java) {
            validator.validate(parseResult)
        }
        
        assertTrue(exception.message!!.contains("expected 3, but found 1 records"))
    }
    
    @Test
    fun `should throw exception for invalid IMEI1 format`() {
        val parseResult = ParseResult(
            header = Header("SERVER123"),
            deviceLines = listOf(
                DeviceLine("12345", "123400000000060", "A5123456700000250", "UNIPOS A5") // Invalid IMEI1
            ),
            trailer = Trailer(1)
        )
        
        val exception = assertThrows(ValidationException::class.java) {
            validator.validate(parseResult)
        }
        
        assertTrue(exception.message!!.contains("Invalid IMEI1 format"))
    }
    
    @Test
    fun `should throw exception for invalid IMEI2 format`() {
        val parseResult = ParseResult(
            header = Header("SERVER123"),
            deviceLines = listOf(
                DeviceLine("123400000000000", "12345", "A5123456700000250", "UNIPOS A5") // Invalid IMEI2
            ),
            trailer = Trailer(1)
        )
        
        val exception = assertThrows(ValidationException::class.java) {
            validator.validate(parseResult)
        }
        
        assertTrue(exception.message!!.contains("Invalid IMEI2 format"))
    }
    
    @Test
    fun `should throw exception for negative trailer count`() {
        val parseResult = ParseResult(
            header = Header("SERVER123"),
            deviceLines = emptyList(),
            trailer = Trailer(-1)
        )
        
        val exception = assertThrows(ValidationException::class.java) {
            validator.validate(parseResult)
        }
        
        assertTrue(exception.message!!.contains("Record count cannot be negative"))
    }
    
    @Test
    fun `should validate with zero records`() {
        val parseResult = ParseResult(
            header = Header("SERVER123"),
            deviceLines = emptyList(),
            trailer = Trailer(0)
        )
        
        assertDoesNotThrow {
            validator.validate(parseResult)
        }
    }
    
    @Test
    fun `should validate IMEI with exactly 15 digits`() {
        val parseResult = ParseResult(
            header = Header("SERVER123"),
            deviceLines = listOf(
                DeviceLine("123456789012345", "987654321098765", "A5123456700000250", "UNIPOS A5")
            ),
            trailer = Trailer(1)
        )
        
        assertDoesNotThrow {
            validator.validate(parseResult)
        }
    }
    
    @Test
    fun `should throw exception for IMEI with letters`() {
        val parseResult = ParseResult(
            header = Header("SERVER123"),
            deviceLines = listOf(
                DeviceLine("12345678901234A", "123400000000060", "A5123456700000250", "UNIPOS A5") // Letter in IMEI
            ),
            trailer = Trailer(1)
        )
        
        val exception = assertThrows(ValidationException::class.java) {
            validator.validate(parseResult)
        }
        
        assertTrue(exception.message!!.contains("Invalid IMEI1 format"))
    }
    
    private fun createValidParseResult(): ParseResult {
        return ParseResult(
            header = Header("HDKF190320201903202020032020171931"),
            deviceLines = listOf(
                DeviceLine("123400000000000", "123400000000060", "A5123456700000250", "UNIPOS A5"),
                DeviceLine("123400000000001", "123400000000061", "A5123456700000251", "UNIPOS A5")
            ),
            trailer = Trailer(2)
        )
    }
} 