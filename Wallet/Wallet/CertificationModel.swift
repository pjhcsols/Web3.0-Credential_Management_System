//
//  CertificationModel.swift
//  Wallet
//
//  Created by Seah Kim on 10/20/24.
//

import Foundation

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

    static let defaultCertifications: [Certification] = [
        Certification(name: "재학증"),
        Certification(name: "여권"),
        Certification(name: "자격증"),
        Certification(name: "주민등록증"),
        Certification(name: "운전면허증"),
    ]
}
