import 'package:flutter/foundation.dart';
import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user_model.dart';
import '../services/api_service.dart';

class AuthProvider extends ChangeNotifier {
  final ApiService _apiService = ApiService();
  UserModel? _user;
  String? _token;
  bool _isLoading = false;
  String? _error;

  UserModel? get user => _user;
  String? get token => _token;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _token != null && _user != null;

  AuthProvider() {
    _loadStoredToken();
  }

  Future<void> _loadStoredToken() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('authToken');
    if (_token != null) {
      notifyListeners();
    }
  }

  Future<bool> login(String appId, String appSecret) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final response = await _apiService.post(
        '/auth/v1/login',
        data: {
          'appId': appId,
          'appSecret': appSecret,
        },
      );

      _token = response.data['appToken'];
      _user = UserModel(
        id: appId,
        email: appId,
        name: 'Test User',
      );

      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('authToken', _token!);

      _isLoading = false;
      notifyListeners();
      return true;
    } on DioException catch (e) {
      _error = e.response?.data['message'] ?? 'Login failed';
      _isLoading = false;
      notifyListeners();
      return false;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    _token = null;
    _user = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('authToken');
    notifyListeners();
  }

  Future<bool> verifyToken() async {
    if (_token == null) return false;

    try {
      await _apiService.post(
        '/auth/v1/verify-token',
        options: Options(headers: {'Authorization': 'Bearer $_token'}),
      );
      return true;
    } catch (e) {
      await logout();
      return false;
    }
  }
}
