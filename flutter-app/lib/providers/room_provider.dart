import 'package:flutter/foundation.dart';
import 'package:dio/dio.dart';
import '../models/room_model.dart';
import '../services/api_service.dart';

class RoomProvider extends ChangeNotifier {
  final ApiService _apiService = ApiService();
  List<RoomModel> _rooms = [];
  RoomModel? _currentRoom;
  bool _isLoading = false;
  String? _error;

  List<RoomModel> get rooms => _rooms;
  RoomModel? get currentRoom => _currentRoom;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchRooms(String token) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final response = await _apiService.get(
        '/room/v1/rooms',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );

      _rooms = (response.data as List)
          .map((room) => RoomModel.fromJson(room))
          .toList();
      _isLoading = false;
      notifyListeners();
    } on DioException catch (e) {
      _error = e.response?.data['message'] ?? 'Failed to fetch rooms';
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createRoom(String token, RoomModel room) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final response = await _apiService.post(
        '/room/v1/rooms',
        data: room.toJson(),
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );

      final newRoom = RoomModel.fromJson(response.data);
      _rooms.add(newRoom);
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> joinRoom(String token, String roomId, String userId, String userName) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final response = await _apiService.get(
        '/room/v1/media/access-token?roomId=$roomId&userId=$userId&userName=$userName',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );

      _currentRoom = _rooms.firstWhere(
        (room) => room.roomId == roomId,
        orElse: () => RoomModel(
          roomId: roomId,
          roomName: 'Test Room',
          description: '',
          ownerId: userId,
          maxUsers: 200,
          currentUsers: 1,
        ),
      );

      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<void> leaveRoom() async {
    _currentRoom = null;
    notifyListeners();
  }

  Future<void> fetchRoomStats(String token, String roomId) async {
    try {
      final response = await _apiService.get(
        '/room/v1/media/rooms/$roomId/stats',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );
      // Update current room stats
      if (_currentRoom != null) {
        _currentRoom = _currentRoom!.copyWith(
          currentUsers: response.data['totalUsers'] ?? 0,
        );
        notifyListeners();
      }
    } catch (e) {
      _error = e.toString();
    }
  }
}
