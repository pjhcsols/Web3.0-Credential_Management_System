import 'package:flutter/material.dart';

void main() {
  runApp(const MaterialApp(home: WalletCard()));
}

class WalletCard extends StatefulWidget {
  const WalletCard({Key? key}) : super(key: key);

  @override
  _WalletCardState createState() => _WalletCardState();
}

class _WalletCardState extends State<WalletCard> {
  int _selectedIndex = 0;
  bool _isCardUp = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: _selectedIndex == 0
          ? AnimatedSwitcher(
              duration: const Duration(milliseconds: 300),
              child: _isCardUp
                  ? const ExpandedCard()
                  : CardDisplay(
                      onTap: () {
                        setState(() {
                          _isCardUp = true;
                        });
                      },
                    ),
            )
          : const Placeholder(),
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
  final VoidCallback onTap;

  const CardDisplay({Key? key, required this.onTap}) : super(key: key);

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
        child: GestureDetector(
          onTap: onTap,
          child: Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20.0),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  offset: const Offset(0, 0),
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
                          fontWeight: FontWeight.bold,
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
      ),
    );
  }
}

class ExpandedCard extends StatelessWidget {
  const ExpandedCard({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 300),
      padding: const EdgeInsets.all(32.0),
      curve: Curves.easeInOut,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Card Information
          const SizedBox(height: 48.0),
          Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20.0),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  offset: const Offset(0, 0),
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
                          fontWeight: FontWeight.bold,
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
          // Usage History
          Expanded(
            child: ListView(
              children: const [
                ListTile(
                  title: Text('정부24 간편인증 (생체인증)'),
                  subtitle: Text('발급처: 정부24 사용일시: 2024.08.17 14:36'),
                ),
                ListTile(
                  title: Text('정부24 간편인증 (생체인증)'),
                  subtitle: Text('발급처: 정부24 사용일시: 2024.08.17 14:36'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
