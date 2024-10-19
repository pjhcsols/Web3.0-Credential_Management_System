//
//  AddCertificateModalView.swift
//  Wallet
//
//  Created by Seah Kim on 10/10/24.
//

import SwiftUI

struct AddCertificateModalView: View {
    @Environment(\.dismiss) var dismiss
    
    let certification: Certification
    
    @State var allAgree = false
    @State var item1Checked = false
    
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
                        }) {
                            Text("발급하기")
                                .fontWeight(.medium)
                                .foregroundColor(.white)
                                .frame(width: 256, height: 45)
                                .background(Color(red: 218/255, green: 33/255, blue: 39/255))
                                .cornerRadius(6)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 6)
                                        .stroke(Color(red: 218/255, green: 33/255, blue: 39/255), lineWidth: 1)
                                )
                        }
                        .padding()
                    }
                    .padding(.top, 48)
                }
            }
        }
    }
}

#Preview {
    AddCertificateModalView(certification: Certification(name: "Sample", organ: "Sample Organ"))
}
