# Flutter Rooms Manager - Test Application

## Overview

Comprehensive Flutter test application for validating the Rooms Manager communication SaaS platform. Supports audio/video streaming, real-time communication, and end-to-end testing.

## Features

### 🔐 Authentication
- App ID & Secret login
- JWT token management
- Secure credential storage
- Auto-token validation

### 🚪 Room Management
- View available rooms
- Create new rooms
- Join rooms with real-time updates
- Room capacity monitoring
- Room settings configuration

### 📹 Audio/Video Streaming
- **WebRTC Integration**: flutter_webrtc library
- **Audio**: Mic control, speaker toggle
- **Video**: Camera feed with PiP (Picture-in-Picture)
- **Media Constraints**: HD quality, 15-30 fps
- **Real-time Streaming**: Multiple peers support

### 🎮 Media Controls
- Mute/Unmute audio
- Enable/Disable video
- Speaker/Headset toggle
- End call functionality

### 📊 Statistics & Monitoring
- Real-time user count
- Room capacity tracking
- Connection status monitoring
- Latency & bitrate tracking

## Tech Stack

```yaml
Core:
  - Flutter 3.0+
  - Dart 3.0+

Networking:
  - http: ^1.1.0
  - dio: ^5.3.0
  - socket_io_client: ^2.0.0

WebRTC:
  - flutter_webrtc: ^0.9.47
  - sdp_transform: ^0.3.0

State Management:
  - provider: ^6.0.0
  - get: ^4.6.5

UI Components:
  - cupertino_icons
  - google_fonts
  - cached_network_image
  - shimmer

Utilities:
  - uuid: ^4.0.0
  - intl: ^0.19.0
  - logger: ^2.0.0
  - shared_preferences: ^2.2.0
  - permission_handler: ^11.4.4
```

## Project Structure

```
flutter-app/
├── lib/
│   ├── main.dart                      # Entry point
│   ├── config/
│   │   └── api_config.dart           # API endpoints
│   ├── models/
│   │   ├── user_model.dart           # User data model
│   │   └── room_model.dart           # Room data model
│   ├── providers/
│   │   ├── auth_provider.dart        # Authentication state
│   │   ├── room_provider.dart        # Room management state
│   │   └── media_provider.dart       # Media state
│   ├── services/
│   │   ├── api_service.dart          # HTTP client
│   │   └── media_service.dart        # WebRTC service
│   └── screens/
│       ├── auth/
│       │   └── login_screen.dart
│       ├── home/
│       │   └── home_screen.dart
│       ├── rooms/
│       │   └── rooms_list_screen.dart
│       └── room/
│           ├── room_detail_screen.dart
│           └── create_room_screen.dart
├── pubspec.yaml                       # Dependencies
├── setup.sh                           # Setup script
└── README.md                          # This file
```

## Getting Started

### Prerequisites
- Flutter SDK 3.0+
- Dart 3.0+
- iOS 11+ or Android 5.0+
- Xcode (for iOS) or Android Studio

### Installation

1. **Clone Repository**
```bash
git clone <repo>
cd flutter-app
```

2. **Install Dependencies**
```bash
flutter pub get
```

3. **Configure API Endpoints**
Edit `lib/config/api_config.dart`:
```dart
static const String baseUrl = 'http://your-server:8001';
```

4. **Run Setup**
```bash
bash setup.sh
```

### Running the App

**Debug Mode (Device/Emulator)**
```bash
flutter run
```

**Debug Mode (Web)**
```bash
flutter run -d chrome
```

**Release Mode**
```bash
flutter run --release
```

## Usage

### Login
1. Enter App ID and App Secret
2. Tap "Login" button
3. Redirected to home screen on success

### Create Room
1. Tap "Create Room" button
2. Fill in room details
3. Configure room features (Audio, Video, Screen, Chat)
4. Tap "Create" to create the room

### Join Room
1. Select a room from the list
2. Tap "Join" button
3. Grant camera/microphone permissions when prompted
4. Start video/audio communication

### Media Controls
During a call:
- **Mic Icon**: Mute/unmute audio
- **Camera Icon**: Enable/disable video
- **Volume Icon**: Toggle speaker/headset
- **End Call**: Red button to end the call

## API Integration

Connects to multiple backend services:

### Auth Service (8001)
```
POST /auth/v1/login
  ├─ appId
  ├─ appSecret
  └─ Returns: appToken

POST /auth/v1/verify-token
  └─ Bearer {token}
```

### Room Service (8002)
```
GET /room/v1/rooms
  └─ Bearer {token}
  └─ Returns: [RoomModel]

POST /room/v1/rooms
  ├─ Bearer {token}
  └─ Body: RoomModel

GET /room/v1/media/access-token?roomId=X&userId=Y&userName=Z
  └─ Bearer {token}
  └─ Returns: {token, mediasoupUrl}
```

### MediaSoup Service (8003)
```
WebSocket Connection
  ├─ URL: ws://localhost:8003
  ├─ Token: {accessToken}
  └─ Events: producer-added, consumer-added, user-joined, etc.
```

## Testing

### Unit Tests
```bash
flutter test
```

### Widget Tests
```bash
flutter test test/widget_test.dart
```

### Integration Tests
```bash
flutter test integration_test/
```

### Manual Testing Scenarios

#### Scenario 1: Authentication
- [ ] Login with valid credentials
- [ ] Login with invalid credentials shows error
- [ ] Token is persisted locally
- [ ] Logout clears token

#### Scenario 2: Room Management
- [ ] Load room list
- [ ] Create new room
- [ ] Delete room
- [ ] Room capacity updates in real-time

#### Scenario 3: Media Streaming
- [ ] Grant permissions on first join
- [ ] Video displays on joining room
- [ ] Audio works bidirectionally
- [ ] Mute/unmute toggles correctly
- [ ] Video enable/disable works
- [ ] Speaker/headset toggle works

#### Scenario 4: Multi-user
- [ ] Open two app instances
- [ ] User 1 creates room
- [ ] User 2 joins room
- [ ] Both see each other's video
- [ ] Audio communication works
- [ ] User count updates correctly

#### Scenario 5: Network Resilience
- [ ] App reconnects on network loss
- [ ] Graceful error handling
- [ ] Offline queue for messages (if applicable)

## Performance Optimization

- **WebRTC Constraints**: Adaptive quality based on network
- **Memory Management**: Proper disposal of resources
- **Image Caching**: Reduces network calls
- **Provider Pattern**: Efficient state updates
- **Lazy Loading**: On-demand widget initialization

## Debugging

### Enable Debug Logging
```dart
import 'package:logger/logger.dart';

final logger = Logger();
logger.d('Debug message');
```

### Enable WebRTC Logging
```dart
import 'package:flutter_webrtc/flutter_webrtc.dart';

await WebRTC.setOptions({
  'webrtc': {
    'logLevel': 'debug',
  },
});
```

### Common Issues

**Permissions Not Granted**
- Check AndroidManifest.xml (Android)
- Check Info.plist (iOS)
- Handle permission requests in code

**WebRTC Connection Fails**
- Verify TURN/STUN servers are accessible
- Check firewall settings
- Verify backend services are running

**API Connection Errors**
- Verify baseUrl in api_config.dart
- Check backend service health
- Check network connectivity

## Building for Production

### Android
```bash
flutter build apk --release
flutter build appbundle --release
```

### iOS
```bash
flutter build ios --release
```

### Web
```bash
flutter build web --release
```

## Deployment

### Google Play Store
```bash
flutter build appbundle --release
# Upload to Google Play Console
```

### Apple App Store
```bash
flutter build ios --release
# Upload using Xcode or transporter
```

## Troubleshooting

### Build Errors
```bash
flutter clean
flutter pub get
flutter pub upgrade
flutter build <platform>
```

### Runtime Errors
- Check logcat/Xcode logs
- Use DevTools for debugging
- Enable verbose logging

### Performance Issues
- Profile with DevTools
- Check memory usage
- Optimize video quality settings

## Contributing

Feel free to submit issues and enhancement requests!

## License

MIT - See LICENSE file for details

## Support

For issues and questions:
- GitHub Issues
- GitHub Discussions
- Email support
