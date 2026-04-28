import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/auth_provider.dart';
import '../../providers/room_provider.dart';
import '../rooms/rooms_list_screen.dart';
import '../room/room_detail_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    _loadRooms();
  }

  void _loadRooms() {
    final auth = context.read<AuthProvider>();
    final rooms = context.read<RoomProvider>();
    if (auth.token != null) {
      rooms.fetchRooms(auth.token!);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Rooms Manager'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadRooms,
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              context.read<AuthProvider>().logout();
            },
          ),
        ],
      ),
      body: Consumer<RoomProvider>(
        builder: (context, roomProvider, _) {
          if (roomProvider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (roomProvider.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 48, color: Colors.red),
                  const SizedBox(height: 16),
                  Text('Error: ${roomProvider.error}'),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: _loadRooms,
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }

          if (roomProvider.rooms.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.video_call, size: 64, color: Colors.grey),
                  const SizedBox(height: 16),
                  const Text('No rooms available'),
                  const SizedBox(height: 16),
                  ElevatedButton.icon(
                    onPressed: _loadRooms,
                    icon: const Icon(Icons.refresh),
                    label: const Text('Refresh'),
                  ),
                ],
              ),
            );
          }

          return RoomsListScreen(rooms: roomProvider.rooms);
        },
      ),
    );
  }
}
