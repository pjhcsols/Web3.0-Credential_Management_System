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
                        KakaoLogin()
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
                .onOpenURL { url in
                    handleOpenURL(url)
                }
            }
        }
    }
    
    private func handleOpenURL(_ url: URL) {
        if url.scheme == "kakao39d3dd59edaf61b248a1aedf5fcc15e3" {
            let queryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems
            if let codeItem = queryItems?.first(where: { $0.name == "code" }) {
                authCode = codeItem.value
                print("인증 코드: \(authCode ?? "")")
                AuthApi.shared.token(code: authCode ?? "") { oauthToken, error in
                    if let error = error {
                        print("토큰 요청 실패: \(error.localizedDescription)")
                        self.errorMessage = "토큰 요청 실패: \(error.localizedDescription)"
                    } else if let oauthToken = oauthToken {
                        print("Access Token: \(oauthToken.accessToken)")
                        print("Refresh Token: \(oauthToken.refreshToken)")
                        self.accessToken = oauthToken.accessToken
                        self.refreshToken = oauthToken.refreshToken
                        self.isLoggedIn = true
                    }
                }
            } else {
                print("인증 코드가 없습니다.")
            }
        }
    }

    
    func KakaoLogin() {
        if (UserApi.isKakaoTalkLoginAvailable()) {
            kakaoLonginWithApp()
        } else {
            kakaoLoginWithAccount()
        }
    }
    
    private func kakaoLonginWithApp() {
        UserApi.shared.loginWithKakaoTalk {(oauthToken, error) in
            if let error = error {
                print(error)
            }
            else {
                print("loginWithKakaoTalk() success.")
                self.kakaoGetUserInfo()
            }
        }
    }
    
    private func kakaoLoginWithAccount() {
        UserApi.shared.loginWithKakaoAccount {(oauthToken, error) in
            if let error = error {
                print(error)
            } else if let oauthToken = oauthToken {
                print("Access Token: \(oauthToken.accessToken)")
                print("Refresh Token: \(oauthToken.refreshToken)")
                accessToken = oauthToken.accessToken
                refreshToken = oauthToken.refreshToken
                isLoggedIn = true
            }
        }
    }
    
    private func kakaoGetUserInfo() {
        UserApi.shared.me { user, error in
            if let error = error {
                print("사용자 정보 가져오기 실패: \(error.localizedDescription)")
            } else if let user = user {
                let nickname = user.kakaoAccount?.profile?.nickname ?? "닉네임 없음"
                print("사용자 닉네임: \(nickname)")
            }
        }
    }
    
    private func kakaoLogout() {
        UserApi.shared.logout {(error) in
            if let error = error {
                print(error)
            }
            else {
                print("logout() success.")
            }
        }
    }
    
    private func kakaoUnlink() {
        UserApi.shared.unlink {(error) in
            if let error = error {
                print(error)
            }
            else {
                print("unlink() success.")
            }
        }
    }
}

#Preview {
    OnboardingView()
}
