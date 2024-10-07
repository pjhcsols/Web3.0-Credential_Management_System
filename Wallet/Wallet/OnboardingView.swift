//
//  OnboardingView.swift
//  Wallet
//
//  Created by Seah Kim on 9/29/24.
//

import SwiftUI
import KakaoSDKAuth
import KakaoSDKUser
import KakaoSDKCommon

struct OnboardingView: View {
    @State private var isVisible = false
    @State private var accessToken: String?
    @State private var errorMessage: String?
    @State private var isLoggedIn = false

    var body: some View {
        Group {
            if isLoggedIn {
                ContentView()
            } else {
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
                        loginWithKakaoAccount()
                    }) {
                        Image("images/kakao_login_medium_wide")
                            .resizable()
                            .scaledToFit()
                            .frame(height: 50)
                    }
                    .padding(.bottom, 40)
                    if let errorMessage = errorMessage {
                        Text(errorMessage)
                            .foregroundColor(.red)
                            .padding()
                    }
                }
            }
        }
    }
    
    private func loginWithKakaoAccount() {
        UserApi.shared.loginWithKakaoAccount { (oauthToken, error) in
            if let error = error {
                self.errorMessage = "로그인 실패: \(error.localizedDescription)"
                print("로그인 실패: \(error.localizedDescription)")
            } else {
                print("loginWithKakaoAccount() success.")
                self.accessToken = oauthToken?.accessToken
                print("액세스 토큰: \(self.accessToken ?? "")")
                self.isLoggedIn = true
                fetchUserInfo()
            }
        }
    }
    
    private func fetchUserInfo() {
        UserApi.shared.me { user, error in
            if let error = error {
                self.errorMessage = "사용자 정보 가져오기 실패: \(error.localizedDescription)"
                print("사용자 정보 가져오기 실패: \(error.localizedDescription)")
            } else if let user = user {
                print("사용자 정보: \(user.kakaoAccount?.email ?? "")")
            }
        }
    }
}

#Preview {
    OnboardingView()
}
