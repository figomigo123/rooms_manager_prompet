#!/bin/bash

# Rooms Manager Flutter Test App - Setup Script

echo "Setting up Flutter app..."

# Get dependencies
flutter pub get

# Run code generation if needed
flutter pub run build_runner build --delete-conflicting-outputs

echo "Setup complete!"
echo ""
echo "To run the app:"
echo "  flutter run"
echo ""
echo "To run tests:"
echo "  flutter test"
echo ""
echo "To build APK:"
echo "  flutter build apk"
echo ""
echo "To build iOS:"
echo "  flutter build ios"
