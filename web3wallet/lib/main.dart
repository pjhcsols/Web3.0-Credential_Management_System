import 'package:kakao_flutter_sdk_common/kakao_flutter_sdk_common.dart';
import 'package:flutter/material.dart';
import 'package:web3wallet/certificates.dart';
import 'history.dart';  // history.dart 파일을 import

void main() {
  KakaoSdk.init(nativeAppKey: 'ff0fcfcf568aacf8b5edf8bfb8a235d9');
  runApp(const MaterialApp(home: WalletCard()));
}

class WalletCard extends StatefulWidget {
  const WalletCard({Key? key}) : super(key: key);

  @override
  _WalletCardState createState() => _WalletCardState();
}

class _WalletCardState extends State<WalletCard> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: _selectedIndex == 0
          ? CardDisplay(
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const HistoryPage()),
                );
              },
            )
          : const Placeholder(),
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
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.add), 
            onPressed: (){
              Navigator.push(context, MaterialPageRoute(builder: (context) => const CertificatePage())
              );
            }),
          IconButton(icon: const Icon(Icons.settings), onPressed: null),
        ],
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
                    const Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            "경북멋쟁이 인증서",
                            style: TextStyle(
                              fontSize: 20.0,
                              fontFamily: 'KNUTRUTH',
                            ),
                          ),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text(
                              "유효기간",
                              style: TextStyle(
                                fontSize: 10.0,
                                fontWeight: FontWeight.bold
                              ),
                             ),
                             Text(
                              "2025.08.21",
                              style: TextStyle(
                                fontSize: 10.0,
                              ),
                             ),
                            ],
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 24.0),
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
