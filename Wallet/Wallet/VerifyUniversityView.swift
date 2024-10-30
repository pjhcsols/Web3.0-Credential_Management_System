//
//  VerifyUniversityView.swift
//  Wallet
//
//  Created by Seah Kim on 10/20/24.
//

import SwiftUI

struct VerifyUniversityView: View {
    @ObservedObject private var walletViewModel = WalletViewModel()
    
    @AppStorage("userUniversity") var univName: String = ""
    @AppStorage("userEmail") private var email: String = ""
    @AppStorage("userVerified") var userVerify: Bool = false
    @AppStorage("userWalletId") var walletId: String = ""
    @AppStorage("checkUniversity") var univCheck: Bool = false
    @AppStorage("userPdfUrls") var pdfUrls: String = ""
    

    
    @State private var selectedPdfData: Data? = nil
    @State private var showDocumentPicker = false
    @State private var codeInput: String = ""
    @State private var isCodeSent: Bool = false
    @State private var navigateToContentView: Bool = false
    @State private var isNavigationAllowed: Bool = false
    
    private var userName = UserDefaults.standard.string(forKey: "userNickname")
    
    var body: some View {
        NavigationStack {
            VStack(alignment: .leading) {
                Spacer()
                Text("\(userName ?? "사용자")님의")
                    .font(.title2)
                    .fontWeight(.semibold)
                Text("이메일을 입력해주세요")
                    .font(.title2)
                    .fontWeight(.semibold)
                TextField("이메일주소", text: $email)
                    .padding()
                    .background(Color(UIColor.systemGray6))
                    .cornerRadius(6)
                    .padding(.bottom, 10)
                    .frame(width: 300)
                Button(action: {
                    sendCode()
                }) {
                    Text("인증번호 요청하기")
                        .foregroundColor(Color(red: 218/255, green: 33/255, blue: 39/255))
                        .frame(width: 300, height: 45)
                        .background(Color.clear)
                        .cornerRadius(6)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color(red: 218/255, green: 33/255, blue: 39/255), lineWidth: 1)
                        )
                }
                .padding(.bottom, 32)
                
                Text("이메일로 전송된\n인증 코드를 입력해주세요")
                    .font(.title3)
                    .fontWeight(.semibold)
                TextField("1234", text: $codeInput)
                    .padding()
                    .background(Color(UIColor.systemGray6))
                    .cornerRadius(6)
                    .frame(width: 300)
                    .padding(.bottom, 32)
                
                Text("(선택)재학증 업로드")
                    .font(.title3)
                    .fontWeight(.semibold)
                Button(action: { showDocumentPicker = true }) {
                    Text("내 파일에서 찾기")
                        .foregroundColor(Color.blue)
                        .frame(width:300, height: 50)
                        .background(Color.clear)
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color.blue)
                        )
                }
                
                Spacer()
                Spacer()
                
                Button(action: {
                    verifyCode()
                    print("인증 코드: \(codeInput)")
                }) {
                    Text("다음")
                        .foregroundColor(.white)
                        .frame(width: 300, height: 45)
                        .background(Color(red: 218/255, green: 33/255, blue: 39/255))
                        .cornerRadius(6)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color(red: 218/255, green: 33/255, blue: 39/255), lineWidth: 1)
                        )
                }
                
            NavigationLink(destination: ContentView().navigationBarBackButtonHidden(true), isActive: $navigateToContentView) {
                    EmptyView()
                }
            }
            .padding()
            .ignoresSafeArea(.keyboard)
            .sheet(isPresented: $showDocumentPicker) {
                DocumentPicker(selectedPdfData: $selectedPdfData)
            }
            .onChange(of: selectedPdfData) { newData in
                if let pdfData = newData {
                    registerUnivPdf(pdfData: pdfData)
                }
            }
        }
        .ignoresSafeArea(.keyboard)
    }
    
    private func sendCode() {
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            print("대학교 이름 인코딩 실패")
            return
        }
        let userEmail = email
        print("user email: \(email)")
        print("encoded userUniversity: \(encodedUnivName)")
        
        guard let url = URL(string: "http://220.89.75.210:8080/api/univcert/send-code?email=\(userEmail)&univName=\(encodedUnivName)") else {
            print("유효하지 않은 URL입니다.")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    DispatchQueue.main.async {
                        isCodeSent = true
                        
                        if let data = data, let responseString = String(data: data, encoding: .utf8) {
                            print("서버 응답 성공: \(responseString)")
                        }
                    }
                } else {
                    if let data = data, let errorResponse = String(data: data, encoding: .utf8) {
                        print("서버 오류: 상태 코드 \(httpResponse.statusCode)")
                        print("서버 오류 응답: \(errorResponse)")
                    }
                }
            }
        }
        task.resume()
    }
    
    private func verifyCode() {
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            print("대학교 이름 인코딩 실패")
            return
        }
        
        let userEmail = email
        
        guard let url = URL(string: "http://220.89.75.210:8080/api/univcert/verify-code?email=\(userEmail)&univName=\(encodedUnivName)&code=\(codeInput)") else {
            print("유효하지 않은 URL입니다.")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    DispatchQueue.main.async {
                        if let data = data, let responseString = String(data: data, encoding: .utf8) {
                            print("서버 응답 성공: \(responseString)")
                            
                            if let jsonData = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
                               let success = jsonData["success"] as? Bool, success {
                                self.userVerify = true
                                checkStoredPdfUrls()
                                clearCertifiedUserList()
                                print()
                            }
                        }
                    }
                } else {
                    if let data = data, let errorResponse = String(data: data, encoding: .utf8) {
                        print("서버 오류: 상태 코드 \(httpResponse.statusCode)")
                        print("서버 오류 응답: \(errorResponse)")

                    }
                }
            }
        }
        task.resume()
    }
    
    private func checkStoredPdfUrls() {
        if let storedPdfUrlsString = UserDefaults.standard.string(forKey: "userPdfUrls"),
           let storedPdfUrlsData = storedPdfUrlsString.data(using: .utf8) {
            do {
                let storedPdfUrls = try JSONDecoder().decode([String: String].self, from: storedPdfUrlsData)
                print("이미 저장되어 있지롱 VerifyContentView: \(storedPdfUrls)")
                
                if !storedPdfUrls.isEmpty {
                    self.navigateToContentView = true
                } else {
                    registerEmptyPdf()
                }
            } catch {
                print("저장된 PDF URLs 디코딩 실패: \(error)")
                registerEmptyPdf()
            }
        } else {
            registerEmptyPdf()
        }
    }

    
    private func clearCertifiedUserList() {
        print("\nclearCertifiedUserList()")
        
        guard let url = URL(string: "http://220.89.75.210:8080/api/univcert/clear-list") else {
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
                DispatchQueue.main.async {
                    print("\n\nUser list cleared successfully")
                }
            } else {
                print("Unexpected response from server")
            }
        }
        
        task.resume()
    }
    
    private func registerEmptyPdf() {
        print("\nregisterPdf()")
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            print("대학교 이름 인코딩 실패")
            return
        }
        
        let userEmail = email

        print("(registerEmptyPdf)walletId: \(walletId)")
        print("user mail: \(userEmail)")
        print("encoded userUniversity: \(encodedUnivName)")
        print("uniVerified: \(univCheck)")

        guard let url = URL(string: "http://220.89.75.210:8080/api/certifications/register?walletId=\(walletId)&email=\(userEmail)&univName=\(encodedUnivName)&univCheck=\(univCheck)") else {
            print("유효하지 않은 URL입니다.")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var body = Data()

        let fileContent = ""
        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"file.pdf\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: application/pdf\r\n\r\n".data(using: .utf8)!)
        body.append(fileContent.data(using: .utf8)!)
        body.append("\r\n".data(using: .utf8)!)

        body.append("--\(boundary)--\r\n".data(using: .utf8)!)

        request.httpBody = body

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                return
            }

            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    if let data = data, let responseString = String(data: data, encoding: .utf8) {
                
                        print("서버 응답 데이터: \(responseString)")
                        
                        DispatchQueue.main.async {
                            let walletViewModel = WalletViewModel()
                            walletViewModel.getWallet()
                            self.navigateToContentView = true
                        }
                    }
                } else {
                    if let data = data, let errorResponse = String(data: data, encoding: .utf8) {
                        print("서버 오류: 상태 코드 \(httpResponse.statusCode)")
                        print("서버 오류 응답: \(errorResponse)")
                    }
                }
            }
        }
        task.resume()
    }
    
    private func registerUnivPdf(pdfData: Data) {
           guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
               print("대학교 이름 인코딩 실패")
               return
           }
           
           let userEmail = email
           guard let url = URL(string: "http://220.89.75.210:8080/api/certifications/register?walletId=\(walletId)&email=\(userEmail)&univName=\(encodedUnivName)&univCheck=\(univCheck)") else {
               print("유효하지 않은 URL입니다.")
               return
           }
           
           var request = URLRequest(url: url)
           request.httpMethod = "POST"
           
           let boundary = UUID().uuidString
           request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
           
           var body = Data()
           body.append("--\(boundary)\r\n".data(using: .utf8)!)
           body.append("Content-Disposition: form-data; name=\"file\"; filename=\"file.pdf\"\r\n".data(using: .utf8)!)
           body.append("Content-Type: application/pdf\r\n\r\n".data(using: .utf8)!)
           body.append(pdfData)
           body.append("\r\n".data(using: .utf8)!)
           body.append("--\(boundary)--\r\n".data(using: .utf8)!)
           
           request.httpBody = body
           print("HTTP Body Set: \(body.count) bytes of data")

           let task = URLSession.shared.dataTask(with: request) { data, response, error in
               if let error = error {
                   print("요청 실패: \(error.localizedDescription)")
                   return
               }
               
               if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
                   if let data = data, let responseString = String(data: data, encoding: .utf8) {
                       print("서버 응답 데이터: \(responseString)")
                       
                       DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                           walletViewModel.getWallet()
                       }
                   }
               } else {
                   print("서버 오류: 상태 코드 \((response as? HTTPURLResponse)?.statusCode ?? -1)")
                   if let data = data, let errorResponse = String(data: data, encoding: .utf8) {
                       print("서버 오류 응답: \(errorResponse)")
                   }
               }
           }
           task.resume()
       }
}

#Preview {
    VerifyUniversityView()
}
