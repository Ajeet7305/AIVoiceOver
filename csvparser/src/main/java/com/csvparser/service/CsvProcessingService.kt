package com.csvparser.service

import com.csvparser.parser.CsvParser
import com.csvparser.parser.ParseResult
import com.csvparser.validator.CsvValidator
import com.csvparser.writer.JsonOutputWriter
import com.csvparser.writer.OutputWriter
import com.csvparser.writer.XmlOutputWriter

/**
 * Main service class that orchestrates CSV processing
 */
class CsvProcessingService(
    private val parser: CsvParser = CsvParser(),
    private val validator: CsvValidator = CsvValidator()
) {
    
    /**
     * Processes a CSV file and generates output in the specified format
     */
    fun processFile(
        inputFilePath: String,
        outputFilePath: String,
        outputFormat: OutputFormat
    ) {
        // Parse the CSV file
        val parseResult = parser.parse(inputFilePath)
        
        // Validate the parsed data
        validator.validate(parseResult)
        
        // Write the output
        val writer = createOutputWriter(outputFormat)
        writer.write(parseResult, outputFilePath)
    }
    
    /**
     * Processes a CSV file and returns the result as a string
     */
    fun processFileToString(
        inputFilePath: String,
        outputFormat: OutputFormat
    ): String {
        // Parse the CSV file
        val parseResult = parser.parse(inputFilePath)
        
        // Validate the parsed data
        validator.validate(parseResult)
        
        // Return formatted string
        return when (outputFormat) {
            OutputFormat.JSON -> {
                val jsonWriter = JsonOutputWriter()
                jsonWriter.toJsonString(parseResult)
            }
            OutputFormat.XML -> {
                val xmlWriter = XmlOutputWriter()
                xmlWriter.toXmlString(parseResult)
            }
        }
    }
    
    private fun createOutputWriter(format: OutputFormat): OutputWriter {
        return when (format) {
            OutputFormat.JSON -> JsonOutputWriter()
            OutputFormat.XML -> XmlOutputWriter()
        }
    }
}

/**
 * Supported output formats
 */
enum class OutputFormat {
    JSON,
    XML
} 