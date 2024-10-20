package main

import (
	"encoding/json"
	"fmt"
	// "io/ioutil" // 개인 키 파일 읽기에 사용
	"github.com/hyperledger/fabric-contract-api-go/contractapi"
	"os"
)

type CertificationContract struct {
	contractapi.Contract
	PrivateKey string // 개인 키 저장
}

type StudentCertification struct {
	Email     string `json:"email"`
	UnivName  string `json:"univName"`
	UnivCheck bool   `json:"univ_check"`
}

// 개인 키 파일을 읽는 함수
func readPrivateKey(path string) (string, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return "", fmt.Errorf("failed to read private key file at %s: %v", path, err) // Include the path in the error
	}
	return string(data), nil
}

// 인증 정보를 저장하는 함수
func (c *CertificationContract) RegisterCertification(ctx contractapi.TransactionContextInterface, key string, email string, univName string, univCheck bool) error {
	// 인증 정보 저장 로직
	cert := StudentCertification{
		Email:     email,
		UnivName:  univName,
		UnivCheck: univCheck,
	}
	certBytes, err := json.Marshal(cert)
	if err != nil {
		return fmt.Errorf("error marshaling data: %v", err)
	}

	err = ctx.GetStub().PutState(key, certBytes)
	if err != nil {
		return fmt.Errorf("error putting state: %v", err)
	}

	return nil
}

// 인증 정보를 조회하는 함수
func (c *CertificationContract) QueryCertification(ctx contractapi.TransactionContextInterface, key string) (*StudentCertification, error) {
	// 인증 정보 조회 로직
	certBytes, err := ctx.GetStub().GetState(key)
	if err != nil {
		return nil, fmt.Errorf("error getting state: %v", err)
	}

	if certBytes == nil {
		return nil, fmt.Errorf("no certification found with key %s", key)
	}

	var cert StudentCertification
	err = json.Unmarshal(certBytes, &cert)
	if err != nil {
		return nil, fmt.Errorf("error unmarshaling data: %v", err)
	}

	return &cert, nil
}

func main() {
	// 개인 키 파일 경로 설정
	privateKeyPath := "/etc/hyperledger/fabric/msp/keystore/priv_sk"
	// 개인 키 파일 읽기
	privateKey, err := readPrivateKey(privateKeyPath)
	if err != nil {
		fmt.Println(err)
		return
	}

	// 체인코드 생성 및 개인 키 설정
	chaincode, err := contractapi.NewChaincode(&CertificationContract{
		PrivateKey: privateKey, // 개인 키를 체인코드에 설정
	})
	if err != nil {
		fmt.Printf("Error creating certification chaincode: %v\n", err)
		return
	}

	fmt.Printf("체인코드 시작 \n")
	// 체인코드 시작
	err = chaincode.Start()
	if err != nil {
		fmt.Printf("Error starting certification chaincode: %v\n", err)
	}
}
