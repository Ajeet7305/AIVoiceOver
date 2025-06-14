package com.csvparser.service

import com.csvparser.exception.TrailerCountMismatchException
import com.csvparser.exception.ValidationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class CsvProcessingServiceTest {
    
    private val service = CsvProcessingService()
    
    @TempDir
    lateinit var tempDir: Path
    
    @Test
    fun `should process valid CSV file to JSON successfully`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            R|123400000000001|123400000000061|A5123456700000251|UNIPOS A5
            T|2
        """.trimIndent()
        
        val inputFile = createTempFile("input.csv", csvContent)
        val outputFile = tempDir.resolve("output.json").toFile()
        
        assertDoesNotThrow {
            service.processFile(inputFile.absolutePath, outputFile.absolutePath, OutputFormat.JSON)
        }
        
        assertTrue(outputFile.exists())
        val jsonContent = outputFile.readText()
        
        assertTrue(jsonContent.contains("Devicedetails"))
        assertTrue(jsonContent.contains("HDKF190320201903202020032020171931"))
        assertTrue(jsonContent.contains("123400000000000"))
        assertTrue(jsonContent.contains("UNIPOS A5"))
    }
    
    @Test
    fun `should process valid CSV file to XML successfully`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent()
        
        val inputFile = createTempFile("input.csv", csvContent)
        val outputFile = tempDir.resolve("output.xml").toFile()
        
        assertDoesNotThrow {
            service.processFile(inputFile.absolutePath, outputFile.absolutePath, OutputFormat.XML)
        }
        
        assertTrue(outputFile.exists())
        val xmlContent = outputFile.readText()
        
        assertTrue(xmlContent.contains("DeviceReportWrapper"))
        assertTrue(xmlContent.contains("HDKF190320201903202020032020171931"))
        assertTrue(xmlContent.contains("123400000000000"))
    }
    
    @Test
    fun `should return JSON string for valid CSV`() {
        val csvContent = """
            H|SERVER123
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent()
        
        val inputFile = createTempFile("input.csv", csvContent)
        
        val result = service.processFileToString(inputFile.absolutePath, OutputFormat.JSON)
        
        assertNotNull(result)
        assertTrue(result.contains("Devicedetails"))
        assertTrue(result.contains("SERVER123"))
        assertTrue(result.contains("123400000000000"))
    }
    
    @Test
    fun `should return XML string for valid CSV`() {
        val csvContent = """
            H|SERVER123
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent()
        
        val inputFile = createTempFile("input.csv", csvContent)
        
        val result = service.processFileToString(inputFile.absolutePath, OutputFormat.XML)
        
        assertNotNull(result)
        assertTrue(result.contains("DeviceReportWrapper"))
        assertTrue(result.contains("SERVER123"))
        assertTrue(result.contains("123400000000000"))
    }
    
    @Test
    fun `should throw exception for trailer count mismatch`() {
        val csvContent = """
            H|SERVER123
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            R|123400000000001|123400000000061|A5123456700000251|UNIPOS A5
            T|1
        """.trimIndent() // Trailer says 1 but there are 2 records
        
        val inputFile = createTempFile("input.csv", csvContent)
        val outputFile = tempDir.resolve("output.json").toFile()
        
        val exception = assertThrows(TrailerCountMismatchException::class.java) {
            service.processFile(inputFile.absolutePath, outputFile.absolutePath, OutputFormat.JSON)
        }
        
        assertTrue(exception.message!!.contains("expected 1, but found 2 records"))
    }
    
    @Test
    fun `should throw exception for invalid IMEI format`() {
        val csvContent = """
            H|SERVER123
            R|12345|123400000000060|A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent() // Invalid IMEI1 (too short)
        
        val inputFile = createTempFile("input.csv", csvContent)
        val outputFile = tempDir.resolve("output.json").toFile()
        
        val exception = assertThrows(ValidationException::class.java) {
            service.processFile(inputFile.absolutePath, outputFile.absolutePath, OutputFormat.JSON)
        }
        
        assertTrue(exception.message!!.contains("Invalid IMEI1 format"))
    }
    
    @Test
    fun `should handle empty device list`() {
        val csvContent = """
            H|SERVER123
            T|0
        """.trimIndent()
        
        val inputFile = createTempFile("input.csv", csvContent)
        val outputFile = tempDir.resolve("output.json").toFile()
        
        assertDoesNotThrow {
            service.processFile(inputFile.absolutePath, outputFile.absolutePath, OutputFormat.JSON)
        }
        
        assertTrue(outputFile.exists())
        val jsonContent = outputFile.readText()
        
        assertTrue(jsonContent.contains("Devicedetails"))
        assertTrue(jsonContent.contains("SERVER123"))
        assertTrue(jsonContent.contains("\"deviceLines\" : [ ]"))
    }
    
    private fun createTempFile(filename: String, content: String): File {
        val file = tempDir.resolve(filename).toFile()
        file.writeText(content)
        return file
    }
} 