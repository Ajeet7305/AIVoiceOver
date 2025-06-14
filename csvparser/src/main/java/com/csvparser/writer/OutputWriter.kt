package com.csvparser.writer

import com.csvparser.model.DeviceReport
import com.csvparser.model.DeviceReportWrapper
import com.csvparser.parser.ParseResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

/**
 * Interface for output writers
 */
interface OutputWriter {
    fun write(parseResult: ParseResult, outputPath: String)
}

/**
 * JSON output writer using Jackson
 */
class JsonOutputWriter : OutputWriter {
    
    private val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
        enable(SerializationFeature.INDENT_OUTPUT)
    }
    
    override fun write(parseResult: ParseResult, outputPath: String) {
        val deviceReport = DeviceReport(
            serverID = parseResult.header.serverID,
            deviceLines = parseResult.deviceLines
        )
        
        val wrapper = DeviceReportWrapper(
            deviceDetails = listOf(deviceReport)
        )
        
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        
        objectMapper.writeValue(outputFile, wrapper)
    }
    
    /**
     * Returns the JSON string representation
     */
    fun toJsonString(parseResult: ParseResult): String {
        val deviceReport = DeviceReport(
            serverID = parseResult.header.serverID,
            deviceLines = parseResult.deviceLines
        )
        
        val wrapper = DeviceReportWrapper(
            deviceDetails = listOf(deviceReport)
        )
        
        return objectMapper.writeValueAsString(wrapper)
    }
}

/**
 * XML output writer using Jackson XML
 */
class XmlOutputWriter : OutputWriter {
    
    private val xmlMapper = XmlMapper().apply {
        registerKotlinModule()
        enable(SerializationFeature.INDENT_OUTPUT)
    }
    
    override fun write(parseResult: ParseResult, outputPath: String) {
        val deviceReport = DeviceReport(
            serverID = parseResult.header.serverID,
            deviceLines = parseResult.deviceLines
        )
        
        val wrapper = DeviceReportWrapper(
            deviceDetails = listOf(deviceReport)
        )
        
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        
        xmlMapper.writeValue(outputFile, wrapper)
    }
    
    /**
     * Returns the XML string representation
     */
    fun toXmlString(parseResult: ParseResult): String {
        val deviceReport = DeviceReport(
            serverID = parseResult.header.serverID,
            deviceLines = parseResult.deviceLines
        )
        
        val wrapper = DeviceReportWrapper(
            deviceDetails = listOf(deviceReport)
        )
        
        return xmlMapper.writeValueAsString(wrapper)
    }
} 