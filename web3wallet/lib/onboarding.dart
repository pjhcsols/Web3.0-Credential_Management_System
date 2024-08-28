import 'package:kakao_flutter_sdk_common/kakao_flutter_sdk_common.dart';
import 'package:kakao_flutter_sdk_user/kakao_flutter_sdk_user.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

void main() {
  KakaoSdk.init(nativeAppKey: 'ff0fcfcf568aacf8b5edf8bfb8a235d9');
  runApp(const MaterialApp(home: OnboardingPage()));
}

class OnboardingPage extends StatefulWidget {
  const OnboardingPage({Key? key}) : super(key: key);

  @override
  _OnboardingPageState createState() => _OnboardingPageState();
}

class _OnboardingPageState extends State<OnboardingPage> {
  bool _isTextVisible = false;

  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(milliseconds: 100), () {
      setState(() {
        _isTextVisible = true;
      });
    });
  }

  Future<void> _loginWithKakao() async {
    try {
      final result = await UserApi.instance.loginWithKakaoTalk();

      print(result);

      final url = 'https://your_redirect_url';
      if (await canLaunch(url)) {
        await launch(url);
      } else {
        throw 'Could not launch $url';
      }
    } catch (e) {
      print('Kakao login error: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Spacer(),
            AnimatedOpacity(
              opacity: _isTextVisible ? 1.0 : 0.0,
              duration: const Duration(seconds: 1),
              child: AnimatedContainer(
                duration: const Duration(seconds: 1),
                transform: Matrix4.translationValues(
                  0.0,
                  _isTextVisible ? 0.0 : 30.0,  // 위로 이동
                  0.0,
                ),
                child: const Center(
                  child: Text(
                    '간편한 공인인증서',
                    style: TextStyle(
                      fontSize: 32.0,
                      fontWeight: FontWeight.w600,
                      fontFamily: "KNUTRUTH",
                    ),
                  ),
                ),
              ),
            ),
            const Spacer(flex: 1),


            Center(
              child: ElevatedButton(
                onPressed: () {
                  // 시작하기 버튼 -> 회원가입 페이지
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFFE60000),
                  minimumSize: const Size(300, 45),
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(4.0),
                  ),
                ),
                child: const Text(
                  '시작하기',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 16.0,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
            const SizedBox(height: 10.0),

            InkWell(
              onTap: _loginWithKakao,
              child: Center(
                child: Image.asset(
                  'assets/images/kakao_login_medium_wide.png',
                ),
              ),
            ),
            const SizedBox(height: 10.0),

            // GestureDetector(
            //   onTap: () {
            //     // 로그인 페이지로 이동
            //   },
            //   child: const Center(
            //     child: Text(
            //       '로그인하기',
            //       style: TextStyle(
            //         fontSize: 14.0,
            //         color: Colors.grey,
            //         decoration: TextDecoration.underline,
            //       ),
            //     ),
            //   ),
            // ),
            const SizedBox(height: 100.0),
          ],
        ),
      ),
    );
  }
}
