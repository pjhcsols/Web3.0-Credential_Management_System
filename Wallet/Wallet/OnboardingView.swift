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
    @State private var isLoggedIn = false
    
    struct User: Decodable {
        let id: Int
        let nickname: String
        let email: String?
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
                }
            }
        }
    }
    
    func loginWithKakaoAccount() {
        UserApi.shared.loginWithKakaoAccount { (oauthToken, error) in
            if let error = error {
                print("* * * * * * * * * * * * * * * * * *")
                print("OnboardingView.swift\n")
                print("로그인 실패: \(error.localizedDescription)")
                print("\nOnboardingView.swift")
                print("* * * * * * * * * * * * * * * * * *\n\n")
            } else {
                print("* * * * * * * * * * * * * * * * * *")
                print("OnboardingView.swift\n")
                print("로그인 성공")
                
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
        guard let url = URL(string: "http://121.151.45.73:8080/api/kakao/login/access?accessToken=\(accessToken)") else {
            print("Invalid URL")
            print("\nOnboardingView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("HTTP request failed: \(error.localizedDescription)")
                print("\nOnboardingView.swift")
                print("* * * * * * * * * * * * * * * * * *\n\n")
                return
            }
            
            if let data = data {
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("서버 응답 JSON(OnboardingView.swift): \(jsonString)")
                }
                
                do {
                    let userInfo = try JSONDecoder().decode(User.self, from: data)
                    DispatchQueue.main.async {
                        print("ID: \(userInfo.id)")
                        print("닉네임: \(userInfo.nickname)")
                        print("이메일: \(userInfo.email ?? "temp")")
                        print("액세스 토큰: \(userInfo.accessToken)")
                        print("JWT 토큰: \(userInfo.jwtToken)")
                        print("리프레시 토큰: \(userInfo.refreshToken)")
                        print("서버 유저 ID: \(userInfo.serverUserId)")
                        print("서버 유저 이메일: \(userInfo.serverUserEmail)")

                        
                        UserDefaults.standard.removeObject(forKey: "accessToken")
                        UserDefaults.standard.removeObject(forKey: "userWalletId")
                        UserDefaults.standard.removeObject(forKey: "userPassword")
                        UserDefaults.standard.removeObject(forKey: "userUniversity")
                        UserDefaults.standard.removeObject(forKey: "userUniversityCheck")
                        UserDefaults.standard.removeObject(forKey: "userEmail")
                        UserDefaults.standard.removeObject(forKey: "userUniversityCheck")
                        UserDefaults.standard.set(userInfo.nickname, forKey: "userNickname")
                        UserDefaults.standard.set(userInfo.jwtToken, forKey: "jwtToken")
                        UserDefaults.standard.set(userInfo.serverUserId, forKey: "userId")
                        
                        
                        for (key, value) in UserDefaults.standard.dictionaryRepresentation() {
                          print("\(key) = \(value) \n")
                        }
                        
                        print("\nOnboardingView.swift")
                        print("* * * * * * * * * * * * * * * * * *\n\n")
                    }
                } catch {
                    print("Failed to decode JSON: \(error.localizedDescription)")
                    print("\nOnboardingView.swift")
                    print("* * * * * * * * * * * * * * * * * *\n\n")
                }
            }
        }

        task.resume()
    }
    
    func clearAuthenticatedUserList() {
        guard let url = URL(string: "http://121.151.45.73:8080/api/univcert/clear-list") else {
            print("Invalid URL for clearing user list")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("Failed to clear user list: \(error.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
                print("User list cleared successfully")
            } else {
                print("Unexpected response from server")
            }
        }
        
        task.resume()
    }

}

#Preview {
    OnboardingView()
}

