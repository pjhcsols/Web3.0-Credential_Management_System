//
//  AddCertificationView.swift
//  Wallet
//
//  Created by Seah Kim on 10/10/24.
//

import SwiftUI

struct AddCertificationView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var showSheet = false
    @State private var selectedCertification: Certification?
    
    let certificationArray = certifications
    
    var body: some View {
        VStack {
            ZStack {
                Text("전자증명서")
                    .font(.title)
                    .fontWeight(.light)
                HStack {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }){
                        Image(systemName: "chevron.left")
                            .font(.title)
                            .foregroundColor(.gray)
                    }
                    Spacer()
                }
            }
            .padding(.top, 12)
            .padding(.horizontal)
            
            List(certificationArray) { item in
                Button(action: {
                    selectedCertification = item
                    print("\(item.name) 클릭됨")
                }) {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(item.name)
                                .font(.body)
                                .fontWeight(.medium)
                            Text(item.organ)
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
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
            AddCertificateModalView(certification: certification)
                .presentationDetents([.fraction(0.3)])
                .presentationDragIndicator(.visible)
        }
    }
}

let certifications: [Certification] = [
    Certification(name: "학생증", organ: "써트피아"),
    Certification(name: "자격증", organ: "정부 24"),
    Certification(name: "주민등록증", organ: "정부 24"),
    Certification(name: "운전면허증", organ: "정부 24"),
    Certification(name: "여권", organ: "정부 24"),
]

struct Certification: Identifiable {
    let id = UUID()
    let name: String
    let organ: String
}

#Preview {
    AddCertificationView()
}
