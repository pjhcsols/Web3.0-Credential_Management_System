//
//  WalletApp.swift
//  Wallet
//
//  Created by Seah Kim on 9/29/24.
//

import SwiftUI
import KakaoSDKCommon
import KakaoSDKAuth
import Foundation

@main
struct WalletApp: App {
//    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    init() {
        KakaoSDK.initSDK(appKey: "d04faa6dc8e7a737b9254f4c3dc47d9a")
    }
    
    var body: some Scene {
        WindowGroup {
            OnboardingView()
            .preferredColorScheme(.light)
        }
    }
}


class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        if (AuthApi.isKakaoTalkLoginUrl(url)) {
            return AuthController.handleOpenUrl(url: url)
        }

        return false
    }
}
