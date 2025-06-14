package com.csvparser.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a device record line in the CSV file (R|IMEI1|IMEI2|SerialNumber|DeviceName)
 */
data class DeviceLine(
    @JsonProperty("IMEI1")
    val imei1: String,
    
    @JsonProperty("IMEI2") 
    val imei2: String,
    
    @JsonProperty("Serialnumber")
    val serialNumber: String,
    
    @JsonProperty("DeviceName")
    val deviceName: String
) 