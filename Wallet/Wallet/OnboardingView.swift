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
import Foundation

struct OnboardingView: View {
    @Environment(\.openURL) var openURL
    
    @State private var isVisible = false
    @State private var accessToken: String?
    @State private var refreshToken: String?
    @State private var authCode: String?
    @State private var errorMessage: String?
    @State private var isLoggedIn = false
    
    var body: some View {
        Group {
            if isLoggedIn {
                LockByCodeView()
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
    
    func loginWithKakaoAccount() {
        UserApi.shared.loginWithKakaoAccount { (oauthToken, error) in
            if let error = error {
                print("로그인 실패: \(error.localizedDescription)")
            } else {
                print("loginWithKakaoAccount() success.")
                
                self.accessToken = oauthToken?.accessToken
                self.refreshToken = oauthToken?.refreshToken
                
                print("액세스 토큰: \(self.accessToken ?? "없음")")
                print("리프레시 토큰: \(self.refreshToken ?? "없음")")
                
                self.isLoggedIn = true
                
                if let accessToken = self.accessToken {
                    sendAccessTokenToBackend(accessToken: accessToken)
                }
//                fetchUserInfo()
            }
        }
    }
    
    func sendAccessTokenToBackend(accessToken: String) {
        guard let url = URL(string: "http://211.107.135.107:8080/api/kakao/login/access?accessToken=\(accessToken)") else {
            print("Invalid URL")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("HTTP request failed: \(error.localizedDescription)")
                return
            }
            
            if let response = response as? HTTPURLResponse {
                if response.statusCode == 200 {
                    print("Access token sent successfully.")
                } else {
                    print("Failed to send access token. Status code: \(response.statusCode)")
                }
            }
        }
        
        task.resume()
    }


    
//    func fetchUserInfo() {
//        UserApi.shared.me { user, error in
//            if let error = error {
//                print("사용자 정보 가져오기 실패: \(error.localizedDescription)")
//            } else if let user = user {
//                let nickname = user.kakaoAccount?.profile?.nickname ?? "닉네임 없음"
//                print("사용자 닉네임: \(nickname)")
//            }
//        }
//    }
}

#Preview {
    OnboardingView()
}

