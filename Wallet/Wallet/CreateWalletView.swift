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
    
    let privateKey = "test1"
    let publicKey = "test2"
    
    struct WalletResponse: Codable {
        let id: Int
        let user: User
        let pdfUrl: String
        let privateKey: String
        let publicKey: String
    }

    struct User: Codable {
        let id: Int
        let email: String
        let password: String
    }
    
    var body: some View {
        VStack {
            Spacer()
            Text("지갑 생성을 시작해볼까요?")
                .font(.title2)
                .fontWeight(.semibold)
            Spacer()
            
//            NavigationLink(destination: ContentView().navigationBarBackButtonHidden(true)) {
            Button(action: {
                createWallet(privateKey: privateKey, publicKey: publicKey)
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
        
        }
        .padding()
        Spacer()
    }
    
    
    private func createWallet(privateKey: String, publicKey: String) {
        guard let jwtToken = jwtToken else {
            print("jwt 토큰이 없습니다.")
            return
        }
        //        let (privateKey, publicKey) = generateKeyPair()
        //
        //        guard let privateKey = privateKey, let publicKey = publicKey else {
        //            print("키 생성에 실패했습니다.")
        //            return
        //        }
                
        //        let privateKey = "test1"
        //        let publicKey = "test2"
                
        print("jwt토큰: \(jwtToken)")
        print("프라이빗키: \(privateKey)")
        print("퍼블릭키: \(publicKey)")

        guard let url = URL(string: "http://211.107.135.107:8080/api/wallets?privateKey=\(privateKey)&publicKey=\(publicKey)") else {
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
                if httpResponse.statusCode == 201 {
                    guard let data = data else {
                        print("응답 데이터가 없습니다.")
                        return
                    }
                    
                    do {
                        let walletResponse = try JSONDecoder().decode(WalletResponse.self, from: data)
                        print("ID: \(walletResponse.id)")
                        print("User Email: \(walletResponse.user.email)")
                        print("PDF URL: \(walletResponse.pdfUrl)")
                        print("Private Key: \(walletResponse.privateKey)")
                        print("Public Key: \(walletResponse.publicKey)")
                        DispatchQueue.main.async {
                            self.walletResponse = walletResponse
                        }
                        print("지갑 생성 성공! PDF URL: \(walletResponse.pdfUrl)")
                    } catch {
                        print("JSON 디코딩 실패: \(error.localizedDescription)")
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

}

#Preview {
    CreateWalletView()
}
