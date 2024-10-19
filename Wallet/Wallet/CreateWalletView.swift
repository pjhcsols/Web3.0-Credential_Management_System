//
//  CreateWalletView.swift
//  Wallet
//
//  Created by Seah Kim on 10/7/24.
//

import SwiftUI

struct CreateWalletView: View {
    @State private var jwtToken: String? = UserDefaults.standard.string(forKey: "jwtToken")
    @State private var walletResponse: WalletResponse?
    @State private var isWalletCreated = false
    @State private var isWalletExists = false
    
    let privateKey = "test1"
    let publicKey = "test2"
    
    struct WalletResponse: Codable {
        let id: Int
        let user: User
        let pdfUrl: String?
        let privateKey: String?
        let publicKey: String?
        let address: String?
    }
    
    struct User: Codable {
        let id: Int
        let email: String
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
                
                // 지갑 O
                NavigationLink(destination: ContentView().navigationBarBackButtonHidden(true), isActive: $isWalletExists) {
                    EmptyView()
                }
            }
        }
    }
    
    private func getWallet() {
        guard let jwtToken = jwtToken else {
            print("jwt 토큰이 없습니다.")
            return
        }
        
        guard let url = URL(string: "http://192.168.1.99:8080/api/wallets/me") else {
            print("유효하지 않은 URL입니다.")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("Bearer \(jwtToken)", forHTTPHeaderField: "Authorization")
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200, let data = data {
                    do {
                        let walletResponse = try JSONDecoder().decode(WalletResponse.self, from: data)
                        print("지갑 정보(get): \(walletResponse)")
                        
                        saveWalletInfo(wallet: walletResponse)
                        
                        DispatchQueue.main.async {
                            self.walletResponse = walletResponse
                            self.isWalletExists = true
                        }
                    } catch {
                        print("JSON 디코딩 실패(get): \(error.localizedDescription)")
                    }
                } else if httpResponse.statusCode == 404 {
                    DispatchQueue.main.async {
                        self.createWallet(privateKey: privateKey, publicKey: publicKey)
                    }
                } else {
                    print("서버 오류: 상태 코드 \(httpResponse.statusCode)")
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
        
        guard let url = URL(string: "http://192.168.1.99:8080/api/wallets?privateKey=\(privateKey)&publicKey=\(publicKey)") else {
            print("유효하지 않은 URL입니다.")
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
            return
        }
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 201, let data = data {
                    do {
                        let walletResponse = try JSONDecoder().decode(WalletResponse.self, from: data)
                        print("지갑 생성 성공! PDF URL: \(walletResponse.pdfUrl ?? "없음")")
                        
                        saveWalletInfo(wallet: walletResponse)
                        
                        DispatchQueue.main.async {
                            self.walletResponse = walletResponse
                            self.isWalletExists = true
                        }
                    } catch {
                        print("JSON 디코딩 실패(post): \(error.localizedDescription)")
                    }
                } else {
                    print("지갑 생성 실패: 상태 코드 \(httpResponse.statusCode)")
                    if let data = data, let errorMessage = String(data: data, encoding: .utf8) {
                        print("서버 오류 메시지: \(errorMessage)")
                    }
                }
            }
        }
        
        task.resume()
    }
    
    private func saveWalletInfo(wallet: WalletResponse) {
        UserDefaults.standard.set(wallet.id, forKey: "walletId")
        UserDefaults.standard.set(wallet.pdfUrl, forKey: "pdfUrl")
        UserDefaults.standard.set(wallet.privateKey, forKey: "privateKey")
        UserDefaults.standard.set(wallet.publicKey, forKey: "publicKey")
        UserDefaults.standard.set(wallet.address, forKey: "walletAddress")
        
        UserDefaults.standard.set(wallet.user.id, forKey: "userId")
        UserDefaults.standard.set(wallet.user.email, forKey: "userEmail")
        UserDefaults.standard.set(wallet.user.password, forKey: "userPassword")
    }
}

#Preview {
    CreateWalletView()
}
