//
//  GenerateKeyPair.swift
//  Wallet
//
//  Created by Seah Kim on 10/9/24.
//

import Security
import Foundation

func generateKeyPair() -> (privateKey: String?, publicKey: String?) {
    let attributes: [String: Any] = [
        kSecAttrKeyType as String: kSecAttrKeyTypeEC,
        kSecAttrKeySizeInBits as String: 256
    ]
    
    var error: Unmanaged<CFError>?
    
    guard let privateKey = SecKeyCreateRandomKey(attributes as CFDictionary, &error) else {
        print("Error creating private key: \(error!.takeRetainedValue())")
        return (nil, nil)
    }
    
    guard let publicKey = SecKeyCopyPublicKey(privateKey) else {
        print("Error creating public key.")
        return (nil, nil)
    }
    
    let privateKeyData = SecKeyCopyExternalRepresentation(privateKey, &error) as Data?
    let publicKeyData = SecKeyCopyExternalRepresentation(publicKey, &error) as Data?
    
    let privateKeyBase64 = privateKeyData?.base64EncodedString()
    let publicKeyBase64 = publicKeyData?.base64EncodedString()
    
    return (privateKeyBase64, publicKeyBase64)
}
