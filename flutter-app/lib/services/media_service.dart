import 'package:flutter_webrtc/flutter_webrtc.dart';
import 'package:logger/logger.dart';

class MediaService {
  final Logger _logger = Logger();
  late RTCPeerConnection _peerConnection;

  static const Map<String, dynamic> iceServers = {
    'iceServers': [
      {'urls': ['stun:stun.l.google.com:19302']},
      {'urls': ['stun:stun1.l.google.com:19302']},
    ]
  };

  static const Map<String, dynamic> constraints = {
    'mandatory': {
      'OfferToReceiveAudio': true,
      'OfferToReceiveVideo': true,
    },
    'optional': [],
  };

  Future<RTCPeerConnection> createPeerConnection() async {
    try {
      _peerConnection = await createPeerConnection(iceServers, constraints);

      _peerConnection.onConnectionState = (RTCConnectionState state) {
        _logger.i('Connection state: $state');
      };

      _peerConnection.onIceCandidate = (RTCIceCandidate candidate) {
        _logger.i('ICE candidate: $candidate');
      };

      return _peerConnection;
    } catch (e) {
      _logger.e('Error creating peer connection: $e');
      rethrow;
    }
  }

  Future<MediaStream> getLocalMediaStream({
    bool audio = true,
    bool video = true,
  }) async {
    try {
      final mediaConstraints = {
        'audio': audio,
        'video': video
            ? {
                'mandatory': {
                  'minWidth': 320,
                  'minHeight': 240,
                  'minFrameRate': 15,
                },
                'facingMode': 'user',
              }
            : false,
      };

      final stream = await navigator.mediaDevices.getUserMedia(mediaConstraints);
      _logger.i('Local media stream obtained: ${stream.id}');
      return stream;
    } catch (e) {
      _logger.e('Error getting user media: $e');
      rethrow;
    }
  }

  Future<void> addAudioTrack(MediaStream stream) async {
    try {
      final audioTrack = stream.getAudioTracks().first;
      await _peerConnection.addTrack(audioTrack, stream);
      _logger.i('Audio track added');
    } catch (e) {
      _logger.e('Error adding audio track: $e');
      rethrow;
    }
  }

  Future<void> addVideoTrack(MediaStream stream) async {
    try {
      final videoTrack = stream.getVideoTracks().first;
      await _peerConnection.addTrack(videoTrack, stream);
      _logger.i('Video track added');
    } catch (e) {
      _logger.e('Error adding video track: $e');
      rethrow;
    }
  }

  Future<RTCSessionDescription?> createOffer() async {
    try {
      final offer = await _peerConnection.createOffer(constraints);
      await _peerConnection.setLocalDescription(offer);
      _logger.i('Offer created');
      return offer;
    } catch (e) {
      _logger.e('Error creating offer: $e');
      rethrow;
    }
  }

  Future<void> setRemoteDescription(RTCSessionDescription description) async {
    try {
      await _peerConnection.setRemoteDescription(description);
      _logger.i('Remote description set');
    } catch (e) {
      _logger.e('Error setting remote description: $e');
      rethrow;
    }
  }

  Future<void> addIceCandidate(RTCIceCandidate candidate) async {
    try {
      await _peerConnection.addCandidate(candidate);
      _logger.i('ICE candidate added');
    } catch (e) {
      _logger.e('Error adding ICE candidate: $e');
    }
  }

  Future<void> closePeerConnection() async {
    try {
      await _peerConnection.close();
      _logger.i('Peer connection closed');
    } catch (e) {
      _logger.e('Error closing peer connection: $e');
    }
  }
}
