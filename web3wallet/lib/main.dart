import 'package:flutter/material.dart';

void main() {
  runApp(const MaterialApp(home: WalletCard()));
}

class WalletCard extends StatefulWidget {
  const WalletCard({Key? key}) : super(key: key);

  @override
  _MainPageState createState() => _MainPageState();
}

class _MainPageState extends State<WalletCard> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _selectedIndex == 0 ? const CardDisplay() : const Placeholder(),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.home),
            label: 'Home',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.add),
            label: 'Add',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.settings),
            label: 'Settings',
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: const Color(0xFFE60000),
        onTap: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
      ),
    );
  }
}

class CardDisplay extends StatelessWidget {
  const CardDisplay({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        title: const Text("Web 3 Wallet"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(20.0),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.1),
                offset: Offset(0, 0),
                blurRadius: 10,
                spreadRadius: 1,
              ),
            ],
          ),
          child: AspectRatio(
            aspectRatio: 2 / 3,
            child: Container(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 24.0),
                  const Center(
                    child: Text(
                      "경북멋쟁이 인증서",
                      style: TextStyle(
                        fontSize: 16.0,
                        fontFamily: 'KNUTRUTH',
                      ),
                    ),
                  ),
                  const SizedBox(height: 10.0),
                  const Center(
                    child: Text(
                      "홍길동",
                      style: TextStyle(
                        fontSize: 24.0,
                      ),
                    ),
                  ),
                  Expanded(
                    child: Center(
                      child: Padding(
                      padding: const EdgeInsets.all(32.0),
                      child: Image.asset(
                        'assets/images/knu_emblem.jpg',
                        fit: BoxFit.contain,
                      ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
