import SwiftUI

struct LockByCodeView: View {
    var numberOfDigits: Int = 4

    @State private var code: String = ""
    @State private var confirmCode: String = ""
    @State private var isConfirming: Bool = false
    @State private var showError: Bool = false

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

            Text(showError ? "비밀번호가 일치하지 않습니다.\n다시 입력해주세요." : (isConfirming ? "\n비밀번호를 한 번 더 입력해주세요" : "인증 시 사용할\n4자리 숫자를 입력해주세요"))
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
                        if index < (isConfirming ? confirmCode.count : code.count) {
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
                VStack {
                    VStack(spacing: 8) {
                        ForEach(keypadNumbers, id: \.self) { row in
                            HStack(spacing: 16) {
                                ForEach(row, id: \.self) { number in
                                    Button(action: {
                                        keypadButtonTapped(number)
                                    }) {
                                        if number == "⌫" && (isConfirming ? confirmCode.isEmpty : code.isEmpty) {
                                            Color.clear
                                                .frame(width: (geometry.size.width - 32) / 3, height: 80)
                                        } else {
                                            Text(number)
                                                .font(.title)
                                                .foregroundColor(Color.black)
                                                .frame(width: (geometry.size.width - 32) / 3, height: 80)
                                        }
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
            if isConfirming {
                if !confirmCode.isEmpty {
                    confirmCode.removeLast()
                }
            } else {
                if !code.isEmpty {
                    code.removeLast()
                }
            }
        } else {
            if isConfirming {
                if confirmCode.count < numberOfDigits {
                    confirmCode.append(number)
                    if confirmCode.count == numberOfDigits {
                        validateCodes()
                    }
                }
            } else {
                if code.count < numberOfDigits {
                    code.append(number)
                    if code.count == numberOfDigits {
                        isConfirming = true
                    }
                }
            }
        }
    }

    private func validateCodes() {
        if code == confirmCode {
            // 비밀번호가 일치하는 경우 처리
            print("비밀번호가 일치합니다.")
        } else {
            // 비밀번호가 일치하지 않는 경우
            confirmCode = ""
            showError = true
        }
    }
}

#Preview {
    LockByCodeView()
}
