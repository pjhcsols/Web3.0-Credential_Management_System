//
//  AddCertificateModalView.swift
//  Wallet
//
//  Created by Seah Kim on 10/10/24.
//

import SwiftUI
import LocalAuthentication
import UniformTypeIdentifiers


struct AddCertificateModalView: View {
    @Environment(\.dismiss) var dismiss
    
    let certification: Certification
    
    @AppStorage("userPdfUrls") var pdfUrls: String = ""
    @AppStorage("userUniversity") var univName: String = ""
    @AppStorage("userWalletId") var walletId: String = ""
    @AppStorage("userEmail") private var email: String = ""
    @AppStorage("checkUniversity") var univCheck: Bool = false
    
    @State var allAgree = false
    @State var item1Checked = false
    @State var pdfIssued = false

    @State private var showPinEntry = false
    @State private var selectedPdfData: Data? = nil
    @State private var showDocumentPicker = false

    var body: some View {
        NavigationStack {
            ZStack {
                Color.white.ignoresSafeArea(edges: .all)
                ScrollView {
                    VStack() {
                        Text(certification.name)
                            .font(.title2)
                            .bold()
                            .foregroundColor(.black)
                        
                        VStack(alignment: .leading, spacing: 10) {
                            Button(action: {
                                allAgree.toggle()
                                item1Checked = allAgree
                            }) {
                                HStack {
                                    Image(allAgree ? "images/checked" : "images/unchecked")
                                        .resizable()
                                        .frame(width: 20, height: 20)
                                    Text("전체동의하기")
                                        .font(.body)
                                        .fontWeight(.bold)
                                        .foregroundColor(.black)
                                }
                            }
                            Button(action: {
                                item1Checked.toggle()
                                if !item1Checked {
                                    allAgree = false
                                }
                            }) {
                                HStack {
                                    Image(item1Checked ? "images/checked" : "images/unchecked")
                                        .resizable()
                                        .frame(width: 20, height: 20)
                                    Text("[필수] 경북멋쟁이 개인정보 제공 동의")
                                        .font(.body)
                                        .foregroundColor(.black)
                                }
                            }
                        }
                        
                        Button(action: {
                            authenticateUser()
                        }) {
                            Text("지갑에 넣기")
                                .fontWeight(.medium)
                                .foregroundColor(.white)
                                .frame(width: 256, height: 45)
                                .background(allAgree && item1Checked ? Color(red: 218/255, green: 33/255, blue: 39/255) : Color.gray)
                                .cornerRadius(6)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 6)
                                        .stroke(allAgree && item1Checked ? Color(red: 218/255, green: 33/255, blue: 39/255) : Color.gray, lineWidth: 1)
                                )
                        }
                        .disabled(!(allAgree && item1Checked))
                        .padding()
                    }
                    .padding(.top, 48)
                }
            }
            .sheet(isPresented: $showPinEntry) {
                CheckByCodeView { success in
                    if success {
                        showDocumentPicker = true
                    }
                }
            }
            .sheet(isPresented: $showDocumentPicker) {
                DocumentPicker(selectedPdfData: $selectedPdfData)
            }
            .onChange(of: selectedPdfData) { pdfData in
                if let pdfData = pdfData {
                    registerUnivPdf(pdfData: pdfData)
                }
            }
            .alert(isPresented: $pdfIssued) {
                Alert(title: Text("PDF 발급 완료"), message: Text("PDF가 성공적으로 발급되었습니다."), dismissButton: .default(Text("확인")) {
                    dismiss()
                })
            }
        }
    }
    
    private func authenticateUser() {
        let context = LAContext()
        var error: NSError?
        
        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            let reason = "Face ID를 사용하여 인증하세요."
            
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, authenticationError in
                DispatchQueue.main.async {
                    if success {
                        print("Face ID 인증 성공")
                        showDocumentPicker = true
                    } else {
                        print("Face ID 인증 실패, PIN 입력으로 전환")
                        showPinEntry = true
                    }
                }
            }
        } else {
            DispatchQueue.main.async {
                print("Face ID를 사용할 수 없음, PIN 입력으로 전환")
                showPinEntry = true
            }
        }
    }
    
    private func registerUnivPdf(pdfData: Data) {
        print("\nregisterPdf()")
        guard let encodedUnivName = univName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let userEmail = email.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)
        else {
            print("이메일 또는 대학교 이름 인코딩 실패")
            return
        }
        
        guard let encodeCertName = ("재학증").addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)
        else {
            print("인코딩 실패")
            return
        }
                
//        guard let url = URL(string: "http://220.89.75.210:8080/api/certifications/replace-pdf?page=1&walletId=\(walletId)&certificateName=\(encodeCertName)") else {
//            print("유효하지 않은 URL입니다.")
//            return
//        }
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
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
                if let data = data, let responseString = String(data: data, encoding: .utf8) {
                    print("서버 응답 데이터: \(responseString)")
                }
            } else {
                print("서버 오류: 상태 코드 \((response as? HTTPURLResponse)?.statusCode ?? -1)")
            }
        }
        task.resume()
    }
}

//struct DocumentPicker: UIViewControllerRepresentable {
//    @Binding var selectedPdfData: Data?
//
//    func makeUIViewController(context: Context) -> UIDocumentPickerViewController {
//        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [UTType.pdf])
//        picker.allowsMultipleSelection = false
//        picker.delegate = context.coordinator
//        return picker
//    }
//
//    func updateUIViewController(_ uiViewController: UIDocumentPickerViewController, context: Context) {}
//    
//    func makeCoordinator() -> Coordinator {
//        Coordinator(self)
//    }
//
//    class Coordinator: NSObject, UIDocumentPickerDelegate {
//        var parent: DocumentPicker
//
//        init(_ parent: DocumentPicker) {
//            self.parent = parent
//        }
//
//        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
//            guard let selectedFileURL = urls.first else { return }
//            
//            // Security-scoped resource 시작
//            if selectedFileURL.startAccessingSecurityScopedResource() {
//                defer { selectedFileURL.stopAccessingSecurityScopedResource() }
//                
//                do {
//                    let pdfData = try Data(contentsOf: selectedFileURL)
//                    DispatchQueue.main.async {
//                        self.parent.selectedPdfData = pdfData
//                    }
//                } catch {
//                    print("파일 데이터를 읽어오는데 실패했습니다: \(error)")
//                }
//            } else {
//                print("파일에 접근할 수 없습니다.")
//            }
//        }
//
//        func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
//            print("문서 선택 취소됨")
//        }
//    }
//}

#Preview {
    AddCertificateModalView(certification: Certification(name: "Sample"))
}
