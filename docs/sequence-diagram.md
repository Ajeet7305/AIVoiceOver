# CSV Parser Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant CLI as CsvParserApp
    participant Service as CsvProcessingService
    participant Parser as CsvParser
    participant Validator as CsvValidator
    participant Writer as OutputWriter
    participant File as Output File

    User->>CLI: Run with args (file, format)
    CLI->>CLI: Parse command line arguments
    CLI->>Service: processFile(inputPath, outputPath, format)
    
    Service->>Parser: parse(inputFilePath)
    Parser->>Parser: Read file line by line
    Parser->>Parser: Parse header (H|...)
    Parser->>Parser: Parse records (R|...)
    Parser->>Parser: Parse trailer (T|...)
    Parser-->>Service: Return ParseResult
    
    Service->>Validator: validate(parseResult)
    Validator->>Validator: Check data integrity
    Validator->>Validator: Validate IMEI formats
    Validator->>Validator: Check record count match
    Validator-->>Service: Validation complete
    
    Service->>Writer: write(parseResult, outputPath)
    Writer->>Writer: Convert to JSON/XML
    Writer->>File: Write formatted output
    Writer-->>Service: Write complete
    
    Service-->>CLI: Processing complete
    CLI-->>User: Success message
```

## Error Flow

```mermaid
sequenceDiagram
    participant User
    participant CLI as CsvParserApp
    participant Service as CsvProcessingService
    participant Parser as CsvParser
    participant Validator as CsvValidator

    User->>CLI: Run with invalid file
    CLI->>Service: processFile(...)
    
    alt File parsing error
        Service->>Parser: parse(inputFilePath)
        Parser-->>Service: Throw InvalidFileFormatException
        Service-->>CLI: Throw CsvParsingException
        CLI-->>User: Display error message
    else Validation error
        Service->>Parser: parse(inputFilePath)
        Parser-->>Service: Return ParseResult
        Service->>Validator: validate(parseResult)
        Validator-->>Service: Throw ValidationException
        Service-->>CLI: Throw CsvParsingException
        CLI-->>User: Display error message
    end
```

## Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLI Layer                            │
│  ┌─────────────────┐  ┌─────────────────────────────────┐   │
│  │     Main.kt     │  │        CsvParserApp             │   │
│  │                 │  │  - Argument parsing             │   │
│  │                 │  │  - Error handling               │   │
│  └─────────────────┘  └─────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              CsvProcessingService                   │   │
│  │  - Orchestrates parsing, validation, writing       │   │
│  │  - Manages output format selection                 │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────┐
│                   Business Logic Layer                     │
│  ┌─────────────────┐              ┌─────────────────────┐   │
│  │    CsvParser    │              │    CsvValidator     │   │
│  │  - Line parsing │              │  - Data validation  │   │
│  │  - Format check │              │  - Business rules   │   │
│  └─────────────────┘              └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    Output Layer                            │
│  ┌─────────────────┐              ┌─────────────────────┐   │
│  │ JsonOutputWriter│              │ XmlOutputWriter     │   │
│  │  - JSON format  │              │  - XML format       │   │
│  │  - Jackson lib  │              │  - Jackson XML      │   │
│  └─────────────────┘              └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────┐
│                     Data Model Layer                       │
│  ┌─────────┐ ┌─────────────┐ ┌─────────┐ ┌─────────────┐   │
│  │ Header  │ │ DeviceLine  │ │ Trailer │ │DeviceReport │   │
│  │         │ │             │ │         │ │             │   │
│  └─────────┘ └─────────────┘ └─────────┘ └─────────────┘   │
└─────────────────────────────────────────────────────────────┘
``` 