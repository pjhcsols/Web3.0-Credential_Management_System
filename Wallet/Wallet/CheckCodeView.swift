//
//  CheckCodeView.swift
//  Wallet
//
//  Created by Seah Kim on 10/10/24.
//

import SwiftUI

struct CheckByCodeView: View {
    var numberOfDigits: Int = 4
    var onSuccess: (Bool) -> Void

    @State private var inputCode: String = ""
    @State private var showError: Bool = false

    let keypadNumbers: [[String]] = [
        ["1", "2", "3"],
        ["4", "5", "6"],
        ["7", "8", "9"],
        ["", "0", "⌫"]
    ]

    var body: some View {
        NavigationStack {
            VStack {
                Spacer()
                    .frame(height: 128)

                Text(showError ? "비밀번호가 일치하지 않습니다.\n다시 입력해주세요." : "4자리 비밀번호를 입력해주세요.")
                    .font(.title3)
                    .fontWeight(.semibold)
                    .multilineTextAlignment(.center)
                    .foregroundColor(showError ? .red : .primary)

                HStack(spacing: 16) {
                    ForEach(0..<numberOfDigits, id: \.self) { index in
                        ZStack {
                            Rectangle()
                                .fill(Color.white)
                                .frame(width: 40, height: 50)
                            if index < inputCode.count {
                                Circle()
                                    .fill(Color.black)
                                    .frame(width: 16, height: 16)
                            } else {
                                Circle()
                                    .stroke(Color.black, lineWidth: 2)
                                    .frame(width: 16, height: 16)
                            }
                        }
                    }
                }
                .padding()

                GeometryReader { geometry in
                    let buttonWidth = max((geometry.size.width - 32) / 3, 0)
                    VStack {
                        VStack(spacing: 8) {
                            ForEach(keypadNumbers, id: \.self) { row in
                                HStack(spacing: 16) {
                                    ForEach(row, id: \.self) { number in
                                        Button(action: {
                                            keypadButtonTapped(number)
                                        }) {
                                            if number == "⌫" && inputCode.isEmpty {
                                                Color.clear
                                                    .frame(width: buttonWidth, height: 80)
                                            } else {
                                                Text(number)
                                                    .font(.title)
                                                    .foregroundColor(Color.black)
                                                    .frame(width: buttonWidth, height: 80)
                                            }
                                        }
                                        .disabled(number.isEmpty)
                                    }
                                }
                            }
                        }
                        Spacer()
                    }
                    .frame(height: 300)
                }
                .padding(.horizontal, 40)
            }
        }
    }

    private func keypadButtonTapped(_ number: String) {
        if number == "⌫" {
            if !inputCode.isEmpty {
                inputCode.removeLast()
            }
        } else {
            if inputCode.count < numberOfDigits {
                inputCode.append(number)
                if inputCode.count == numberOfDigits {
                    validateCode()
                }
            }
        }
    }

    private func validateCode() {
        let savedPinCode = UserDefaults.standard.string(forKey: "userPinCode") ?? ""

        if inputCode == savedPinCode {
            onSuccess(true)  // 성공 콜백
        } else {
            showError = true
            inputCode = ""
        }
    }
}

#Preview {
    CheckByCodeView(onSuccess: { success in
        if success {
            print("PIN validation successful!")
        }
    })
}
