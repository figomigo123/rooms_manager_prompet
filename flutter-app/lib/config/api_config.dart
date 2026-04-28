class ApiConfig {
  static const String baseUrl = 'http://localhost:8001';
  static const String authUrl = '$baseUrl/auth/v1';
  static const String roomUrl = 'http://localhost:8002/room/v1';
  static const String mediaUrl = 'http://localhost:8003';
  static const String mediaWsUrl = 'ws://localhost:8003';

  // Auth endpoints
  static const String loginEndpoint = '$authUrl/login';
  static const String registerEndpoint = '$authUrl/register';
  static const String verifyTokenEndpoint = '$authUrl/verify-token';

  // Room endpoints
  static const String roomsEndpoint = '$roomUrl/rooms';
  static const String mediaAccessTokenEndpoint = '$roomUrl/media/access-token';
  static const String mediaStatsEndpoint = '$roomUrl/media/rooms';

  // MediaSoup endpoints
  static const String mediasoupHealthEndpoint = '$mediaUrl/health';
}
