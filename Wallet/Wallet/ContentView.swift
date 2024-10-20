//
//  ContentView.swift
//  Wallet
//
//  Created by Seah Kim on 9/29/24.
//

import SwiftUI

struct ContentView: View {
    @State private var isExpanded = true
    @State private var showHistory = false
    @AppStorage("userNickname") var nickname: String = ""
    @AppStorage("walletId") var walletId: String = ""
    @AppStorage("userEmail") var email: String = ""
    @AppStorage("univName") var univName: String = ""
    @AppStorage("univCheck") var univCheck: Bool = false
    @AppStorage("pdfUrl") var pdfUrl: String = ""

    var body: some View {
        VStack {
            HStack {
                Text("web3wallet")
                    .font(.title)
                    .fontWeight(.light)
                    .padding(.leading, 12)
                Spacer()
                NavigationLink(destination: AddCertificationView().navigationBarBackButtonHidden(true)){
                    Image(systemName: "plus")
                        .font(.title)
                        .foregroundColor(.gray)
                }
                Button(action: {
                }){
                    Image(systemName: "gearshape")
                        .font(.title)
                        .foregroundColor(.gray)
                }
            }
            .padding(.top, 12)
            
            Button(action: {
                withAnimation(.easeInOut(duration: 0.3)) {
                    isExpanded.toggle()
                    if isExpanded {
                        showHistory = false
                    } else {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                            withAnimation(.easeInOut(duration: 0.5)) {
                                showHistory.toggle()
                            }
                        }
                    }
                }
            }) {
                VStack {
                    HStack {
                        Text("경북멋쟁이 인증서")
                            .font(.custom("KNU TRUTH", size: 18))
                            .foregroundColor(.black)
                            .padding(.top, 24)
                            .padding(.leading, 24)
                        Spacer()
                        Text("2024.01.01.")
                            .font(.subheadline)
                            .foregroundColor(.black)
                            .padding(.top, 24)
                            .padding(.trailing, 24)
                    }
                    Text(nickname)
                        .font(isExpanded ? .title : .title2)
                        .fontWeight(.medium)
                        .foregroundColor(.black)
                        .padding(.top, isExpanded ? 24 : 0)
                    
                    Image("images/knu")
                        .resizable()
                        .scaledToFit()
                        .frame(width: isExpanded ? 230 : 120, height: isExpanded ? 230 : 120)
                        .padding(.top, isExpanded ? 36.0 : 0)
                    
                    Spacer()
                }
                .background(Color.white)
                .cornerRadius(10)
                .shadow(color: Color.gray.opacity(0.3), radius: 10, x: 0, y: 0)
            }
            .frame(width: UIScreen.main.bounds.width * 0.85,
                   height: isExpanded ? (UIScreen.main.bounds.width * 0.85) * (3.0 / 2.0) : (UIScreen.main.bounds.width * 0.85) * (2.0 / 3.0),
                   alignment: .top)
            if showHistory {
                HistoryListView()
                    .transition(.opacity)
                    .animation(.easeInOut(duration: 0.1), value: showHistory)
                        }
        }
        .padding()
        .frame(maxHeight: .infinity, alignment: .top)
        .onAppear {
            if pdfUrl.isEmpty {
                registerPdf()
            } else{
                print("pdfURL: \(pdfUrl)")
            }
        }
    }

    private func registerPdf() {
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            print("대학교 이름 인코딩 실패")
            return
        }

        print("walletId: \(walletId)")
        print("email: \(email)")
        print("encoded univName: \(encodedUnivName)")
        print("univCheck: \(univCheck)")

        guard let url = URL(string: "http://220.81.24.60:8080/api/certifications/register?walletId=\(walletId)&email=\(email)&univName=\(encodedUnivName)&univCheck=\(univCheck)") else {
            print("유효하지 않은 URL입니다.")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var body = Data()

        let fileContent = "test"
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
                        print("서버 응답 성공: PDF 등록 완료")
                        print("서버 응답 데이터: \(responseString)")
                        print("pdfURL: \(pdfUrl)")
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
    ContentView()
}
