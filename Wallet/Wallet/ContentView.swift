//
//  ContentView.swift
//  Wallet
//
//  Created by Seah Kim on 9/29/24.
//

import SwiftUI

struct ContentView: View {
    @State private var isExpanded = true
    @State private var showHistory = false
    
    var body: some View {
        VStack {
            HStack {
                Text("web3wallet")
                    .font(.title)
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
            .padding(.top, 12)
            Spacer()
            
            Button(action: {
                            withAnimation(.easeInOut(duration: 0.3)) {
                                isExpanded.toggle()
                                if isExpanded {
                                    showHistory = false
                                } else {
                                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                                        withAnimation(.easeInOut(duration: 0.5)) {
                                            showHistory.toggle()
                                        }
                                    }
                                }
                            }
                        }) {
                VStack {
                    HStack {
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
                        .font(isExpanded ? .title : .title2)
                        .fontWeight(.medium)
                        .foregroundColor(.black)
                        .padding(.top, isExpanded ? 24 : 2)
                    
                    Image("images/knu")
                        .resizable()
                        .scaledToFit()
                        .frame(width: isExpanded ? 230 : 120, height: isExpanded ? 230 : 120)
                        .padding(.top, isExpanded ? 36.0 : 2.0)
                        .padding(.bottom, isExpanded ? 0 : 16.0)
                    
                    Spacer()
                }
                .frame(maxWidth: .infinity, alignment: .top)
                .background(Color.white)
                .cornerRadius(10)
                .shadow(color: Color.gray.opacity(0.3), radius: 10, x: 0, y: 0)
            }
            .frame(width: UIScreen.main.bounds.width * 0.85,
                   height: isExpanded ? (UIScreen.main.bounds.width * 0.85) * (3.0 / 2.0) : (UIScreen.main.bounds.width * 0.85) * (2.0 / 3.0),
                   alignment: .top)
            .padding(.top, isExpanded ? 0 : 20)
            .padding(.bottom, isExpanded ? 0 : 20)
            
            if showHistory {
                HistoryListView()
                    .transition(.opacity)
                    .animation(.easeInOut(duration: 0.1), value: showHistory)
                        }
            Spacer()
        }
        .padding()
        .frame(maxHeight: .infinity, alignment: .top)
    }
}

#Preview {
    ContentView()
}
