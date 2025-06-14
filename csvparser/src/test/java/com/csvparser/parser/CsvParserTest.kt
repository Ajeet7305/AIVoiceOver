package com.csvparser.parser

import com.csvparser.exception.InvalidFileFormatException
import com.csvparser.exception.MissingFieldException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class CsvParserTest {
    
    private val parser = CsvParser()
    
    @TempDir
    lateinit var tempDir: Path
    
    @Test
    fun `should parse valid CSV file successfully`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            R|123400000000001|123400000000061|A5123456700000251|UNIPOS A5
            T|2
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        val result = parser.parse(file.absolutePath)
        
        assertEquals("HDKF190320201903202020032020171931", result.header.serverID)
        assertEquals(2, result.deviceLines.size)
        assertEquals(2, result.trailer.recordCount)
        
        with(result.deviceLines[0]) {
            assertEquals("123400000000000", imei1)
            assertEquals("123400000000060", imei2)
            assertEquals("A5123456700000250", serialNumber)
            assertEquals("UNIPOS A5", deviceName)
        }
    }
    
    @Test
    fun `should throw exception for non-existent file`() {
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse("non-existent-file.csv")
        }
        assertTrue(exception.message!!.contains("File not found"))
    }
    
    @Test
    fun `should throw exception for empty file`() {
        val file = createTempFile("")
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertEquals("File is empty", exception.message)
    }
    
    @Test
    fun `should throw exception for missing header`() {
        val csvContent = """
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertEquals("Header line is missing", exception.message)
    }
    
    @Test
    fun `should throw exception for missing trailer`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertEquals("Trailer line is missing", exception.message)
    }
    
    @Test
    fun `should throw exception for multiple headers`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            H|ANOTHER_SERVER_ID
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertEquals("Multiple header lines found", exception.message)
    }
    
    @Test
    fun `should throw exception for multiple trailers`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|1
            T|1
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertEquals("Multiple trailer lines found", exception.message)
    }
    
    @Test
    fun `should throw exception for invalid line format`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            INVALID_LINE
            T|0
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertTrue(exception.message!!.contains("Invalid line format at line 2"))
    }
    
    @Test
    fun `should throw exception for header with wrong number of parts`() {
        val csvContent = """
            H|SERVER_ID|EXTRA_PART
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertTrue(exception.message!!.contains("Header line must have exactly 2 parts"))
    }
    
    @Test
    fun `should throw exception for record with wrong number of parts`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            R|123400000000000|123400000000060|A5123456700000250
            T|1
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertTrue(exception.message!!.contains("Record line must have exactly 5 parts"))
    }
    
    @Test
    fun `should throw exception for empty server ID`() {
        val csvContent = """
            H|
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(MissingFieldException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertEquals("ServerID cannot be empty", exception.message)
    }
    
    @Test
    fun `should throw exception for empty record fields`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            R|123400000000000||A5123456700000250|UNIPOS A5
            T|1
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(MissingFieldException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertTrue(exception.message!!.contains("All record fields must be non-empty"))
    }
    
    @Test
    fun `should throw exception for invalid trailer count format`() {
        val csvContent = """
            H|HDKF190320201903202020032020171931
            R|123400000000000|123400000000060|A5123456700000250|UNIPOS A5
            T|not_a_number
        """.trimIndent()
        
        val file = createTempFile(csvContent)
        
        val exception = assertThrows(InvalidFileFormatException::class.java) {
            parser.parse(file.absolutePath)
        }
        assertTrue(exception.message!!.contains("Invalid record count format"))
    }
    
    private fun createTempFile(content: String): File {
        val file = tempDir.resolve("test.csv").toFile()
        file.writeText(content)
        return file
    }
} 