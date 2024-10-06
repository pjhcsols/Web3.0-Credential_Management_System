//
//  OnboardingView.swift
//  Wallet
//
//  Created by Seah Kim on 9/29/24.
//

import SwiftUI

struct OnboardingView: View {
    @State private var isVisible = false
    
    var body: some View {
        VStack {
            Spacer()
            Text("간편한 공인인증서")
                .font(.custom("KNU TRUTH", size: 36))
                .opacity(isVisible ? 1 : 0)
                .offset(y: isVisible ? 0 : 20)
                .animation(.easeInOut(duration: 1).delay(0.3), value: isVisible)
                .onAppear {
                    isVisible = true
                }
            Spacer()
            Button(action: {
            }) {
                Image("images/kakao_login_medium_wide")
            }
            .padding(.bottom, 40)
        }
    }
}

#Preview {
    OnboardingView()
}
