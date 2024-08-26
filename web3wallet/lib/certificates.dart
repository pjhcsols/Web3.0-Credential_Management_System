import 'package:flutter/material.dart';
import 'package:local_auth/local_auth.dart';
import 'showCertificate.dart';

class CertificatePage extends StatelessWidget {
  const CertificatePage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        title: const Text('전자증명서'),
        leading: IconButton(
          icon: const Icon(Icons.close, color: Colors.black),
          onPressed: () {
            Navigator.pop(context);
          },
        ),
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: ListView(
          padding: const EdgeInsets.all(0.0),
          children: [
            _buildListTile(context, '주민등록등본', '행정안정부'),
            _buildListTile(context, '학교생활기록부', '정부24'),
            _buildListTile(context, '국제학생증', 'isic'),
          ],
        ),
      ),
    );
  }

  Widget _buildListTile(BuildContext context, String title, String issuer) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16.0),
      padding: const EdgeInsets.all(12.0),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(10.0),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            offset: const Offset(0, 0),
            blurRadius: 10,
            spreadRadius: 1,
          ),
        ],
        border: Border.all(color: Colors.grey.withOpacity(0.1), width: 1.0),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: const TextStyle(
                  fontSize: 16.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                issuer,
                style: const TextStyle(
                  fontSize: 14.0,
                  color: Colors.grey,
                ),
              ),
            ],
          ),
          IconButton(
            icon: const Icon(Icons.add_circle_outline_rounded, color: Color(0xFFE60000)),
            onPressed: () {
              addDialog(context, title);
            },
          ),
        ],
      ),
    );
  }

  void addDialog(BuildContext context, String title) {
    bool isCheckedAll = false;
    bool isCheckedIndividual = false;

    showModalBottomSheet(
      context: context,
      builder: (BuildContext context) {
        return StatefulBuilder(
          builder: (BuildContext context, StateSetter setState) {
            return Container(
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.only(
                  topLeft: Radius.circular(30),
                  topRight: Radius.circular(30),
                ),
              ),
              padding: const EdgeInsets.all(20.0),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 18.0,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 20),
                  Row(
                    children: [
                      Checkbox(
                        value: isCheckedAll,
                        activeColor: const Color(0xFFE60000),
                        onChanged: (bool? value) {
                          setState(() {
                            isCheckedAll = value ?? false;
                            isCheckedIndividual = value ?? false;
                          });
                        },
                      ),
                      const Text(
                        '전체 동의하기',
                        style: TextStyle(
                          fontSize: 16.0,
                        ),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      Checkbox(
                        value: isCheckedIndividual,
                        activeColor: const Color(0xFFE60000),
                        onChanged: (bool? value) {
                          setState(() {
                            isCheckedIndividual = value ?? false;
                            if (!value!) {
                              isCheckedAll = false;
                            }
                          });
                        },
                      ),
                      const Text(
                        '[필수] 경북멋쟁이 제 3자 제공 동의',
                        style: TextStyle(
                          fontSize: 14.0,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  ElevatedButton(
                    onPressed: isCheckedIndividual
                        ? () async {
                            final isAuthenticated = await authenticateUser(context);
                            if (isAuthenticated) {
                              Navigator.push(
                                context,
                                MaterialPageRoute(builder: (context) => ShowCertificatePage()),
                              );
                            }
                          }
                        : null,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFE60000),
                      minimumSize: const Size(double.infinity, 45),
                      elevation: 0,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(4.0),
                      ),
                    ),
                    child: const Text(
                      '인증 후 신청하기',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 16.0,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Future<bool> authenticateUser(BuildContext context) async {
    final LocalAuthentication auth = LocalAuthentication();
    bool isAuthenticated = false;

    try {
      isAuthenticated = await auth.authenticate(
        localizedReason: '페이스 아이디로 인증해주세요.',
        options: const AuthenticationOptions(
          stickyAuth: true,
          biometricOnly: true,
        ),
      );
    } catch (e) {
      print(e);
    }

    // 페이스 아이디 인증 실패 시 비밀번호 입력 요청
    if (!isAuthenticated) {
      isAuthenticated = await _showPasswordDialog(context);
    }

    return isAuthenticated;
  }

  Future<bool> _showPasswordDialog(BuildContext context) async {
    String inputPin = '';
    return await showDialog<bool>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('비밀번호 입력'),
          content: TextField(
            keyboardType: TextInputType.number,
            maxLength: 6,
            obscureText: true,
            decoration: const InputDecoration(
              hintText: '6자리 비밀번호를 입력하세요.',
            ),
            onChanged: (value) {
              inputPin = value;
            },
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop(false); // 인증 실패
              },
              child: const Text('취소'),
            ),
            TextButton(
              onPressed: () {
                if (inputPin == '123456') { //사용자의 PIN과 비교 ㄱㄱ
                  Navigator.of(context).pop(true); // 인증 성공
                } else {
                  Navigator.of(context).pop(false); // 인증 실패
                }
              },
              child: const Text('확인'),
            ),
          ],
        );
      },
    ) ?? false;
  }
}
