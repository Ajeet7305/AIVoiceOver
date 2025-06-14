package com.csvparser.parser

import com.csvparser.exception.*
import com.csvparser.model.*
import java.io.File

/**
 * Parser for pipe-delimited CSV files containing device information
 */
class CsvParser {
    
    companion object {
        private const val DELIMITER = "|"
        private const val HEADER_PREFIX = "H"
        private const val RECORD_PREFIX = "R"
        private const val TRAILER_PREFIX = "T"
    }
    
    /**
     * Parses a CSV file and returns the structured data
     */
    fun parse(filePath: String): ParseResult {
        val file = File(filePath)
        if (!file.exists()) {
            throw InvalidFileFormatException("File not found: $filePath")
        }
        
        val lines = file.readLines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            throw InvalidFileFormatException("File is empty")
        }
        
        var header: Header? = null
        val deviceLines = mutableListOf<DeviceLine>()
        var trailer: Trailer? = null
        
        lines.forEachIndexed { index, line ->
            try {
                when {
                    line.startsWith(HEADER_PREFIX + DELIMITER) -> {
                        if (header != null) {
                            throw InvalidFileFormatException("Multiple header lines found")
                        }
                        header = parseHeader(line)
                    }
                    line.startsWith(RECORD_PREFIX + DELIMITER) -> {
                        deviceLines.add(parseRecord(line))
                    }
                    line.startsWith(TRAILER_PREFIX + DELIMITER) -> {
                        if (trailer != null) {
                            throw InvalidFileFormatException("Multiple trailer lines found")
                        }
                        trailer = parseTrailer(line)
                    }
                    else -> {
                        throw InvalidFileFormatException("Invalid line format at line ${index + 1}: $line")
                    }
                }
            } catch (e: Exception) {
                when (e) {
                    is CsvParsingException -> throw e
                    else -> throw InvalidFileFormatException("Error parsing line ${index + 1}: ${e.message}")
                }
            }
        }
        
        return ParseResult(
            header = header ?: throw InvalidFileFormatException("Header line is missing"),
            deviceLines = deviceLines,
            trailer = trailer ?: throw InvalidFileFormatException("Trailer line is missing")
        )
    }
    
    private fun parseHeader(line: String): Header {
        val parts = line.split(DELIMITER)
        if (parts.size != 2) {
            throw InvalidFileFormatException("Header line must have exactly 2 parts: $line")
        }
        
        val serverID = parts[1].trim()
        if (serverID.isBlank()) {
            throw MissingFieldException("ServerID cannot be empty")
        }
        
        return Header(serverID)
    }
    
    private fun parseRecord(line: String): DeviceLine {
        val parts = line.split(DELIMITER)
        if (parts.size != 5) {
            throw InvalidFileFormatException("Record line must have exactly 5 parts: $line")
        }
        
        val imei1 = parts[1].trim()
        val imei2 = parts[2].trim()
        val serialNumber = parts[3].trim()
        val deviceName = parts[4].trim()
        
        if (imei1.isBlank() || imei2.isBlank() || serialNumber.isBlank() || deviceName.isBlank()) {
            throw MissingFieldException("All record fields must be non-empty: $line")
        }
        
        return DeviceLine(imei1, imei2, serialNumber, deviceName)
    }
    
    private fun parseTrailer(line: String): Trailer {
        val parts = line.split(DELIMITER)
        if (parts.size != 2) {
            throw InvalidFileFormatException("Trailer line must have exactly 2 parts: $line")
        }
        
        val countStr = parts[1].trim()
        if (countStr.isBlank()) {
            throw MissingFieldException("Record count cannot be empty")
        }
        
        val count = try {
            countStr.toInt()
        } catch (e: NumberFormatException) {
            throw InvalidFileFormatException("Invalid record count format: $countStr")
        }
        
        return Trailer(count)
    }
}

/**
 * Container for parsed CSV data
 */
data class ParseResult(
    val header: Header,
    val deviceLines: List<DeviceLine>,
    val trailer: Trailer
) 