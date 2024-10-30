//
//  AddCertificationView.swift
//  Wallet
//
//  Created by Seah Kim on 10/10/24.
//

import SwiftUI

struct AddCertificationView: View {
    @Environment(\.presentationMode) var presentationMode
    
    @AppStorage("userPdfUrls") var pdfUrls: String = ""
    @AppStorage("userWalletId") var walletId: String = ""
    @AppStorage("certificationList") private var certificationListData: Data?
    
    @State private var showSheet = false
    @State private var errorMessage: String?
    @State private var selectedCertification: Certification?
    @State private var certifications: [Certification] = []
    
    var body: some View {
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
            .onAppear {
                fetchCertifications()
            }
        }
        .padding(.top)
        .sheet(item: $selectedCertification) { certification in
            AddCertificateModalView(certification: certification)
                .presentationDetents([.fraction(0.3)])
                .presentationDragIndicator(.visible)
        }
    }
    
    private func fetchCertifications() {
        guard !pdfUrls.isEmpty, let url = URL(string: "http://220.89.75.210:8080/api/certifications/cert-names?walletId=\(walletId)")
        else {
            print("유효하지 않은 URL입니다.")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("요청 실패: \(error.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200, let data = data {
                    do {
                        if let certificationNames = try JSONSerialization.jsonObject(with: data, options: []) as? [String] {
                            DispatchQueue.main.async {
                                let newCertifications = certificationNames.map { Certification(name: $0) }
                                                            
                                loadCertifications()
                                let combinedCertifications = Set(certifications + newCertifications)
                                                    
                                certifications = Array(combinedCertifications)
                                saveCertifications()
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
    
    private func saveCertifications() {
        do {
            let data = try JSONEncoder().encode(certifications)
            certificationListData = data
        } catch {
            print("Certification 저장 실패: \(error)")
        }
    }
    
    private func loadCertifications() {
        guard let data = certificationListData else { return }
        do {
            certifications = try JSONDecoder().decode([Certification].self, from: data)
        } catch {
            print("Certification 로드 실패: \(error)")
        }
    }
}

#Preview {
    AddCertificationView()
}
