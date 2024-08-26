import 'package:flutter/material.dart';
import 'package:internet_file/internet_file.dart';
import 'package:pdfx/pdfx.dart';

class ShowCertificatePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text('PDF 보기'),
      ),
      body: Center(
        child: const Text('PDF 내용이 여기에 표시됩니다.'),
      ),
    );
  }
}
