//
//  AddCertificationView.swift
//  Wallet
//
//  Created by Seah Kim on 10/10/24.
//

import SwiftUI

struct AddCertificationView: View {
    @State private var showSheet = false
    @State private var selectedCertification: Certification? = nil
    
    let certificationArray = certifications
    var body: some View {
        VStack {
            ZStack {
                Text("전자증명서")
                    .font(.title)
                    .fontWeight(.light)
                HStack {
                    Button(action: {
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
                    showSheet = true
                    print("\(item.name) 클릭됨")
                }) {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(item.name)
                                .font(/*@START_MENU_TOKEN@*/.body/*@END_MENU_TOKEN@*/)
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
        .sheet(isPresented: $showSheet) {
            if let certification = selectedCertification {
                CertificationDetailView(certification: certification)
            }
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

struct CertificationDetailView: View {
    let certification: Certification
    @State var allAgree = false
    @State var item1Checked = false
    
    var body: some View {
        VStack {
            Text(certification.name)
                .font(.title3)
                .fontWeight(.bold)
            VStack(alignment: .leading){
                Button(action: {
                    allAgree.toggle()
                    item1Checked = allAgree
                }) {
                    HStack {
                        Image(allAgree ? "images/checked" : "images/unchecked")
                        Text("전체동의하기")
                            .font(.body)
                            .fontWeight(.bold)
                            .foregroundColor(.black)
                    }
                }
                HStack {
                    Image(item1Checked ? "images/checked" : "images/unchecked")
                    Text("[필수] 경북멋쟁이 개인정보 제공 동의")
                        .font(.body)
                }
            }
            
        }
        .presentationDetents([.fraction(0.33)])
    }
}


#Preview {
    AddCertificationView()
}
