//
//  University.swift
//  Wallet
//
//  Created by Seah Kim on 10/20/24.
//

import SwiftUI

struct University: View {
    
    @AppStorage("userNickname") var nickname: String = ""
    @AppStorage("univName") var univName: String = ""
    @AppStorage("univCheck") var univCheck: Bool = false
    @State private var isUnivChecked: Bool = false
    
    var body: some View {
        NavigationStack {
            VStack(alignment: .leading) {
                Spacer()
                Text("\(nickname)님의")
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
                
                NavigationLink(destination: ContentView().navigationBarBackButtonHidden(true), isActive: $isUnivChecked) {
                    EmptyView()
                }
            }
            .padding()
            .ignoresSafeArea(.keyboard)
        }
    }

    private func checkUniversity(univName: String) {
        let baseURL = "http://192.168.1.188:8080/api/univcert/check-univ"
        guard let url = URL(string: "\(baseURL)?univName=\(univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")") else {
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
                DispatchQueue.main.async {
                    self.isUnivChecked = true  // 응답이 왔으면 이동
                }
                if httpResponse.statusCode == 200, let data = data {
                    do {
                        if let jsonResponse = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
                            print("서버 응답 JSON: \(jsonResponse)")
                            if let success = jsonResponse["success"] as? Bool {
                                DispatchQueue.main.async {
                                    self.univCheck = success
                                }
                            }
                            print("univ name: \(univName)")
                            print("univ check: \(univCheck)")
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

#Preview {
    University()
}
