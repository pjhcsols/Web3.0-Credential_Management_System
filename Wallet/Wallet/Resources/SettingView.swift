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
    
    @State private var endDate: String = ""
    
    var body: some View {
        VStack{
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
            
            VStack (alignment: .leading) {
                HStack {
                    Text("경북멋쟁이 인증서")
                    .font(.custom("KNU TRUTH", size: 18))
                    .foregroundColor(.black)
                    .padding(.top, 24)
                    .padding(.leading, 24)
                    Spacer()
                }
                    Text("이름")
                    .font(.body)
                    .foregroundColor(.gray)
                    .padding(.top, 24)
                    .padding(.leading, 24)
                    Text(nickname)
                    .font(.title3)
                    .foregroundColor(.black)
                    .padding(.leading, 24)
                    
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
                    
                    Text("유효기간")
                    .font(.body)
                    .foregroundColor(.gray)
                    .padding(.top, 16)
                    .padding(.leading, 16)
                    Text("\(endDate) ~ ")
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
            .background(Color.white)
            .cornerRadius(10)
            .shadow(color: Color.gray.opacity(0.3), radius: 10, x: 0, y: 0)
            .frame(width: UIScreen.main.bounds.width * 0.85,
                   height: (UIScreen.main.bounds.width * 0.85) * (3.0 / 2.0),
                   alignment: .top)
            
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
        .onAppear(){
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
        guard let encodeCertName = ("재학증").addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)
        else {
            print("인코딩 실패")
            return
        }
        guard let data = pdfUrls.data(using: .utf8),
              let pdfDict = try? JSONDecoder().decode([String: String].self, from: data),
              let pdfUrl = pdfDict["재학증_\(walletId)"],
              let encodedPdfUrl = pdfUrl.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: "http://220.89.75.210:8080/api/certifications/get-content?pdfUrl=\(encodedPdfUrl)&certName=\(encodeCertName)&walletId=\(walletId)") else {
            print("Invalid URL for GET request or JSON decoding failed")
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
                            if let email = item["email"] {
                                self.email = email
                            }
                            if let univName = item["univName"] {
                                self.univName = univName
                            }
                            if let certifiedDate = item["certified_date"] {
                                self.certified_date = certifiedDate
                                let formattedDate:String = formatDate(certified_date)
                                self.endDate = formattedDate
                                print("Date certified_date: \(certified_date)")
                                print("Date formattedDate: \(formattedDate)")
                                print("Date endDate: \(endDate)")
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

    private func deleteAllCertificates() {
        guard let walletIdInt = Int(walletId) else {
            print("Invalid walletId")
            return
        }
        
        guard let url = URL(string: "http://220.89.75.210:8080/api/certifications/delete-wallet-certificates?walletId=\(walletIdInt)") else {
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
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    print("All certificates deleted successfully from server.")
                    DispatchQueue.main.async {
                        // 상태관리 필요한거 고민해야함
                    }
                } else {
                    print("Failed to delete all certificates with status code: \(httpResponse.statusCode)")
                }
            }
        }
        
        task.resume()
    }
}

#Preview {
    SettingView()
}
