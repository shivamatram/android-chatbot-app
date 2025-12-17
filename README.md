# Chatbot App

An Android chatbot application with voice input, image attachment, and conversation management features.

## Features

- 💬 **Interactive Chat Interface** - Clean and modern Material Design UI
- 🎤 **Voice Input** - Speak to the chatbot using voice recognition
- 📎 **Attachment Support** - Send images and files to the chatbot
- ⚙️ **Settings** - Customize your chatbot experience
- 💾 **Conversation Management** - Save and load chat history
- 🌐 **Network Communication** - Connect to external chatbot APIs

## Requirements

- Android Studio (latest version recommended)
- Android SDK 24 or higher
- Java 11 or higher
- Gradle 8.x

## Permissions

The app requires the following permissions:
- `INTERNET` - For API communication
- `RECORD_AUDIO` - For voice input feature
- `CAMERA` - For taking photos to send
- `READ_EXTERNAL_STORAGE` - For selecting images
- `WRITE_EXTERNAL_STORAGE` - For saving attachments

## Project Structure

```
ChatBot/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/chatbot_app/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SettingsActivity.java
│   │   │   │   ├── VoiceInputHandler.java
│   │   │   │   └── AttachmentHandler.java
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle
├── gradle/
├── build.gradle
└── settings.gradle
```

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/shivamatram/android-chatbot-app.git
cd ChatBot
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Click "Open an Existing Project"
3. Navigate to the ChatBot directory
4. Wait for Gradle sync to complete

### 3. Build the Project

```bash
./gradlew build
```

Or use Android Studio's Build menu:
- Build → Make Project

### 4. Run the App

1. Connect an Android device or start an emulator
2. Click the "Run" button in Android Studio
3. Select your target device

## Configuration

### API Settings

Configure your chatbot API endpoint in the Settings activity:
- Open the app
- Tap the menu button (three dots)
- Select "Settings"
- Enter your API URL and other preferences

### Network Security

The app uses a custom network security configuration. Check `network_security_config.xml` for HTTPS settings.

## Development

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Cleaning Build Files

```bash
./gradlew clean
```

## Key Components

### MainActivity
The main chat interface with message handling, UI management, and API communication.

### VoiceInputHandler
Manages voice recognition and speech-to-text conversion.

### AttachmentHandler
Handles image selection, camera capture, and file attachments.

### SettingsActivity
User preferences and app configuration.

## Dependencies

- AndroidX AppCompat
- Material Components
- ConstraintLayout
- JUnit (testing)
- Espresso (UI testing)

See [app/build.gradle](app/build.gradle) for complete dependency list.

## Troubleshooting

### Gradle Sync Issues
- Ensure you have a stable internet connection
- Try `File → Invalidate Caches / Restart`
- Check that your Android SDK is up to date

### Permission Denied Errors
- Grant all required permissions in Android settings
- For API 23+, runtime permissions are handled by the app

### Network Errors
- Verify your API endpoint URL
- Check network connectivity
- Ensure HTTPS certificates are valid

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is available for educational and personal use.

## Version History

- **1.0** (Current)
  - Initial release
  - Basic chat functionality
  - Voice input support
  - Image attachments
  - Settings management

## Contact

For questions or support, please open an issue in the repository.

email: shivamatram2002@gmail.com

---

Built with ❤️ using Android Studio
