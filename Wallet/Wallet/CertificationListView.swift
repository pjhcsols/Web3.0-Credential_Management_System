//
//  CertificationListView.swift
//  Wallet
//
//  Created by Seah Kim on 10/5/24.
//

import SwiftUI
import PDFKit

struct CertificationListView: View {
    @AppStorage("userPdfUrls") var pdfUrls: String = ""
    @AppStorage("certificationList") private var certificationListData: Data?
    @AppStorage("userWalletId") var walletId: String = ""
    
    @State private var selectedCertification: Certification?
    @State private var certificationList: [Certification] = []
    @State private var isShowingPDF = false
    @State private var selectedPDFURL = URL(string: "https://s3.ap-northeast-2.amazonaws.com/basilium-product-bucket/3_student_certifications.pdf")
    
    var body: some View {
        VStack(alignment: .leading) {
            Spacer()
            Text("내 인증서")
                .font(.title)
                .fontWeight(.bold)
                .padding()
            List {
                ForEach(certificationList) { item in
                    VStack(alignment: .leading) {
                        Button(action: {
                            selectedCertification = item
                            fetchPDFURL(for: item.name)
                            print("\(item.name) clicked")
                        }) {
                            HStack {
                                Text(item.name)
                                    .font(.body)
                                    .fontWeight(.medium)
                                Spacer()
                                Text("보기")
                                    .font(.body)
                                    .foregroundColor(Color(red: 218/255, green: 33/255, blue: 39/255))
                            }
                            .background(Color.white)
                        }
                        .buttonStyle(PlainButtonStyle())
                        .listRowInsets(EdgeInsets())
                    }
                    .swipeActions(edge: .trailing) {
                        Button(role: .destructive) {
                            deleteCertification(item)
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                    }
                }
            }
            .listStyle(PlainListStyle())
            .onAppear {
                loadCertifications()
            }
            .sheet(isPresented: $isShowingPDF) {
                if let pdfURL = selectedPDFURL {
                    PDFViewer(url: pdfURL)
                } else {
                    Text("No PDF selected.")
                }
            }
        }
    }
    
    private func loadCertifications() {
        guard let data = certificationListData else { return }
        do {
            certificationList = try JSONDecoder().decode([Certification].self, from: data)
            print("Certifications loaded: \(certificationList)")
        } catch {
            print("Failed to load certifications: \(error)")
        }
    }
    
    private func fetchPDFURL(for certificationName: String) {
        let baseCertificationName = certificationName.split(separator: "_").first.map(String.init) ?? certificationName
        
        guard let data = pdfUrls.data(using: .utf8),
              let pdfDict = try? JSONDecoder().decode([String: String].self, from: data) else {
            print("PDF URL lookup failed: data decoding error")
            return
        }
        
        if let pdfURLString = pdfDict.first(where: { $0.key.starts(with: baseCertificationName) })?.value,
           let pdfURL = URL(string: pdfURLString) {
            selectedPDFURL = pdfURL
            isShowingPDF = true
            print("Selected PDF URL: \(pdfURL)")
        } else {
            print("PDF URL lookup failed: no matching key found")
        }
    }
    
    private func deleteCertification(_ certification: Certification) {
        guard let pdfDict = try? JSONDecoder().decode([String: String].self, from: pdfUrls.data(using: .utf8) ?? Data()) else {
            print("Failed to decode PDF URLs from userPdfUrls")
            return
        }

        print("PDF URLs Dictionary: \(pdfDict)")
        
        let baseCertificationName = certification.name.split(separator: "_").first.map(String.init) ?? certification.name
        guard let pdfUrl = pdfDict.first(where: { $0.key.starts(with: baseCertificationName) })?.value else {
            print("No URL found for certification \(certification.name)")
            return
        }
        
        guard let userWalletId = Int(walletId) else {
            print("Failed to convert walletId to Int")
            return
        }
        
        deletePDFFile(pdfUrl: pdfUrl, walletId: userWalletId, certificateName: certification.name) { success in
            if success {
                // Remove from local list and save updated list to AppStorage
                if let index = certificationList.firstIndex(where: { $0.id == certification.id }) {
                    certificationList.remove(at: index)
                    saveCertifications()
                    print("Certification \(certification.name) deleted successfully.")
                }
            }
        }
    }


    
    private func saveCertifications() {
        if let encoded = try? JSONEncoder().encode(certificationList) {
            certificationListData = encoded
        }
    }
    
    private func deletePDFFile(pdfUrl: String, walletId: Int, certificateName: String, completion: @escaping (Bool) -> Void) {
        // Encode pdfUrl for safe transmission
        guard let encodedPdfUrl = pdfUrl.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: "http://220.89.75.210:8080/api/certifications/delete-pdf?pdfUrl=\(encodedPdfUrl)&walletId=\(walletId)&certificateName=\(certificateName)") else {
            print("Invalid URL for deletion request")
            completion(false)
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("Error deleting certification: \(error.localizedDescription)")
                completion(false)
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    print("Certification deleted successfully from server.")
                    completion(true)
                } else {
                    print("Failed to delete certification from server with status code: \(httpResponse.statusCode)")
                    completion(false)
                }
            } else {
                print("Failed to cast response as HTTPURLResponse")
                completion(false)
            }
        }
        task.resume()
    }

}

struct PDFViewer: View {
    let url: URL

    var body: some View {
        PDFKitView(url: url)
            .edgesIgnoringSafeArea(.all)
    }
}

struct PDFKitView: UIViewRepresentable {
    let url: URL
    
    func makeUIView(context: Context) -> PDFView {
        let pdfView = PDFView()
        pdfView.autoScales = true
        pdfView.displayMode = .singlePage
        pdfView.displaysAsBook = false
        pdfView.isUserInteractionEnabled = false
        
        print("Attempting to load PDF from URL: \(url)")
        
        DispatchQueue.global(qos: .userInitiated).async {
            if let document = PDFDocument(url: url) {
                DispatchQueue.main.async {
                    pdfView.document = document
                    if let firstPage = document.page(at: 0) {
                        pdfView.go(to: firstPage)
                    }
                    print("PDF document successfully loaded, showing first page only.")
                }
            } else {
                DispatchQueue.main.async {
                    print("Failed to load PDF document from URL: \(url)")
                }
            }
        }
        
        return pdfView
    }

    func updateUIView(_ pdfView: PDFView, context: Context) {}
}
