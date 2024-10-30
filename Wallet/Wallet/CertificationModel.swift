//
//  CertificationModel.swift
//  Wallet
//
//  Created by Seah Kim on 10/20/24.
//

import Foundation

//struct Certification: Identifiable {
//    let id = UUID()
//    let name: String
//}
//

struct Certification: Identifiable, Codable, Hashable {
    let id: UUID
    let name: String
    
    init(name: String) {
        self.id = UUID()
        self.name = name
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(name)
    }

    static func == (lhs: Certification, rhs: Certification) -> Bool {
        return lhs.name == rhs.name
    }
}

    // 기본 초기화
//    init(name: String) {
//        self.id = UUID()
//        self.name = name
//    }
//    
//    // JSON으로부터 객체를 초기화하는 생성자
//    init(id: UUID, name: String) {
//        self.id = id
//        self.name = name
//    }

