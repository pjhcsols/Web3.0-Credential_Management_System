//
//  WalletViewModel.swift
//  Wallet
//
//  Created by Seah Kim on 10/19/24.
//

import Foundation

class WalletViewModel: ObservableObject {
    @Published var isWalletExists = false
    @Published var walletResponse: Wallet?
    
    private var jwtToken = UserDefaults.standard.string(forKey: "jwtToken")

    struct Wallet: Codable {
        let id: Int
        let user: User
        let pdfUrls: [String: String]?
        let privateKey: String?
        let publicKey: String?
    }
    
    struct User: Codable {
        let id: Int
        let email: String?
        let password: String
    }

    func getWallet() {
        guard let jwtToken = jwtToken else {
            print("jwt 토큰이 없습니다.")
            return
        }

        guard let url = URL(string: "http://121.151.25.247:8080/api/wallets/me") else {
            print("유효하지 않은 URL입니다.")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("Bearer \(jwtToken)", forHTTPHeaderField: "Authorization")

        let task = URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                return
            }

            guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200, let data = data else {
                print("서버 오류 또는 잘못된 응답 형식")
                return
            }
            
            do {
                let walletResponse = try JSONDecoder().decode(Wallet.self, from: data)
                
                if let pdfUrls = walletResponse.pdfUrls,
                   let pdfUrlsData = try? JSONEncoder().encode(pdfUrls),
                   let pdfUrlsString = String(data: pdfUrlsData, encoding: .utf8) {
                    UserDefaults.standard.set(pdfUrlsString, forKey: "userPdfUrls")
                    
                    print("PDF URLs 저장 성공(WalletViewModel): \(pdfUrlsString)")
                }
                
                UserDefaults.standard.set(walletResponse.id, forKey: "userWalletId")
                
                DispatchQueue.main.async {
                    self?.walletResponse = walletResponse
                    self?.isWalletExists = true
                }
                
                print("지갑 정보(get): \(walletResponse)")
                
            } catch {
                print("JSON 디코딩 실패(get): \(error.localizedDescription)")
            }
        }
        task.resume()
    }
    
    func loadCertifications() -> [Certification] {
        guard let pdfUrls = walletResponse?.pdfUrls else {
            print("지갑에 저장된 인증서가 없습니다.")
            return []
        }
        
        // pdfUrls의 키를 Certification 이름으로 사용하여 Certification 리스트 생성
        let certifications = pdfUrls.keys.map { Certification(name: $0) }
        print("Loaded certifications from wallet: \(certifications)")
        return certifications
    }
}
