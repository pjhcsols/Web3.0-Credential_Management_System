//
//  HistoryListView.swift
//  Wallet
//
//  Created by Seah Kim on 10/5/24.
//

import SwiftUI

struct HistoryListView: View {
    let historyArray = histories
    var body: some View {
        List(historyArray){ item in
            VStack(alignment: .leading){
                HStack{
                    Text(item.name)
                    Text(item.types)
                }
                HStack{
                    Text(item.organ)
                        .font(/*@START_MENU_TOKEN@*/.caption/*@END_MENU_TOKEN@*/)
                    Text(item.time)
                        .font(/*@START_MENU_TOKEN@*/.caption/*@END_MENU_TOKEN@*/)
                }
            }
        }
        .scrollContentBackground(.hidden)
    }
}

#Preview {
    HistoryListView()
}

let histories: [History] = [
    History(name: "정부 24 간편인증", types: "(생체인증)", organ: "발급처: 정부 24", time: "사용일시: 2024.08.17 14:36"),
    History(name: "정부 24 간편인증", types: "(생체인증)", organ: "발급처: 정부 24", time: "사용일시: 2024.09.17 15:36"),
    History(name: "정부 24 간편인증", types: "(생체인증)", organ: "발급처: 정부 24", time: "사용일시: 2024.10.17 14:36"),
]

struct History: Identifiable {
    let id = UUID()
    let name: String
    let types: String
    let organ: String
    let time: String
}
