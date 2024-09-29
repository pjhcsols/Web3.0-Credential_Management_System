//
//  ContentView.swift
//  Wallet
//
//  Created by Seah Kim on 9/29/24.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack {
            HStack{
                Text("web3wallet")
                    .font(/*@START_MENU_TOKEN@*/.title/*@END_MENU_TOKEN@*/)
                    .fontWeight(.light)
                    .padding(.leading, 12)
                Spacer()
                Button(action: {
                }){
                    Image(systemName: "plus")
                        .font(.title)
                        .foregroundColor(.gray)
                }
                Button(action: {
                }){
                    Image(systemName: "gearshape")
                        .font(.title)
                        .foregroundColor(.gray)
                }
            }
            Spacer()
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
                        .font(.title)
                        .fontWeight(.medium)
                        .foregroundColor(.black)
                        .padding(.top, 24)
                    Image("images/knu")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 230, height: 230)
                        .padding(.vertical, 36.0)
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.white)
                .cornerRadius(10)
                .shadow(color: Color.gray.opacity(0.3),
                        radius: 10,
                        x: 0,
                        y: 0)
            }
            .frame(width: UIScreen.main.bounds.width * 0.85,
                   height: (UIScreen.main.bounds.width * 0.85) * (3.0 / 2.0))
            Spacer()
            Spacer()
        }
    
        .padding()
        
    }
}

#Preview {
    ContentView()
}
