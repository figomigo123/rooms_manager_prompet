class RoomModel {
  final String roomId;
  final String roomName;
  final String description;
  final String ownerId;
  final int maxUsers;
  final int currentUsers;
  final String status;
  final RoomSettings? settings;

  RoomModel({
    required this.roomId,
    required this.roomName,
    required this.description,
    required this.ownerId,
    required this.maxUsers,
    required this.currentUsers,
    this.status = 'ACTIVE',
    this.settings,
  });

  factory RoomModel.fromJson(Map<String, dynamic> json) {
    return RoomModel(
      roomId: json['roomId'] ?? '',
      roomName: json['roomName'] ?? '',
      description: json['description'] ?? '',
      ownerId: json['ownerId'] ?? '',
      maxUsers: json['maxUsers'] ?? 200,
      currentUsers: json['currentUsers'] ?? 0,
      status: json['status'] ?? 'ACTIVE',
      settings: json['settings'] != null
          ? RoomSettings.fromJson(json['settings'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'roomId': roomId,
      'roomName': roomName,
      'description': description,
      'ownerId': ownerId,
      'maxUsers': maxUsers,
      'currentUsers': currentUsers,
      'status': status,
      'settings': settings?.toJson(),
    };
  }

  RoomModel copyWith({
    String? roomId,
    String? roomName,
    String? description,
    String? ownerId,
    int? maxUsers,
    int? currentUsers,
    String? status,
    RoomSettings? settings,
  }) {
    return RoomModel(
      roomId: roomId ?? this.roomId,
      roomName: roomName ?? this.roomName,
      description: description ?? this.description,
      ownerId: ownerId ?? this.ownerId,
      maxUsers: maxUsers ?? this.maxUsers,
      currentUsers: currentUsers ?? this.currentUsers,
      status: status ?? this.status,
      settings: settings ?? this.settings,
    );
  }
}

class RoomSettings {
  final bool audioEnabled;
  final bool videoEnabled;
  final bool screenshareEnabled;
  final bool whiteboardEnabled;
  final bool chatEnabled;

  RoomSettings({
    this.audioEnabled = true,
    this.videoEnabled = true,
    this.screenshareEnabled = true,
    this.whiteboardEnabled = false,
    this.chatEnabled = true,
  });

  factory RoomSettings.fromJson(Map<String, dynamic> json) {
    return RoomSettings(
      audioEnabled: json['audioEnabled'] ?? true,
      videoEnabled: json['videoEnabled'] ?? true,
      screenshareEnabled: json['screenshareEnabled'] ?? true,
      whiteboardEnabled: json['whiteboardEnabled'] ?? false,
      chatEnabled: json['chatEnabled'] ?? true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'audioEnabled': audioEnabled,
      'videoEnabled': videoEnabled,
      'screenshareEnabled': screenshareEnabled,
      'whiteboardEnabled': whiteboardEnabled,
      'chatEnabled': chatEnabled,
    };
  }
}
