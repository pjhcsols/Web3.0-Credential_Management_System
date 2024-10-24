//
//  CreateWalletView.swift
//  Wallet
//
//  Created by Seah Kim on 10/7/24.
//

import SwiftUI

struct CreateWalletView: View {
    @State private var isWalletCreated = false
    @State private var isWalletExists = false
    @State private var navigateToContentView = false
    
    private var jwtToken = UserDefaults.standard.string(forKey: "jwtToken")
    @State private var privateKey: String? = ""
    @State private var publicKey: String? = ""
    @State private var walletResponse: Wallet?
    @AppStorage("userVerified") var userVerify: Bool = false

    struct Wallet: Codable {
        let id: Int
        let user: User
        let pdfUrl: String?
        let privateKey: String?
        let publicKey: String?
    }
    
    struct User: Codable {
        let id: Int
        let email: String?
        let password: String
    }
    
    var body: some View {
        NavigationView {
            VStack {
                Spacer()
                Text("지갑 생성을 시작해볼까요?")
                    .font(.title2)
                    .fontWeight(.semibold)
                Spacer()
                
                Button(action: {
                    generateKeys()
                    getWallet()
                }) {
                    Text("다음")
                        .foregroundColor(.white)
                        .frame(width: 256, height: 45)
                        .background(Color(red: 218/255, green: 33/255, blue: 39/255))
                        .cornerRadius(6)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color(red: 218/255, green: 33/255, blue: 39/255), lineWidth: 1)
                        )
                }
                .padding()

                
                NavigationLink(destination: GetUniversityView().navigationBarBackButtonHidden(true), isActive: $isWalletExists) {
                    EmptyView()
                }
//                NavigationLink(destination: ContentView().navigationBarBackButtonHidden(true), isActive: $navigateToContentView) {
//                    EmptyView()
//                }
            }
            .onAppear {
                if userVerify {
                    navigateToContentView = true
                }
            }
        }
    }
    private func generateKeys() {
        let keys = generateKeyPair()
        self.privateKey = keys.privateKey
        self.publicKey = keys.publicKey
        print("* * * * * * * * * * * * * * * * * *")
        print("CreateWalletView.swift\n")
        
        if let privateKey = self.privateKey, let publicKey = self.publicKey {
            print("Private Key: \(privateKey)")
            print("Generated Public Key: \(publicKey)")
        } else {
            print("Failed to generate keys")
            print("\nCreateWalletView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")
        }
    }
    
    private func getWallet() {
        guard let jwtToken = jwtToken else {
            print("jwt 토큰이 없습니다.")
            print("\nCreateWalletView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")
            return
        }
        
        guard let url = URL(string: "http://121.151.45.73:8080/api/wallets/me") else {
            print("유효하지 않은 URL입니다.")
            print("C\nreateWalletView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("Bearer \(jwtToken)", forHTTPHeaderField: "Authorization")
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                print("\nCreateWalletView.swift")
                print("* * * * * * * * * * * * * * * * * *\n\n")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200, let data = data {
                    do {
                        let walletResponse = try JSONDecoder().decode(Wallet.self, from: data)
                        print("지갑 정보(get): \(walletResponse)")
                        
                        saveWalletInfo(wallet: walletResponse)
                        
                        DispatchQueue.main.async {
                            self.walletResponse = walletResponse
                            self.isWalletExists = true
                        }
                        print("\nCreateWalletView.swift")
                        print("* * * * * * * * * * * * * * * * * *\n\n")
                    } catch {
                        print("JSON 디코딩 실패(get): \(error.localizedDescription)")
                        print("\nCreateWalletView.swift")
                        print("* * * * * * * * * * * * * * * * * *\n\n")
                    }
                } else if httpResponse.statusCode == 404 {
                    DispatchQueue.main.async {
                        guard let privateKey = self.privateKey, let publicKey = self.publicKey else {
                                                print("Keys are not available.")
                                                return
                                            }
                        self.createWallet(privateKey: privateKey, publicKey: publicKey)
                    }
                } else {
                    print("서버 오류: 상태 코드 \(httpResponse.statusCode)")
                    print("\nCreateWalletView.swift")
                    print("* * * * * * * * * * * * * * * * * *\n\n")
                }
            }
        }
        
        task.resume()
    }
    
    private func createWallet(privateKey: String, publicKey: String) {
        guard let jwtToken = jwtToken else {
            print("jwt 토큰이 없습니다.")
            return
        }
        
        print("jwt토큰: \(jwtToken)")
        print("프라이빗키: \(privateKey)")
        print("퍼블릭키: \(publicKey)")
        
        guard let url = URL(string: "http://121.151.45.73:8080/api/wallets?privateKey=\(privateKey)&publicKey=\(publicKey)") else {
            print("유효하지 않은 URL입니다.")
            print("\nCreateWalletView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(jwtToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "loginUser": jwtToken,
            "privateKey": privateKey,
            "publicKey": publicKey
        ]
        
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: body, options: [])
            request.httpBody = jsonData
        } catch {
            print("JSON 직렬화 에러: \(error.localizedDescription)")
            print("\nCreateWalletView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")
            return
        }
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                print("\nCreateWalletView.swift")
                print("* * * * * * * * * * * * * * * * * *\n\n")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 201, let data = data {
                    do {
                        let walletResponse = try JSONDecoder().decode(Wallet.self, from: data)
                        print("지갑 생성 성공! PDF URL: \(walletResponse.pdfUrl ?? "없음")")
                        
                        saveWalletInfo(wallet: walletResponse)
                        
                        DispatchQueue.main.async {
                            self.walletResponse = walletResponse
                            self.isWalletExists = true
                        }
                        print("\nCreateWalletView.swift")
                        print("* * * * * * * * * * * * * * * * * *\n\n")
                    } catch {
                        print("JSON 디코딩 실패(post): \(error.localizedDescription)")
                        print("\nCreateWalletView.swift")
                        print("* * * * * * * * * * * * * * * * * *\n\n")
                    }
                } else {
                    print("지갑 생성 실패: 상태 코드 \(httpResponse.statusCode)")
                    print("\nCreateWalletView.swift")
                    print("* * * * * * * * * * * * * * * * * *\n\n")
                    if let data = data, let errorMessage = String(data: data, encoding: .utf8) {
                        print("서버 오류 메시지: \(errorMessage)")
                        print("\nCreateWalletView.swift")
                        print("* * * * * * * * * * * * * * * * * *\n\n")
                    }
                }
            }
        }
        
        task.resume()
    }
    
    private func saveWalletInfo(wallet: Wallet) {
        UserDefaults.standard.set(wallet.id, forKey: "userWalletId")
        UserDefaults.standard.set(wallet.pdfUrl, forKey: "userPdfUrl")
    }
}

#Preview {
    CreateWalletView()
}
