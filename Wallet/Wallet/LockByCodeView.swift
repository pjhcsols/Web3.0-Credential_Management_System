//
//  LockByCodeView.swift
//  Wallet
//
//  Created by Seah Kim on 10/7/24.
//

import SwiftUI

struct LockByCodeView: View {
    var numberOfDigits: Int = 4
    
    @State private var code: String = ""
    @FocusState private var isKeyboardFocused: Bool

    let keypadNumbers: [[String]] = [
        ["1", "2", "3"],
        ["4", "5", "6"],
        ["7", "8", "9"],
        ["", "0", "⌫"]
    ]
    
    var body: some View {
        VStack {
            Spacer()
                .frame(height: 128)

            Text("인증 시 사용할\n4자리 숫자를 입력해주세요")
                .font(.title3)
                .fontWeight(.semibold)
                .multilineTextAlignment(.center)
    
            HStack(spacing: 16) {
                ForEach(0..<numberOfDigits, id: \.self) { index in
                    ZStack {
                        Rectangle()
                            .fill(Color.white)
                            .frame(width: 40, height: 50)
                        
            
                        if index < code.count {
                            Circle()
                                .fill(Color(red: 218/255, green: 33/255, blue: 39/255))
                                .frame(width: 16, height: 16)
                        }
                    }
                }
            }
            .padding()

            GeometryReader { geometry in
                VStack {
                    VStack(spacing: 8) {
                        ForEach(keypadNumbers, id: \.self) { row in
                            HStack(spacing: 16) {
                                ForEach(row, id: \.self) { number in
                                    Button(action: {
                                        keypadButtonTapped(number)
                                    }) {
                                        Text(number)
                                            .font(.title)
                                            .foregroundColor(Color.black)
                                            .frame(width: (geometry.size.width - 32) / 3, height: 80)
                                    }
                                    .disabled(number.isEmpty)
                                }
                            }
                        }
                    }
                    Spacer()
                }
            }
            .padding(.horizontal, 40)
        }
    }

    private func keypadButtonTapped(_ number: String) {
        if number == "⌫" {
            if !code.isEmpty {
                code.removeLast()
            }
        } else {
            if code.count < numberOfDigits {
                code.append(number)
            }
        }
    }
}

#Preview {
    LockByCodeView()
}
