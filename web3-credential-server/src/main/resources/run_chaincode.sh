#!/bin/bash

# 도커 컨테이너에서 환경 변수 가져오기
CORE_PEER_ADDRESS=$(docker exec peer0.org1.example.com printenv CORE_PEER_ADDRESS)
CORE_PEER_LOCALMSPID=$(docker exec peer0.org1.example.com printenv CORE_PEER_LOCALMSPID)
ORDERER_ADDRESS=$(docker exec orderer.example.com printenv ORDERER_ADDRESS)

echo "1"
# 개인 키 파일 존재 여부 확인 (도커 내부에서 확인)
if ! docker exec peer0.org1.example.com [ -f /etc/hyperledger/fabric/msp/keystore/priv_sk ]; then
    echo "Error: Private key file does not exist in the container."
    exit 1
fi

echo "2"
# 체인코드 디렉토리 존재 여부 확인 및 생성
if ! docker exec peer0.org1.example.com [ -d /opt/gopath/src/chaincode ]; then
    echo "Chaincode directory does not exist. Creating it..."
    docker exec peer0.org1.example.com mkdir -p /opt/gopath/src/chaincode
fi

echo "3"
# 체인코드 파일 도커 컨테이너로 복사
echo "Copying chaincode file to the container..."
docker cp /Users/hansol/Desktop/공개SW/2차/BE/Web3.0-Credential_Management_System/web3-credential-server/src/main/resources/chaincode/certification_chaincode.go peer0.org1.example.com:/opt/gopath/src/chaincode

echo "4"
# 체인코드가 정상적으로 복사되었는지 확인
if ! docker exec peer0.org1.example.com [ -f /opt/gopath/src/chaincode/certification_chaincode.go ]; then
    echo "Error: Chaincode file does not exist in the container."
    exit 1
fi

echo "5"
# 패키지 관리자가 있는지 확인 후 설치
if ! docker exec peer0.org1.example.com command -v apt > /dev/null; then
    echo "apt package manager not found. Installing apt..."
    docker exec peer0.org1.example.com /bin/bash -c "apt-get update && apt-get install -y apt"
fi

# wget과 tar 설치
if ! docker exec peer0.org1.example.com command -v wget > /dev/null; then
    echo "wget not found. Installing wget..."
    docker exec peer0.org1.example.com /bin/bash -c "apt-get update && apt-get install -y wget tar"
fi

# Go 설치 확인 및 설치
echo "6"
# 중복된 Go tar.gz 파일 삭제
#docker exec peer0.org1.example.com /bin/bash -c "rm -f /root/go1.20.3.linux-arm64.tar.gz /root/go.tar.gz"


# Go 설치 확인 및 설치
if ! docker exec peer0.org1.example.com /bin/bash -c "export PATH=\$PATH:/usr/local/go/bin && go version" > /dev/null; then
    echo "Go is not installed. Installing Go..."
    docker exec peer0.org1.example.com /bin/bash -c "wget https://go.dev/dl/go1.20.3.linux-arm64.tar.gz -O /root/go.tar.gz"
    docker exec peer0.org1.example.com /bin/bash -c "tar -C /usr/local -xzf /root/go.tar.gz"
    docker exec peer0.org1.example.com /bin/bash -c "echo 'export PATH=$PATH:/usr/local/go/bin' >> /root/.bashrc"
    docker exec peer0.org1.example.com /bin/bash -c "source /root/.bashrc"

    echo "Go installation completed."
else
    echo "Go is already installed."
fi

# Go 버전 확인
docker exec peer0.org1.example.com /bin/bash -c "export PATH=\$PATH:/usr/local/go/bin && go version"

echo "7"
# 체인코드 ID 설정
export CORE_CHAINCODE_ID_NAME=certification_chaincode:1.0

echo "8"
# 체인코드 실행 환경 설정 및 실행
echo "컨테이너 내에서 CORE_CHAINCODE_ID_NAME 설정 및 체인코드 실행 중..."
docker exec -it peer0.org1.example.com /bin/bash -c "export CORE_CHAINCODE_ID_NAME=certification_chaincode:1.0 && export PATH=\$PATH:/usr/local/go/bin && cd /opt/gopath/src/chaincode && go run certification_chaincode.go --peer.address='peer0.org1.example.com:7051'"

echo "체인코드 실행 완료"
