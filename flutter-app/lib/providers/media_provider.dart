import 'package:flutter/foundation.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import '../services/media_service.dart';

class MediaProvider extends ChangeNotifier {
  final MediaService _mediaService = MediaService();

  RTCPeerConnection? _peerConnection;
  List<MediaStream> _remoteStreams = [];
  bool _isAudioEnabled = true;
  bool _isVideoEnabled = true;
  bool _isSpeaker = false;

  RTCPeerConnection? get peerConnection => _peerConnection;
  List<MediaStream> get remoteStreams => _remoteStreams;
  bool get isAudioEnabled => _isAudioEnabled;
  bool get isVideoEnabled => _isVideoEnabled;
  bool get isSpeaker => _isSpeaker;

  Future<void> initializeMediaConnection({
    required String roomId,
    required String userId,
    required String accessToken,
  }) async {
    try {
      _peerConnection = await _mediaService.createPeerConnection();
      notifyListeners();
    } catch (e) {
      rethrow;
    }
  }

  Future<bool> toggleAudio() async {
    _isAudioEnabled = !_isAudioEnabled;
    notifyListeners();
    return _isAudioEnabled;
  }

  Future<bool> toggleVideo() async {
    _isVideoEnabled = !_isVideoEnabled;
    notifyListeners();
    return _isVideoEnabled;
  }

  Future<bool> toggleSpeaker() async {
    _isSpeaker = !_isSpeaker;
    notifyListeners();
    return _isSpeaker;
  }

  void addRemoteStream(MediaStream stream) {
    _remoteStreams.add(stream);
    notifyListeners();
  }

  void removeRemoteStream(String streamId) {
    _remoteStreams.removeWhere((stream) => stream.id == streamId);
    notifyListeners();
  }

  Future<void> dispose() async {
    await _peerConnection?.close();
    _remoteStreams.clear();
    notifyListeners();
  }
}
