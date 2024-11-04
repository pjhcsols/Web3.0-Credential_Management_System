//
//  GetUniversityView.swift
//  Wallet
//
//  Created by Seah Kim on 10/20/24.
//

import SwiftUI

struct GetUniversityView: View {
    
    @Binding var stack: NavigationPath
    @AppStorage("userUniversity") var univName: String = ""
    @AppStorage("checkUniversity") var isUnivChecked: Bool = false
    @AppStorage("userNickname") var userName: String = ""
    
    @State private var navigateToVerify = false
    
    let onVerificationComplete: () -> Void
    
    init(stack: Binding<NavigationPath>, onVerificationComplete: @escaping () -> Void) {
        _stack = stack
        self.onVerificationComplete = onVerificationComplete
    }
    
    var body: some View {
        NavigationStack() {
            VStack(alignment: .leading) {
                Spacer()
                Text("\(userName ?? "사용자")님의")
                    .font(.title2)
                    .fontWeight(.semibold)
                Text("재학중이거나 졸업한 대학교를")
                    .font(.title2)
                    .fontWeight(.semibold)
                Text("입력해주세요")
                    .font(.title2)
                    .fontWeight(.semibold)
                TextField("대학교 이름을 입력하세요", text: $univName)
                    .padding()
                    .background(Color(UIColor.systemGray6))
                    .cornerRadius(6)
                    .padding(.vertical, 10)
                    .frame(width: 300)
                Spacer()
                Spacer()
                Button(action: {
                    checkUniversity(univName: univName)
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
                .navigationDestination(isPresented: $navigateToVerify) {
                        VerifyUniversityView(
                            stack: $stack,
                            onVerificationComplete: onVerificationComplete // Pass closure down
                        )
                    }
                }
            .padding()
            .ignoresSafeArea(.keyboard)
            .navigationBarBackButtonHidden(true)
        }
    }

    private func checkUniversity(univName: String) {
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            print("대학교 이름 인코딩 실패")
            return
        }
        
        guard let url = URL(string: "http://121.151.25.247:8080/api/univcert/check-univ?univName=\(encodedUnivName)") else {
            print("유효하지 않은 URL입니다.")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200, let data = data {
                    do {
                        if let jsonResponse = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
                            print("서버 응답 JSON: \(jsonResponse)")
                            
                            if let success = jsonResponse["success"] as? Int, success == 1 {
                                print("서버 응답 성공: success == 1")
                                DispatchQueue.main.async {
                                    self.isUnivChecked = true
                                    self.navigateToVerify = true
                                    UserDefaults.standard.set(univName, forKey: "userUniversity")
                                }
                            } else {
                                print("서버 응답 실패 또는 다른 상태")
                            }
                        }
                    } catch {
                        print("JSON 디코딩 실패: \(error.localizedDescription)")
                    }
                } else {
                    print("서버 오류: 상태 코드 \(httpResponse.statusCode)")
                }
            }
        }
        task.resume()
    }
}
