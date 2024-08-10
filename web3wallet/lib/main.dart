import 'package:flutter/material.dart';

void main() {
  runApp(const MaterialApp(home: WalletCard()));
}

class WalletCard extends StatefulWidget {
  const WalletCard({Key? key}) : super(key: key);

  @override
  _VerticalCardSliderState createState() => _VerticalCardSliderState();
}

class _VerticalCardSliderState extends State<WalletCard> {
  List<Map<String, String>> cards = [
    {"title": "경북대학교 학생증", "description": "테스트 입니다"},
    {"title": "국제학생증", "description": "테스트 입니다"},
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Web 3 Wallet"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: ListView.builder(
          itemCount: cards.length,
          itemBuilder: (context, index) {
            return Card(
              margin: const EdgeInsets.symmetric(vertical: 10.0),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16.0),
              ),
              child: Container(
                height: 240.0,
                padding: const EdgeInsets.all(24.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      cards[index]['title']!,
                      style: const TextStyle(
                        fontSize: 24.0,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 10.0),
                    Text(
                      cards[index]['description']!,
                      style: TextStyle(
                        fontSize: 16.0,
                        color: Colors.grey[700],
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}