import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../models/room_model.dart';
import '../../providers/room_provider.dart';
import '../../providers/auth_provider.dart';
import '../../providers/media_provider.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';

class RoomDetailScreen extends StatefulWidget {
  final RoomModel room;

  const RoomDetailScreen({Key? key, required this.room}) : super(key: key);

  @override
  State<RoomDetailScreen> createState() => _RoomDetailScreenState();
}

class _RoomDetailScreenState extends State<RoomDetailScreen> {
  late RTCVideoRenderer _localRenderer;
  late RTCVideoRenderer _remoteRenderer;
  bool _isInitialized = false;

  @override
  void initState() {
    super.initState();
    _initializeRenderers();
    _joinRoom();
  }

  Future<void> _initializeRenderers() async {
    _localRenderer = RTCVideoRenderer();
    _remoteRenderer = RTCVideoRenderer();
    await _localRenderer.initialize();
    await _remoteRenderer.initialize();
  }

  Future<void> _joinRoom() async {
    final auth = context.read<AuthProvider>();
    final rooms = context.read<RoomProvider>();
    final media = context.read<MediaProvider>();

    if (auth.token != null) {
      final success = await rooms.joinRoom(
        auth.token!,
        widget.room.roomId,
        auth.user!.id,
        auth.user!.name,
      );

      if (success) {
        // Initialize media
        try {
          await media.initializeMediaConnection(
            roomId: widget.room.roomId,
            userId: auth.user!.id,
            accessToken: auth.token!,
          );
          setState(() => _isInitialized = true);
        } catch (e) {
          _showError('Failed to initialize media: $e');
        }
      } else {
        _showError('Failed to join room: ${rooms.error}');
      }
    }
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  @override
  void dispose() {
    _localRenderer.dispose();
    _remoteRenderer.dispose();
    context.read<RoomProvider>().leaveRoom();
    context.read<MediaProvider>().dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.room.roomName),
        centerTitle: true,
      ),
      body: _isInitialized
          ? Column(
              children: [
                Expanded(
                  child: Consumer<MediaProvider>(
                    builder: (context, media, _) {
                      return Stack(
                        children: [
                          // Remote video
                          media.remoteStreams.isNotEmpty
                              ? RTCVideoView(
                                  _remoteRenderer,
                                  mirror: false,
                                  objectFit:
                                      RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
                                )
                              : Container(
                                  color: Colors.grey.shade900,
                                  child: const Center(
                                    child: Text(
                                      'Waiting for remote stream...',
                                      style: TextStyle(color: Colors.white),
                                    ),
                                  ),
                                ),
                          // Local video (PiP)
                          Positioned(
                            bottom: 16,
                            right: 16,
                            width: 120,
                            height: 160,
                            child: Container(
                              decoration: BoxDecoration(
                                border: Border.all(color: Colors.white, width: 2),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: RTCVideoView(
                                _localRenderer,
                                mirror: true,
                                objectFit:
                                    RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
                              ),
                            ),
                          ),
                        ],
                      );
                    },
                  ),
                ),
                // Controls
                Container(
                  padding: const EdgeInsets.all(16),
                  child: Consumer<MediaProvider>(
                    builder: (context, media, _) {
                      return Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          FloatingActionButton(
                            mini: true,
                            backgroundColor:
                                media.isAudioEnabled ? Colors.blue : Colors.red,
                            onPressed: () => media.toggleAudio(),
                            child: Icon(
                              media.isAudioEnabled
                                  ? Icons.mic
                                  : Icons.mic_off,
                            ),
                          ),
                          FloatingActionButton(
                            mini: true,
                            backgroundColor:
                                media.isVideoEnabled ? Colors.blue : Colors.red,
                            onPressed: () => media.toggleVideo(),
                            child: Icon(
                              media.isVideoEnabled
                                  ? Icons.videocam
                                  : Icons.videocam_off,
                            ),
                          ),
                          FloatingActionButton(
                            mini: true,
                            backgroundColor:
                                media.isSpeaker ? Colors.blue : Colors.grey,
                            onPressed: () => media.toggleSpeaker(),
                            child: Icon(
                              media.isSpeaker ? Icons.volume_up : Icons.volume_off,
                            ),
                          ),
                          FloatingActionButton(
                            mini: true,
                            backgroundColor: Colors.red,
                            onPressed: () => Navigator.of(context).pop(),
                            child: const Icon(Icons.call_end),
                          ),
                        ],
                      );
                    },
                  ),
                ),
              ],
            )
          : const Center(child: CircularProgressIndicator()),
    );
  }
}
