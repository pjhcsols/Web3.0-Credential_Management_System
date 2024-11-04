//
//  AddCertificationView.swift
//  Wallet
//
//  Created by Seah Kim on 10/10/24.
//

import SwiftUI

struct AddCertificationView: View {
    @Environment(\.presentationMode) var presentationMode
    
    @State private var stack = NavigationPath()
    
    @AppStorage("userPdfUrls") var pdfUrls: String = ""
    @AppStorage("userWalletId") var walletId: String = ""
    @AppStorage("certificationList") private var certificationListData: Data?
    
    @State private var showSheet = false
    @State private var errorMessage: String?
    @State private var selectedCertification: Certification?
    @State private var certifications: [Certification] = Certification.defaultCertifications
    @State private var showGetUniversityView = false
    
    var body: some View {
        NavigationStack(){
            VStack {
                ZStack {
                    Text("전자증명서")
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
                .padding(.top, 12)
                .padding(.horizontal)
                
                List(certifications) { item in
                    Button(action: {
                        selectedCertification = item
                        print("\(item.name) 클릭됨")
                    }) {
                        HStack {
                            Text(item.name)
                                .font(.body)
                                .fontWeight(.medium)
                            Spacer()
                            Image(systemName: "plus")
                                .foregroundColor(Color(red: 218/255, green: 33/255, blue: 39/255))
                        }
                        .padding()
                        .background(Color.white)
                    }
                    .buttonStyle(PlainButtonStyle())
                    .listRowInsets(EdgeInsets())
                }
                .listStyle(PlainListStyle())
                .padding(.horizontal, 16.0)
                .scrollContentBackground(.hidden)
            }
            .padding(.top)
            .sheet(item: $selectedCertification) { certification in
                AddCertificateModalView(
                    stack: $stack,
                    certification: certification,
                    onDismissAndNavigate: {
                        showGetUniversityView = true
                    }
                )
                .presentationDetents([.fraction(0.3)])
                .presentationDragIndicator(.visible)
            }
            .navigationDestination(isPresented: $showGetUniversityView) {
                GetUniversityView(
                    stack: $stack,
                    onVerificationComplete: {
                        showGetUniversityView = false
                        stack = NavigationPath()
                    }
                )
            }
        }
    }
}

#Preview {
    AddCertificationView()
}
