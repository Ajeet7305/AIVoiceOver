package com.csvparser

import com.csvparser.exception.CsvParsingException
import com.csvparser.service.CsvProcessingService
import com.csvparser.service.OutputFormat
import org.apache.commons.cli.*
import java.io.File
import kotlin.system.exitProcess

/**
 * Main CLI application for CSV parsing
 */
fun main(args: Array<String>) {
    val app = CsvParserApp()
    app.run(args)
}

class CsvParserApp {
    
    private val service = CsvProcessingService()
    
    fun run(args: Array<String>) {
        try {
            val commandLine = parseArguments(args)
            
            val inputFile = commandLine.getOptionValue("file")
            val outputFormat = determineOutputFormat(commandLine)
            val outputFile = commandLine.getOptionValue("output")
            
            if (outputFile != null) {
                // Write to file
                service.processFile(inputFile, outputFile, outputFormat)
                println("✅ Successfully processed '$inputFile' and saved output to '$outputFile'")
            } else {
                // Print to console
                val result = service.processFileToString(inputFile, outputFormat)
                println(result)
            }
            
        } catch (e: ParseException) {
            printUsage()
            exitProcess(1)
        } catch (e: CsvParsingException) {
            println("❌ Error processing CSV file: ${e.message}")
            exitProcess(1)
        } catch (e: Exception) {
            println("❌ Unexpected error: ${e.message}")
            e.printStackTrace()
            exitProcess(1)
        }
    }
    
    private fun parseArguments(args: Array<String>): CommandLine {
        val options = Options().apply {
            addOption(
                Option.builder("f")
                    .longOpt("file")
                    .desc("Path to the input CSV file")
                    .hasArg()
                    .required(true)
                    .build()
            )
            
            addOption(
                Option.builder("o")
                    .longOpt("output")
                    .desc("Path to the output file (optional, prints to console if not provided)")
                    .hasArg()
                    .required(false)
                    .build()
            )
            
            addOption(
                Option.builder()
                    .longOpt("json")
                    .desc("Output in JSON format (default)")
                    .required(false)
                    .build()
            )
            
            addOption(
                Option.builder()
                    .longOpt("xml")
                    .desc("Output in XML format")
                    .required(false)
                    .build()
            )
            
            addOption(
                Option.builder("h")
                    .longOpt("help")
                    .desc("Show this help message")
                    .required(false)
                    .build()
            )
        }
        
        val parser = DefaultParser()
        val commandLine = parser.parse(options, args)
        
        if (commandLine.hasOption("help")) {
            printUsage()
            exitProcess(0)
        }
        
        // Validate input file exists
        val inputFile = File(commandLine.getOptionValue("file"))
        if (!inputFile.exists()) {
            throw IllegalArgumentException("Input file does not exist: ${inputFile.absolutePath}")
        }
        
        return commandLine
    }
    
    private fun determineOutputFormat(commandLine: CommandLine): OutputFormat {
        return when {
            commandLine.hasOption("xml") -> OutputFormat.XML
            else -> OutputFormat.JSON // Default to JSON
        }
    }
    
    private fun printUsage() {
        println("""
            CSV Parser - Converts pipe-delimited device reports to JSON/XML
            
            Usage: csv-parser --file <input-file> [options]
            
            Required:
              -f, --file <path>     Path to input CSV file
            
            Optional:
              -o, --output <path>   Output file path (prints to console if not provided)
              --json                Output in JSON format (default)
              --xml                 Output in XML format
              -h, --help            Show this help message
            
            Examples:
              csv-parser --file devices.csv --json
              csv-parser --file devices.csv --output report.json
              csv-parser --file devices.csv --xml --output report.xml
              
            Input Format:
              H|ServerID
              R|IMEI1|IMEI2|SerialNumber|DeviceName
              ...
              T|RecordCount
        """.trimIndent())
    }
} 