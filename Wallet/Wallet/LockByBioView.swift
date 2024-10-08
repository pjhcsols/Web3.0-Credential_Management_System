//
//  LockByBioView.swift
//  Wallet
//
//  Created by Seah Kim on 10/8/24.
//

import SwiftUI

struct LockByBioView: View {
    var body: some View {
        VStack{
            Spacer()
                .frame(height: 256)
            Text("생체인증 등록")
                .font(.title3)
                .fontWeight(.semibold)
            Spacer()
                .frame(height: 32)
            HStack{
                Image("images/face-id")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 80)
                Spacer()
                    .frame(width: 32)
                Image("images/touch-id")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 80)
            }
            Spacer()
            HStack{
                Button(action: {
                }) {
                    Text("건너뛰기")
                        .foregroundColor(.black)
                        .frame(width: 156, height: 45)
                        .background(Color.white)
                        .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .stroke(Color.gray, lineWidth: 1)
                            )
                }
                
                Button(action: {
                }) {
                    Text("등록하기")
                        .foregroundColor(.white)
                        .frame(width: 156, height: 45)
                        .background(Color(red: 218/255, green: 33/255, blue: 39/255))
                        .cornerRadius(6)
                        .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                .stroke(Color(red: 218/255, green: 33/255, blue: 39/255), lineWidth: 1)
                        )
                }
            }
            
        }
        .padding()
        Spacer()
    }
}

#Preview {
    LockByBioView()
}
