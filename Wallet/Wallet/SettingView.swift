//
//  SettingView.swift
//  Wallet
//
//  Created by Seah Kim on 10/19/24.
//

import SwiftUI

struct SettingView: View {
    @Environment(\.presentationMode) var presentationMode

    @AppStorage("userNickname") var nickname: String = ""
    @AppStorage("userWalletId") var walletId: String = ""
    @AppStorage("userEmail") var email: String = ""
    @AppStorage("userPdfUrls") var pdfUrls: String = ""
    @AppStorage("userUniversity") var univName: String = ""
    @AppStorage("userCertifiedDate") var certified_date: String = ""

    @State private var certificationList: [Certification] = []
    @State private var endDate: String = ""
    
    
    @AppStorage("userPassportNumber") var passportNumber: String = ""
    @AppStorage("userPassportendDate") var passportendDate: String = ""
    @AppStorage("userPassportIssue") var passportIssue: String = ""
    @AppStorage("userPassportExpiry") var passportExpiry: String = ""
    @AppStorage("userPassportDate") var passportDate: String = ""
//   @State private var passportNumber: String = ""
//    @State private var passportIssue: String = ""
//    @State private var passportExpiry: String = ""
//    @State private var passportDate: String = ""

    var body: some View {
        VStack {
            ZStack {
                Text("지갑 정보")
                    .font(.title)
                    .fontWeight(.light)
                HStack {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Image(systemName: "chevron.left")
                            .font(.title)
                            .foregroundColor(.gray)
                    }
                    Spacer()
                }
            }
            .padding(.top, 16)
            .padding(.horizontal)

            TabView {
                WalletCardView(
                    certificationName: "경북멋쟁이 인증서",
                    nickname: nickname,
                    passportNumber: passportNumber,
                    passportIssue: passportIssue,
                    passportExpiry: passportExpiry,
                    passportDate: passportDate,
                    univName: "",
                    email: "",
                    endDate: "",
                    isPassportCard: true
                )
                
                WalletCardView(
                    certificationName: "경북멋쟁이 인증서",
                    nickname: nickname,
                    passportNumber: "",
                    passportIssue:"",
                    passportExpiry: "",
                    passportDate: "",
                    univName: univName,
                    email: email,
                    endDate: endDate,
                    isPassportCard: false
                )
            }
            .frame(width: UIScreen.main.bounds.width * 0.95, height: (UIScreen.main.bounds.width * 0.95) * (3.0 / 2.0))
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .always))

            Button(action: {
                deleteAllCertificates()
            }) {
                Text("인증서 전체 삭제하기")
                    .foregroundColor(.white)
                    .frame(width: 200, height: 36)
                    .background(Color(red: 218/255, green: 33/255, blue: 39/255))
                    .cornerRadius(6)
                    .overlay(
                        RoundedRectangle(cornerRadius: 6)
                            .stroke(Color(red: 218/255, green: 33/255, blue: 39/255), lineWidth: 1)
                    )
            }
            .padding()
        }
        .padding()
        .frame(maxHeight: .infinity, alignment: .top)
        .onAppear {
            fetchCertificationNames()
            fetchCertificationContent()
        }
    }

    private func formatDate(_ dateString: String) -> String {
        let inputFormatter = DateFormatter()
        inputFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm"

        let outputFormatter = DateFormatter()
        outputFormatter.dateFormat = "yyyy.MM.dd. HH:mm"

        if let date = inputFormatter.date(from: dateString) {
            return outputFormatter.string(from: date)
        } else {
            return dateString
        }
    }

    private func fetchCertificationContent() {
        guard let data = pdfUrls.data(using: .utf8),
              let pdfDict = try? JSONDecoder().decode([String: String].self, from: data),
              let pdfUrl = pdfDict["재학증_\(walletId)"],
              let encodedPdfUrl = pdfUrl.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let encodeCertName = "재학증".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: "http://121.151.25.247:8080/api/certifications/get-content?pdfUrl=\(encodedPdfUrl)&certName=\(encodeCertName)&walletId=\(walletId)")
        else {
            print("Invalid URL or encoding failure")
            return
        }

        print("Requesting URL: \(url)")

        let task = URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                print("Error fetching certification content: \(error.localizedDescription)")
                return
            }

            guard let data = data else {
                print("No data returned from request")
                return
            }

            do {
                if let jsonArray = try JSONSerialization.jsonObject(with: data, options: []) as? [[String: String]] {
                    DispatchQueue.main.async {
                        for item in jsonArray {
                            self.email = item["email"] ?? ""
                            self.univName = item["univName"] ?? ""
                            if let certifiedDate = item["certified_date"] {
                                self.certified_date = certifiedDate
                                self.endDate = self.formatDate(certifiedDate)
                            }
                        }
                    }
                } else {
                    print("JSON data could not be parsed as expected")
                }
            } catch {
                print("Error parsing JSON: \(error)")
            }
        }
        task.resume()
    }

    private func fetchCertificationNames() {
        guard let url = URL(string: "http://121.151.25.247:8080/api/certifications/cert-names?walletId=\(walletId)") else {
            print("Invalid URL")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("Failed to fetch certification names: \(error.localizedDescription)")
                return
            }

            guard let data = data else {
                print("No data received.")
                return
            }

            do {
                let certNames = try JSONDecoder().decode([String].self, from: data)
                DispatchQueue.main.async {
                    self.certificationList = certNames.map { Certification(name: $0) }
                    print("Certification names fetched: \(certNames)")
                }
            } catch {
                print("Failed to decode certification names: \(error)")
            }
        }
        task.resume()
    }

    private func deleteAllCertificates() {
        guard let url = URL(string: "http://121.151.25.247:8080/api/certifications/delete-wallet-certificates?walletId=\(walletId)") else {
            print("Invalid URL for DELETE request")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("Error deleting all certificates: \(error.localizedDescription)")
                return
            }

            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
                print("All certificates deleted successfully from server.")
                DispatchQueue.main.async {
                    self.certificationList = []
                }
            } else {
                print("Failed to delete all certificates.")
            }
        }
        task.resume()
    }
}

struct WalletCardView: View {
    let certificationName: String
    let nickname: String
    let passportNumber: String
    let passportIssue: String
    let passportExpiry: String
    let passportDate: String
    let univName: String
    let email: String
    let endDate: String
    let isPassportCard: Bool

    var body: some View {
        ZStack(alignment: .leading) {
            VStack(alignment: .leading) {
                Text("경북멋쟁이 인증서")
                    .font(.custom("KNU TRUTH", size: 18))
                    .foregroundColor(.black)
                    .padding(.top, 24)
                    .padding(.leading, 24)
                Text("이름")
                    .font(.body)
                    .foregroundColor(.gray)
                    .padding(.top, 24)
                    .padding(.leading, 24)
                Text(nickname)
                    .font(.title3)
                    .foregroundColor(.black)
                    .padding(.leading, 24)
                
                if isPassportCard {
                    Text("여권번호")
                        .font(.body)
                        .foregroundColor(.gray)
                        .padding(.top, 16)
                        .padding(.leading, 24)
                    Text(passportNumber)
                        .font(.title3)
                        .foregroundColor(.black)
                        .padding(.leading, 24)
                    
                    Text("여권 발급일")
                        .font(.body)
                        .foregroundColor(.gray)
                        .padding(.top, 16)
                        .padding(.leading, 24)
                    Text(passportIssue)
                        .font(.title3)
                        .foregroundColor(.black)
                        .padding(.leading, 24)
                    
                    Text("여권 만료일")
                        .font(.body)
                        .foregroundColor(.gray)
                        .padding(.top, 16)
                        .padding(.leading, 24)
                    Text(passportExpiry)
                        .font(.title3)
                        .foregroundColor(.black)
                        .padding(.leading, 24)
                    
                    Text("인증 일시")
                        .font(.body)
                        .foregroundColor(.gray)
                        .padding(.top, 16)
                        .padding(.leading, 24)
                    Text(passportDate)
                        .font(.title3)
                        .foregroundColor(.black)
                        .padding(.leading, 24)
                    
                    Spacer()
                    Text("codef API의 인증서비스를 통해 발급된 카드입니다.")
                        .font(.caption)
                        .foregroundColor(.gray)
                        .padding(.bottom, 24)
                        .padding(.leading, 24)
                } else {
                    Text("대학교")
                        .font(.body)
                        .foregroundColor(.gray)
                        .padding(.top, 16)
                        .padding(.leading, 24)
                    Text(univName)
                        .font(.title3)
                        .foregroundColor(.black)
                        .padding(.leading, 24)
                    
                    Text("이메일")
                        .font(.body)
                        .foregroundColor(.gray)
                        .padding(.top, 16)
                        .padding(.leading, 24)
                    Text(email)
                        .font(.title3)
                        .foregroundColor(.black)
                        .padding(.leading, 24)
                    
                    Text("인증 일시")
                        .font(.body)
                        .foregroundColor(.gray)
                        .padding(.top, 16)
                        .padding(.leading, 24)
                    Text(endDate)
                        .font(.title3)
                        .foregroundColor(.black)
                        .padding(.leading, 24)
                    
                    Spacer()
                    Text("univcert의 재학인증서비스를 통해 발급된 카드입니다.")
                        .font(.caption)
                        .foregroundColor(.gray)
                        .padding(.bottom, 24)
                        .padding(.leading, 24)
                }
            }
            
            HStack {
                Spacer()
                Image(isPassportCard ? "images/korea" : "images/knu")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 220, height: 220)
                    .padding(.top, 36.0)
                    .opacity(0.3)
                Spacer()
            }
        }
        .background(Color.white)
        .cornerRadius(10)
        .shadow(color: Color.gray.opacity(0.3), radius: 10, x: 0, y: 0)
        .padding()
    }
}
