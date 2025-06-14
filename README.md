# Android Interview Assistant 🎤

A comprehensive Android application built with **MVVM + Clean Architecture** that helps users prepare for technical interviews using AI-powered assistance and voice recognition.

## 🚀 Features

- **AI-Powered Interview Assistance** - OpenAI integration for intelligent responses
- **Voice Recognition & Translation** - Real-time speech processing
- **Company Management** - Track and manage interview opportunities
- **Interview Sessions** - Structured interview practice sessions
- **Dashboard Analytics** - Performance tracking and insights
- **Modern UI** - Built with Jetpack Compose

## 🏗️ Architecture

This project follows **Clean Architecture** principles with **MVVM** pattern:

```
📁 aivoice/
├── 📁 data/           # Data layer (repositories, APIs, database)
├── 📁 domain/         # Domain layer (use cases, models, repositories)
├── 📁 presentation/   # Presentation layer (ViewModels, UI)
├── 📁 di/            # Dependency injection modules
└── 📁 ui/            # UI theme and components
```

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **AI Integration**: OpenAI API
- **Audio Processing**: TensorFlow Lite
- **Testing**: JUnit, Mockito, Coroutines Test

## 📊 Test Coverage

- **178 Unit Tests** with **100% Success Rate**
- **13.2% Code Coverage** (4,071 of 30,885 instructions)
- Comprehensive test suite covering all layers
- JaCoCo integration for coverage reporting

### Run Tests & Coverage
```bash
# Run tests with coverage
./gradlew :aivoice:test :aivoice:jacocoTestReport

# Generate test summary report
./test-summary.sh --coverage
```

## 🔧 Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/Ajeet7305/AIVoiceOver.git
cd AIVoiceOver
```

### 2. Configure OpenAI API Key

**⚠️ Important**: Never commit your actual API key to version control!

#### Option A: Environment Variable (Recommended)
```bash
export OPENAI_API_KEY="your-actual-api-key-here"
```

#### Option B: Local Properties File
Create `local.properties` in the root directory:
```properties
OPENAI_API_KEY=your-actual-api-key-here
```

Then update `aivoice/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        // Read from local.properties
        val localProperties = Properties()
        localProperties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY")}\"")
    }
}
```

### 3. Build and Run
```bash
./gradlew :aivoice:assembleDebug
```

## 📱 App Modules

### Main Features
- **Dashboard**: Overview of interview progress and analytics
- **Interview Sessions**: Practice technical interviews with AI assistance
- **Company Management**: Track applications and interview opportunities
- **Translator**: Real-time voice translation and transcription

### Supporting Modules
- **Voice Classifier**: Audio classification and processing

## 🧪 Testing

The project includes comprehensive testing with custom reporting tools:

### Test Summary Report
```bash
# Quick test summary
./test-summary.sh

# Run tests first, then show summary
./test-summary.sh --run-tests

# Generate coverage and show summary
./test-summary.sh --coverage
```

### Coverage by Layer
- **Domain Layer**: 69.4% coverage (excellent)
- **Data Layer**: 46.7% coverage (good)
- **Presentation Layer**: 10.0% coverage (needs improvement)
- **UI Layer**: 0.0% coverage (critical)

## 🔒 Security

- API keys are not stored in version control
- GitHub push protection enabled
- Secure credential management practices

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Contact

**Ajeet Yadav** - [GitHub Profile](https://github.com/Ajeet7305)

Project Link: [https://github.com/Ajeet7305/AIVoiceOver](https://github.com/Ajeet7305/AIVoiceOver)

---

⭐ **Star this repository if you find it helpful!** 