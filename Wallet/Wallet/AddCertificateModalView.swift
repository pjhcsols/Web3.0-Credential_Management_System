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
    
    @Binding var stack: NavigationPath
    
    let certification: Certification
    let onDismissAndNavigate: () -> Void
    
    @AppStorage("userPdfUrls") var pdfUrls: String = ""
    @AppStorage("userUniversity") var univName: String = ""
    @AppStorage("userWalletId") var walletId: String = ""
    @AppStorage("userEmail") private var email: String = ""
    @AppStorage("checkUniversity") var univCheck: Bool = false
    
    @State var allAgree = false
    @State var item1Checked = false
    @State var pdfIssued = false
    
    //    @State private var showPinEntry = false
    //    @State private var selectedPdfData: Data? = nil
    //    @State private var showDocumentPicker = false
    @State private var showGetUniversityView = false
    
    var body: some View {
        NavigationStack() {
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
//                            showGetUniversityView = true
                            dismiss() // Dismiss the modal
                            onDismissAndNavigate()
                        }) {
                            Text("인증 후 지갑에 넣기")
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
                        .navigationDestination(isPresented: $showGetUniversityView) {
                            GetUniversityView(stack: $stack, onVerificationComplete: {
                                stack = NavigationPath() // Reset the navigation stack
                            })
//                            GetUniversityView(stack: $stack)
                        }
                    }
                    .padding(.top, 48)
                }
            }
            //            .sheet(isPresented: $showPinEntry) {
            //                CheckByCodeView { success in
            //                    if success {
            //                        showDocumentPicker = true
            //                    }
            //                }
            //            }
            //            .sheet(isPresented: $showDocumentPicker) {
            //                DocumentPicker(selectedPdfData: $selectedPdfData)
            //            }
            //            .onChange(of: selectedPdfData) { pdfData in
            //                if let pdfData = pdfData {
            //                    registerUnivPdf(pdfData: pdfData)
            //                }
            //            }
            .alert(isPresented: $pdfIssued) {
                Alert(title: Text("PDF 발급 완료"), message: Text("PDF가 성공적으로 발급되었습니다."), dismissButton: .default(Text("확인")) {
                    dismiss()
                })
            }
        }
    }
    
    //    private func authenticateUser() {
    //        let context = LAContext()
    //        var error: NSError?
    //
    //        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
    //            let reason = "Face ID를 사용하여 인증하세요."
    //
    //            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, authenticationError in
    //                DispatchQueue.main.async {
    //                    if success {
    //                        print("Face ID 인증 성공")
    //                        showDocumentPicker = true
    //                    } else {
    //                        print("Face ID 인증 실패, PIN 입력으로 전환")
    //                        showPinEntry = true
    //                    }
    //                }
    //            }
    //        } else {
    //            DispatchQueue.main.async {
    //                print("Face ID를 사용할 수 없음, PIN 입력으로 전환")
    //                showPinEntry = true
    //            }
    //        }
    //    }
    
}

#Preview {
    AddCertificateModalView(
        stack: .constant(NavigationPath()),
        certification: Certification(name: "Sample"),
        onDismissAndNavigate: {
            // Placeholder action for preview
        }
    )
}
