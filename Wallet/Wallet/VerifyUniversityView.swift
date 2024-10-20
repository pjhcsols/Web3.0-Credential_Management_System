//
//  VerifyUniversityView.swift
//  Wallet
//
//  Created by Seah Kim on 10/20/24.
//

import SwiftUI

struct VerifyUniversityView: View {
    
    @AppStorage("userNickname") var nickname: String = ""
    @AppStorage("univName") var univName: String = ""
    @AppStorage("univCheck") var univCheck: Bool = false
    @AppStorage("walletId") var walletId: String = ""
    @AppStorage("email") var email: String = ""
    @AppStorage("pdfUrl") var pdfUrl: String = ""
    @AppStorage("code") var code: Int = 0
    
    
    @State private var navigateToContentView: Bool = false
    @State private var isCodeSent: Bool = false
    @State private var codeInput: String = ""
    
    var body: some View {
        NavigationStack {
            VStack(alignment: .leading) {
                Spacer()
                Text("\(nickname)님의")
                    .font(.title2)
                    .fontWeight(.semibold)
                Text("이메일을 입력해주세요")
                    .font(.title2)
                    .fontWeight(.semibold)
                TextField("exaple@knu.ac.kr", text: $email)
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
                .padding(.bottom, 48)
                
                Text("이메일로 전송된\n인증 코드를 입력해주세요")
                    .font(.title3)
                    .fontWeight(.semibold)
                TextField("1234", text: $codeInput)
                    .padding()
                    .background(Color(UIColor.systemGray6))
                    .cornerRadius(6)
                    .frame(width: 300)
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
            .onAppear {
            }
        }
    }
    
    private func sendCode() {
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            print("대학교 이름 인코딩 실패")
            return
        }
        
        print("email: \(email)")
        print("4자리 코드: \(codeInput)")
        print("encoded univName: \(encodedUnivName)")
        
        guard let url = URL(string: "http://192.168.1.188:8080/api/univcert/send-code?email=\(email)&univName=\(encodedUnivName)") else {
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
        
        guard let url = URL(string: "http://192.168.1.188:8080/api/univcert/verify-code?email=\(email)&univName=\(encodedUnivName)&code=\(codeInput)") else {
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

                                self.navigateToContentView = true
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
}

#Preview {
    VerifyUniversityView()
}

#Preview {
    VerifyUniversityView()
}
