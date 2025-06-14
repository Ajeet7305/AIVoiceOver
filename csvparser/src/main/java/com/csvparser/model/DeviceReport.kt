package com.csvparser.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the complete device report containing all parsed data
 */
data class DeviceReport(
    @JsonProperty("ServerID")
    val serverID: String,
    
    @JsonProperty("deviceLines")
    val deviceLines: List<DeviceLine>
)

/**
 * Root wrapper class for JSON output
 */
data class DeviceReportWrapper(
    @JsonProperty("Devicedetails")
    val deviceDetails: List<DeviceReport>
) 