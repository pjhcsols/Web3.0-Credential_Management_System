//
//  CreateWalletView.swift
//  Wallet
//
//  Created by Seah Kim on 10/7/24.
//

import SwiftUI

struct CreateWalletView: View {
    
    var body: some View {
        VStack {
            Spacer()
            Text("지갑 생성을 시작해볼까요?")
                .font(.title2)
                .fontWeight(.semibold)
            Spacer()
            NavigationLink(destination: ContentView().navigationBarBackButtonHidden(true)) {
                Text("다음")
                    .foregroundColor(.white)
                    .frame(width: 256, height: 45)
                    .background(Color(red: 218/255, green: 33/255, blue: 39/255))
                    .cornerRadius(6)
                    .overlay(
                        RoundedRectangle(cornerRadius: 6)
                            .stroke(Color(red: 218/255, green: 33/255, blue: 39/255), lineWidth: 1)
                    )
            }
        
        }
        .padding()
        Spacer()
    }
}

#Preview {
    CreateWalletView()
}
