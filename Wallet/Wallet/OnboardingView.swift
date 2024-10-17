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
    @State private var nickname: String = ""
    
    struct UserInfo: Decodable {
        let id: Int
        let nickname: String
        let email: String
        let accessToken: String
        let jwtToken: String
        let refreshToken: String
        let serverUserId: Int
        let serverUserEmail: String
    }
    
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
            
            if let data = data {
                do {
                    let userInfo = try JSONDecoder().decode(UserInfo.self, from: data)
                    DispatchQueue.main.async {
                        
                        /*
                        print("ID: \(userInfo.id)")
                        print("닉네임: \(userInfo.nickname)")
                        print("이메일: \(userInfo.email)")
                        print("액세스 토큰: \(userInfo.accessToken)")
                        print("JWT 토큰: \(userInfo.jwtToken)")
                        print("리프레시 토큰: \(userInfo.refreshToken)")
                        print("서버 유저 ID: \(userInfo.serverUserId)")
                        print("서버 유저 이메일: \(userInfo.serverUserEmail)")
                        */
                        
                        UserDefaults.standard.set(userInfo.nickname, forKey: "userNickname")
                        UserDefaults.standard.set(userInfo.jwtToken, forKey: "jwtToken")
                    }
                } catch {
                    print("Failed to decode JSON: \(error.localizedDescription)")
                }
            }
        }
        
        task.resume()
    }
}

#Preview {
    OnboardingView()
}

