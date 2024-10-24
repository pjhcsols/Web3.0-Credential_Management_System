//
//  VerifyUniversityView.swift
//  Wallet
//
//  Created by Seah Kim on 10/20/24.
//

import SwiftUI

struct VerifyUniversityView: View {
    @AppStorage("userUniversity") var univName: String = ""
    @AppStorage("userEmail") var email: String = ""
    @AppStorage("userVerified") var userVerify: Bool = false
    @State private var codeInput: String = ""
    @State private var isCodeSent: Bool = false
    @State private var navigateToContentView: Bool = false
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
        print("* * * * * * * * * * * * * * * * * *")
        print("VerifyUniversityView.swift\n")
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            print("대학교 이름 인코딩 실패")
            print("\nVerifyUniversityView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")
            return
        }
        let userEmail = email
        print("user email: \(email)")
        print("4자리 코드: \(codeInput)")
        print("encoded userUniversity: \(encodedUnivName)")
        
        guard let url = URL(string: "http://121.151.45.73:8080/api/univcert/send-code?email=\(userEmail)&univName=\(encodedUnivName)") else {
            print("유효하지 않은 URL입니다.")
            print("\nVerifyUniversityView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")

            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                print("\nVerifyUniversityView.swift")
                print("* * * * * * * * * * * * * * * * * *\n\n")

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
                        print("\nVerifyUniversityView.swift")
                        print("* * * * * * * * * * * * * * * * * *\n\n")

                    }
                }
            }
        }
        task.resume()
    }
    
    private func verifyCode() {
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            print("대학교 이름 인코딩 실패")
            print("\nVerifyUniversityView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")

            return
        }
        
        let userEmail = email
        
        guard let url = URL(string: "http://121.151.45.73:8080/api/univcert/verify-code?email=\(userEmail)&univName=\(encodedUnivName)&code=\(codeInput)") else {
            print("유효하지 않은 URL입니다.")
            print("\nVerifyUniversityView.swift")
            print("* * * * * * * * * * * * * * * * * *\n\n")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                print("\nVerifyUniversityView.swift")
                print("* * * * * * * * * * * * * * * * * *\n\n")

            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    DispatchQueue.main.async {
                        if let data = data, let responseString = String(data: data, encoding: .utf8) {
                            print("서버 응답 성공: \(responseString)")
                            
                            if let jsonData = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
                               let success = jsonData["success"] as? Bool, success {
                                self.userVerify = true
                                self.navigateToContentView = true
                                
                                clearCertifiedUserList()
                                
                                print("\nVerifyUniversityView.swift")
                                print("* * * * * * * * * * * * * * * * * *\n\n")

                            }
                            print("\nVerifyUniversityView.swift")
                            print("* * * * * * * * * * * * * * * * * *\n\n")

                        }
                    }
                } else {
                    if let data = data, let errorResponse = String(data: data, encoding: .utf8) {
                        print("서버 오류: 상태 코드 \(httpResponse.statusCode)")
                        print("서버 오류 응답: \(errorResponse)")
                        print("\nVerifyUniversityView.swift")
                        print("* * * * * * * * * * * * * * * * * *\n\n")

                    }
                }
            }
        }
        task.resume()
    }
    
    private func clearCertifiedUserList() {
        guard let url = URL(string: "http://121.151.45.73:8080/api/univcert/clear-list") else {
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
                    print("User list cleared successfully")
                }
            } else {
                print("Unexpected response from server")
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
