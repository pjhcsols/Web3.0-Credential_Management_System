import 'package:flutter/material.dart';

class VerticalSlidePageRoute extends PageRouteBuilder {
  final Widget page;

  VerticalSlidePageRoute({required this.page})
      : super(
          pageBuilder: (context, animation, secondaryAnimation) => page,
          transitionsBuilder: (context, animation, secondaryAnimation, child) {
            // Define the start and end offsets for the vertical transition
            const begin = Offset(0.0, 1.0); // Start from bottom
            const end = Offset.zero; // End at the current position
            const curve = Curves.easeInOut;

            var tween = Tween(begin: begin, end: end);
            var offsetAnimation = animation.drive(tween.chain(CurveTween(curve: curve)));

            return SlideTransition(position: offsetAnimation, child: child);
          },
        );
}
