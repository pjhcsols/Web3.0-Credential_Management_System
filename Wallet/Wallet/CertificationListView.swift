//
//  HistoryListView.swift
//  Wallet
//
//  Created by Seah Kim on 10/5/24.
//

import SwiftUI
import PDFKit

struct CertificationListView: View {
    @AppStorage("userPdfUrls") var pdfUrls: String = ""
    @AppStorage("certificationList") private var certificationListData: Data?
    
    @State private var selectedCertification: Certification?
    @State private var certificationList: [Certification] = []
    @State private var isShowingPDF = false
    @State private var selectedPDFURL = URL(string: "https://s3.ap-northeast-2.amazonaws.com/basilium-product-bucket/3_student_certifications.pdf")
    
    var body: some View {
        VStack(alignment: .leading){
            Spacer()
            Text("내 인증서")
                .font(.title)
                .fontWeight(.bold)
                .padding()
            List(certificationList) { item in
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
        
        // Find the first matching key that starts with baseCertificationName
        if let pdfURLString = pdfDict.first(where: { $0.key.starts(with: baseCertificationName) })?.value,
           let pdfURL = URL(string: pdfURLString) {
            selectedPDFURL = pdfURL
            isShowingPDF = true
            print("Selected PDF URL: \(pdfURL)")
        } else {
            print("PDF URL lookup failed: no matching key found")
        }
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

#Preview {
    CertificationListView()
}
