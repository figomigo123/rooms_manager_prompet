import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../models/room_model.dart';
import '../../providers/room_provider.dart';
import '../../providers/auth_provider.dart';

class CreateRoomScreen extends StatefulWidget {
  const CreateRoomScreen({Key? key}) : super(key: key);

  @override
  State<CreateRoomScreen> createState() => _CreateRoomScreenState();
}

class _CreateRoomScreenState extends State<CreateRoomScreen> {
  final _roomNameController = TextEditingController();
  final _descriptionController = TextEditingController();
  int _maxUsers = 200;
  bool _audioEnabled = true;
  bool _videoEnabled = true;
  bool _screenshareEnabled = true;
  bool _whiteboardEnabled = false;
  bool _chatEnabled = true;

  @override
  void dispose() {
    _roomNameController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _createRoom() async {
    if (_roomNameController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter room name')),
      );
      return;
    }

    final auth = context.read<AuthProvider>();
    final rooms = context.read<RoomProvider>();

    if (auth.token != null) {
      final room = RoomModel(
        roomId: 'room_${DateTime.now().millisecondsSinceEpoch}',
        roomName: _roomNameController.text,
        description: _descriptionController.text,
        ownerId: auth.user!.id,
        maxUsers: _maxUsers,
        currentUsers: 1,
        settings: RoomSettings(
          audioEnabled: _audioEnabled,
          videoEnabled: _videoEnabled,
          screenshareEnabled: _screenshareEnabled,
          whiteboardEnabled: _whiteboardEnabled,
          chatEnabled: _chatEnabled,
        ),
      );

      final success = await rooms.createRoom(auth.token!, room);

      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Room created successfully!')),
        );
        Navigator.of(context).pop();
      } else if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: ${rooms.error}')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Create Room'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            TextField(
              controller: _roomNameController,
              decoration: InputDecoration(
                labelText: 'Room Name',
                hintText: 'Enter room name',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _descriptionController,
              decoration: InputDecoration(
                labelText: 'Description',
                hintText: 'Enter room description',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              maxLines: 3,
            ),
            const SizedBox(height: 16),
            const Text('Max Users'),
            Slider(
              value: _maxUsers.toDouble(),
              min: 10,
              max: 200,
              divisions: 19,
              label: '$_maxUsers',
              onChanged: (value) {
                setState(() => _maxUsers = value.toInt());
              },
            ),
            const SizedBox(height: 16),
            const Text(
              'Room Features',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            CheckboxListTile(
              title: const Text('Audio'),
              value: _audioEnabled,
              onChanged: (value) {
                setState(() => _audioEnabled = value ?? false);
              },
            ),
            CheckboxListTile(
              title: const Text('Video'),
              value: _videoEnabled,
              onChanged: (value) {
                setState(() => _videoEnabled = value ?? false);
              },
            ),
            CheckboxListTile(
              title: const Text('Screen Share'),
              value: _screenshareEnabled,
              onChanged: (value) {
                setState(() => _screenshareEnabled = value ?? false);
              },
            ),
            CheckboxListTile(
              title: const Text('Whiteboard'),
              value: _whiteboardEnabled,
              onChanged: (value) {
                setState(() => _whiteboardEnabled = value ?? false);
              },
            ),
            CheckboxListTile(
              title: const Text('Chat'),
              value: _chatEnabled,
              onChanged: (value) {
                setState(() => _chatEnabled = value ?? false);
              },
            ),
            const SizedBox(height: 32),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: _createRoom,
                style: ElevatedButton.styleFrom(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: const Text('Create Room'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
