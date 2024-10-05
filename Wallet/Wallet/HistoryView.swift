//
//  HistoryView.swift
//  Wallet
//
//  Created by Seah Kim on 9/30/24.
//

import SwiftUI

struct HistoryView: View {
    var body: some View {
        VStack{
            HStack{
                Text("web3wallet")
                    .font(/*@START_MENU_TOKEN@*/.title/*@END_MENU_TOKEN@*/)
                    .fontWeight(.light)
                    .padding(.leading, 12)
                Spacer()
                ShareLink(item: URL(string: "https://github.com/pjhcsols/Web3.0-Credential_Management_System")!) {
                    Label("", systemImage: "square.and.arrow.up")
                        .font(.title)
                        .foregroundColor(.gray)
                }
            }
            Button(action: {
            }) {
                VStack() {
                    HStack{
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
                    Text("홍길동")
                        .font(.title2)
                        .fontWeight(.medium)
                        .foregroundColor(.black)
                        .padding(.top, 4)
                    Image("images/knu")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 100, height: 100)
                        
                        .padding(.bottom, 24.0)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.white)
                .cornerRadius(10)
                .shadow(color: Color.gray.opacity(0.2),
                        radius: 10,
                        x: 0,
                        y: 0)
            }
            .frame(width: UIScreen.main.bounds.width * 0.85,
                   height: (UIScreen.main.bounds.width * 0.85) * (2.0 / 3.0))
            Spacer()
            Spacer()
            HistoryListView()
        }
        .padding()
    }
}

#Preview {
    HistoryView()
}
