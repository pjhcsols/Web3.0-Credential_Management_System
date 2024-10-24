//
//  LockByBioView.swift
//  Wallet
//
//  Created by Seah Kim on 10/8/24.
//

import SwiftUI
import LocalAuthentication

struct LockByBioView: View {
    @State private var isUnlocked = false
    @State private var authErrorMessage: String?
    
    var body: some View {
        VStack {
            Spacer()
                .frame(height: 256)
            Text("생체인증 등록")
                .font(.title3)
                .fontWeight(.semibold)
            Spacer()
                .frame(height: 32)
            HStack {
                Image("images/face-id")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 80)
                Spacer()
                    .frame(width: 32)
                Image("images/touch-id")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 80)
            }
            Spacer()
            HStack {
                NavigationLink(destination: CreateWalletView().navigationBarBackButtonHidden(true)) {
                    Text("건너뛰기")
                        .foregroundColor(.black)
                        .frame(width: 156, height: 45)
                        .background(Color.white)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color.gray, lineWidth: 1)
                        )
                }
                
                Button(action: { authenticate() }) {
                    Text("등록하기")
                        .foregroundColor(.white)
                        .frame(width: 156, height: 45)
                        .background(Color(red: 218/255, green: 33/255, blue: 39/255))
                        .cornerRadius(6)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color(red: 218/255, green: 33/255, blue: 39/255), lineWidth: 1)
                        )
                }
            }
            NavigationLink(destination: CreateWalletView().navigationBarBackButtonHidden(true), isActive: $isUnlocked) {
                EmptyView()
            }
            
            if let errorMessage = authErrorMessage {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .padding()
            }
            
        }
        .padding()

        Spacer()
    }
    
    private func authenticate() {
        let context = LAContext()
        var error: NSError?
        
        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            let reason = "페이스 아이디나 터치 아이디로 인증해 주세요."
            
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, authenticationError in
                DispatchQueue.main.async {
                    if success {
                        isUnlocked = true
                        authErrorMessage = nil
                    } else {
                        authErrorMessage = "인증에 실패했습니다. 다시 시도해주세요."
                    }
                }
            }
        } else {
            authErrorMessage = "페이스 아이디 또는 터치 아이디를 사용할 수 없습니다."
        }
    }
}

#Preview {
    LockByBioView()
}
