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
                Text("카카오 로그인")
                    .font(.headline)
                    .foregroundColor(.black)
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(Color(red: 254/255, green: 229/255, blue: 0/255))
                    .cornerRadius(10)
            }
            .padding([.leading, .bottom, .trailing], 40)
        }
    }
}

#Preview {
    OnboardingView()
}
