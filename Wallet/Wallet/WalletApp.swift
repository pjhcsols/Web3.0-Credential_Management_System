//
//  WalletApp.swift
//  Wallet
//
//  Created by Seah Kim on 9/29/24.
//

import SwiftUI
import KakaoSDKCommon
import KakaoSDKAuth

@main
struct WalletApp: App {
    
    init() {
        KakaoSDK.initSDK(appKey: "39d3dd59edaf61b248a1aedf5fcc15e3")
    }
    
    var body: some Scene {
        WindowGroup {
            OnboardingView()
            .onOpenURL(perform: { url in
                OnboardingView().handleOpenURL(url)
            })
        }
    }
}


class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        print("Received URL kkkkk: \(url)")
        if (AuthApi.isKakaoTalkLoginUrl(url)) {
            return AuthController.handleOpenUrl(url: url)
        }
        return false
    }
}
