# :closed_lock_with_key: [경북멋쟁이] Web 3.0 신원/자격증명 관리 시스템 
<img width="880" alt="theme" src="https://github.com/user-attachments/assets/4e92c1b0-97d9-45a2-811c-b2ae2ec90f49">
<br>
<br>

## 주제
**개인이 데이터를 직접 소유하고 관리하는 Web3 전자지갑 시스템 : “Web 3.0 신원/자격증명 관리 시스템"**
<br>
<br>

이 시스템은 Web3 구조와 외부 인증 API를 통합하여 사용자의 지갑 생성 및 인증서 관리 기능을 강화하고, 추가적인 보안 및 신원 확인을 제공합니다.  
전체 구조는 사용자 인터페이스에서부터 블록체인 및 서버 측까지 각 단계별로 체계적으로 설계되어 있습니다.
<br>
<br>

### 🔗 Youtube 시연영상
[[https://www.youtube.com/Web3.0-Credential_Management_System](https://youtu.be/bgmQniGsJ1U)]
<br>
<br>
<br>
<br>

## 시스템 아키텍처
<img width="880" alt="arch" src="https://github.com/user-attachments/assets/d7139346-dbc6-466c-8955-4bdd134c21f1">
<br>
<br>
<br>
<br>

## 주요 화면
### 온보딩 화면 및 로그인 화면
* 카카오 로그인 API를 사용하여 로그인을 진행합니다.
* 로그인이 완료되면 4자리 PIN 코드를 설정하고 생체 인증 여부를 확인하게 됩니다.
<img width="880" alt="onboarding" src="https://github.com/user-attachments/assets/ab6b83b2-2013-47fd-919d-dc7191fc2c30">
<br>
<br>

### 메인 화면
* 로그인한 사용자의 신원 정보가 표시되는 화면입니다. 
* 메인 화면의 오른쪽 상단에 위치한 '톱니바퀴' 버튼을 클릭하면, 설정 화면으로 들어갑니다.
* 설정 화면에서 사용자가 발급한 인증서 정보를 볼 수 있고 인증서 전체 삭제가 가능합니다.
<img width="440" alt="main" src="https://github.com/user-attachments/assets/e8477a14-ac9f-4870-95f9-305517d4e9f8">
<br>
<br>

### 전자 증명서 발급 화면
* 메인 화면의 오른쪽 상단에 위치한 ‘+’ 버튼을 클릭하면, 증명서를 발급할 수 있습니다.
* 사용자는 발급을 원하는 증명서를 선택한 후, 동의 체크박스를 클릭하고 간단한 인증을 거치면 지갑에 PDF 형태로 인증서가 들어갑니다.
<img width="880" alt="add" src="https://github.com/user-attachments/assets/1c2b4a84-6e1e-4ed0-9a87-67a79aa6aaa6">
<br>
<br>

### 인증서 보기 화면
* 메인화면의 카드를 클릭 시, 사용자가 발급한 인증서 목록이 나타납니다.
* 각 목록을 클릭 시, 해당하는 인증서를 볼 수 있습니다.
* 각 목록을 옆으로 슬라이드 시, 해당하는 인증서를 삭제할 수 있습니다.
<img width="720" alt="add" src="https://github.com/user-attachments/assets/10406a08-9e2e-41ec-9c63-0ee6f5bfccf7">

<br>
<br>

## 주요 기능

### OAuth 2.0 소셜 로그인

![image](https://github.com/user-attachments/assets/5994c56f-0d6c-4caa-94f6-9e9bad855472)

* 이 프로젝트에서는 OAuth 2.0 프로토콜을 기반으로 소셜 로그인 기능을 구현하였습니다.
* 카카오 API를 활용하여 서버에서 카카오 엑세스 토큰을 발급 받아 사용자의 소셜 계정 정보를 안전하게 가져오고,  
이를 통해 시스템에 손쉽게 회원가입과 로그인을 수행할 수 있습니다.

사용자는 기존에 사용하던 카카오 계정 정보를 그대로 사용하여 별도의 계정 생성 과정 없이 시스템에 접근할 수 있습니다. 이 과정에서 OAuth 2.0의 보안 메커니즘을 통해 사용자의 인증 정보가 안전하게 관리되며, 서버 측에서는 카카오로부터 전달받은 액세스 토큰을 활용하여 사용자의 프로필 정보 및 기본적인 계정 정보를 획득합니다.

이후 해당 정보를 바탕으로 사용자의 회원가입 절차를 자동화하거나, 기존 회원이라면 바로 로그인 처리하여 사용자의 편의성을 극대화하였습니다. 또한, 이 과정에서 사용자 동의 절차를 거쳐 필요한 정보만을 수집하며, 개인정보 보호와 관련된 법적 요구 사항을 철저히 준수합니다.

<br>

### 2차 인증: 생체 인증 등 전자 지갑 생성을 위한 2차 인증 절차를 포함

![image](https://github.com/user-attachments/assets/611bc384-68b3-4350-b2b9-8b959bc05d67)

* Android 기기에서는 BiometricPrompt API를, iOS 기기에서는 LocalAuthentication Framework를 사용하여 지문, 얼굴 인식과 같은 생체 정보를 이용한 인증을 수행합니다.
* 디바이스 상의 문제로 인하여 생체인증이 불가능한 사용자를 위해 PIN 코드로 2차 인증을 할 수 있는 방안도 마련해 두었습니다.


기본적인 로그인 절차 외에도 추가적인 보안 강화를 위해 2차 인증 기능을 포함시켰습니다. 2차 인증 절차는 전자 지갑 생성을 위한 필수 단계로, 사용자의 신원을 한층 더 강력하게 보호합니다. 특히, 생체 인증을 활용한 2차 인증 절차를 통해 사용자는 보다 안전하면서도 간편하게 시스템에 접근할 수 있습니다.  

사용자는 2차 인증이 성공적으로 완료된 후에만 Web3 기반의 전자 지갑을 생성할 수 있으며, 이 지갑은 블록체인 상에서의 다양한 거래와 상호작용을 가능하게 합니다. 이를 통해 시스템은 사용자의 자산과 개인 정보를 더욱 철저히 보호하며, 높은 수준의 보안 요구 사항을 충족시킵니다.

2차 인증 절차는 사용자의 편의성을 해치지 않으면서도 보안을 강화하는 중요한 역할을 하며, 특히 중요한 데이터에 접근할 때 필수적인 추가 보안 계층을 제공합니다.



<br>

## Web3 지갑 생성 과정

* 지갑이 생성될 때 블록과 S3 스토리지의 PDF가 생성되며 블록과 PDF의 URL이 사용자 지갑에 저장됩니다.  
* S3 스토리지에 실물 인증서와 블록에 신원인증 정보를 포함하여 개인의 디바이스에서 증명이 가능합니다.

![image](https://github.com/user-attachments/assets/3bcd06e9-3ee7-4bea-9444-caa2806a9155)

1. 클라이언트가 사용자가 엑세스 토큰을 통해 Web3 지갑 생성을 요청합니다.
2. 지갑에서 인증서 PDF와 메타데이터 Verifiable Credential (Credential Metadata, Claims, Proofs)를 S3 스토리지에 저장하고 개인 디바이스에 다운로드할 수 있습니다. 이때 데이터베이스에는 해당 사용자의 S3 PDF 주소값을 저장합니다.
3. 지갑에서 개인 디바이스의 블록(PDF)이 생성되며 블록(PDF)에는 추후 업로드하는 해당 증명에 관한 key : value 값을 저장하고 블록(PDF)이 생성 및 추가되며 이를 통해 외부 인증과의 연동이 수행됩니다.
4. 지갑에서 해당되는 인증서를 업로드 가능하며 PDF의 페이지 별 메타데이터를 별도로 관리하며 인증과정을 수행하는 메타데이터는 key : value 값으로 Verifiable Credential로 관리합니다.

<br>

## 인증서 발급 과정

### 인증서 등록

![image](https://github.com/user-attachments/assets/96e2e235-ea12-4360-8449-e30555901ae8)

1. 클라이언트는 인증서 등록을 위해 2차 인증을 수행합니다.
2. Web3 환경의 PDF를 통해 인증서에 대한 Verifiable Credential 생성 및 신분증명 요청을 처리합니다.
3. 외부 인증 API를 활용해 인증서 Verifiable Credential에 대한 신분 증명을 요청합니다.
4. 서버는 등록된 인증서의 블록(PDF) URL을 저장하고 관리합니다.

<br>

### 인증서 접근

![image](https://github.com/user-attachments/assets/4f154c47-4ab3-4a45-b07a-50ed825e7bfb)

1. 클라이언트는 등록된 인증서에 접근하기 위해 2차 인증을 수행합니다.
2. Web3 환경의 PDF를 통해 인증서에 대한 인증을 요청하고 증명서 URL을 반환합니다.
3. 외부 인증 API를 활용해 인증서 Verifiable Credential에 대한 신분 증명을 요청하고 확인합니다.
4. 서버는 인증이 완료된 인증서에 접근할 수 있도록 지원합니다.

<br>

### 인증서 목록 보기

![image](https://github.com/user-attachments/assets/ca75af9d-9d04-456d-99c1-c713910ba3cb)

1. 클라이언트는 등록된 인증서에 접근하기 위해 2차 인증을 수행합니다.
2. Web3 환경의 PDF를 통해 인증서에서 Verifiable Credential을 찾아오고, 인증서 URL을 반환합니다.
3. 서버로 인증서 URL을 요청합니다.
4. URL을 통해 S3 스토리지에 있는 인증서에 접근해 해당 객체의 메타데이터에 접근합니다.
5. 메타데이터 가공 후 클라이언트에게 목록 반환합니다.

<br>
<br>

## 블록체인의 블록과 S3 메타데이터를 통한 인증 수행을 위한 Verifiable Credential 관리

![image](https://github.com/user-attachments/assets/610a4423-7311-4690-8f87-7c0ba3c7808f)

1. S3 스토리지에 실물 인증서와 메타데이터에 외부 인증 Verifiable Credential을 저장하고 사용자가 어플리케이션을 재다운로드하면 해당 S3 스토리지의 인증서와 Verifiable Credential을 통해 블록을 생성하고 사용자의 디바이스에 저장합니다.

![image](https://github.com/user-attachments/assets/a342c8d9-f8b7-4296-81c3-5126884a8fd5)
<br>
![image](https://github.com/user-attachments/assets/34af8fde-ea4f-4d23-beae-1c888972aced)

2. Web3 블록체인의 블록(PDF)에 외부 인증 값 및 정보 저장을 처리합니다. 개인 디바이스의 블록에 외부 인증을 수행하는 Verifiable Credential, Credential Metadata, Claims, Proofs를 포함한 블록을 생성 관리합니다.
3. 외부 인증 API를 활용해 전자지갑의 인증서 사용 시, 외부 인증과의 연동을 수행합니다.

<br>
<br>


# 블록체인 & PDF 메타데이터 인증서 관리 시스템 (Certificate Management System)

이 프로젝트는 사용자가 데이터를 직접 관리하는 암호화 기반의 인증서 관리 시스템으로, OAuth2.0 및 Web3 환경을 통합하여 사용자 지갑과 인증서를 안전하게 관리합니다. 사용자는 자신만의 지갑에서 인증서 발급 및 관리가 가능하며, 블록체인과 PDF 메타데이터를 통해 인증서의 진본성을 확인할 수 있습니다.

## 주요 기능 설명
<img width="880" alt="features13" src="https://github.com/user-attachments/assets/375d83d2-91bb-4d3c-aa6c-8bbe68116a90">
<img width="880" alt="features14" src="https://github.com/user-attachments/assets/e2830c2f-d9c5-4d27-b02c-426d2b3eceb4">
<img width="880" alt="features15" src="https://github.com/user-attachments/assets/dd6850a6-c6a4-4f2d-b3e4-d7c6997a5c47">
<img width="880" alt="features16" src="https://github.com/user-attachments/assets/3293f19b-fa49-42fa-9816-7ce0dbceed75">
<img width="880" alt="features17" src="https://github.com/user-attachments/assets/2f052612-ecb3-498b-8fa4-bbf537d05615">
<img width="880" alt="features18" src="https://github.com/user-attachments/assets/fd28833c-693e-464a-92b9-6f9c4291a5ba">
<img width="880" alt="features19" src="https://github.com/user-attachments/assets/cb8ce313-4207-4978-91b3-a4229c81fbb6">
<img width="880" alt="features20" src="https://github.com/user-attachments/assets/9fb56b34-9e0e-48db-af76-0057b87ea4de">
<img width="880" alt="features21" src="https://github.com/user-attachments/assets/157e64ad-9704-468a-b1cb-6c9ef017e03b">
<img width="880" alt="features22" src="https://github.com/user-attachments/assets/251cd465-d5fa-43aa-b728-9a830d31d854">
<img width="880" alt="features23" src="https://github.com/user-attachments/assets/3a0c9af1-1a6e-4b34-8abe-01c6da32c6fc">
<img width="880" alt="features24" src="https://github.com/user-attachments/assets/38e3527e-b2e6-4284-8911-c94b91c31661">
<img width="880" alt="features25" src="https://github.com/user-attachments/assets/bca3aa99-d157-4e06-9fa3-cd6348d80c0c">
<img width="880" alt="features26" src="https://github.com/user-attachments/assets/100859c5-efa2-4f12-83ad-ea6b7c5e7236">
<img width="880" alt="features27" src="https://github.com/user-attachments/assets/b3c6fe49-cd3b-4e8f-bc3a-4e7641db4610">
<img width="880" alt="features28" src="https://github.com/user-attachments/assets/37187c3c-d843-446c-a353-8804a6803ffd">
<img width="880" alt="features29" src="https://github.com/user-attachments/assets/578f2f77-efe1-4911-a4cb-039d7a0ab5e4">
<img width="880" alt="features30" src="https://github.com/user-attachments/assets/668e81f6-9f44-46e7-9a0e-c8aa695072f2">
<img width="880" alt="features31" src="https://github.com/user-attachments/assets/64cac3e2-95a1-4271-aa84-82e777c3d961">
<img width="880" alt="features32" src="https://github.com/user-attachments/assets/f9eda760-b4ab-47dd-812a-8a93ca3de6ab">
---

## 데이터베이스 구조

### Wallet 테이블 구조 (SQL)

```sql
CREATE TABLE wallets (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   user_id BIGINT NOT NULL,
   private_key VARCHAR(255) NOT NULL,
   public_key VARCHAR(255),
   FOREIGN KEY (user_id) REFERENCES users(id)
);
```

- **user_id**: 사용자와 지갑을 연결하는 참조 키.
- **private_key**: 메타데이터 디코딩용 개인 키, RSA 디코딩 시 사용.
- **public_key**: 메타데이터 암호화용 공개 키, RSA 암호화 시 사용.
- **pdfHash**: PDF 파일의 해시값을 저장하여 인증서 진본성 확인.


이 인증서 관리 시스템은 Web2와 Web3의 장점을 결합하여 사용자 지갑과 인증서를 안전하게 관리하며, 사용자가 요청 시에 직접 인증서 진위를 확인하고 필요한 정보를 안전하게 관리할 수 있는 신뢰성 높은 인증 시스템을 제공합니다.


## 외부 API를 이용한 인증 (대학 재학 인증, 자격증, 주민등록증, 여권, 운전면허)

사용자 및 인증서의 신뢰성을 위해 외부 API를 연동하여 대학 재학을 인증하고,  
Qnet 자격증 확인서 및 주민등록 진위 여부 인증을 테스트하는 작업을 수행하였습니다.


### 1. 재학 인증 테스트

사용자가 특정 대학에 재학 중인지를 확인하기 위해 외부 API를 통해 사용자의 학적 정보를 확인할 수 있는 기능을 통합하였습니다. API 요청을 통해 사용자의 재학 상태를 검증하고, 인증기관 서버는 이 정보를 바탕으로 사용자가 제공한 정보의 진위를 확인합니다. 성공적인 재학 인증 요청에 대해, 시스템은 사용자가 제공한 정보와 외부 API의 응답이 일치하는지를 확인하고, 재학 상태가 검증된 사용자로 표시합니다.

### API 개요

- **API 제공처**: UnivCert https://univcert.com/
- **요청 헤더**:
    - `Content-Type`: `application/json`
- **필요 데이터**: API키, 사용자 이름, 대학교 이메일, 대학교명, 인증번호

### 테스트 절차

1. **이용자 메일 인증 시작 (인증코드 발송)**
    - **요청 URL**: https://univcert.com/api/v1/certify

### Request 예시

```json
{
  “key” : “{부여받은 API KEY}”,
  "email” : “{대학교 이메일}”,
  “univName” : “{대학교명}”,
  “univ_check” : true
}
```

("univ_check"가 true라면 해당 대학 재학 여부, false라면 메일 소유자 여부를 판단해 줍니다.)

### Response 예시

```json
//메일로 인증번호 전송이 성공한 경우
{
  “success” : true
}
//실패한 경우
{
  "status" : 400,
  "success" : false,
  "message" : "{에러 메시지}"
}
```

2. **이용자 메일에 발송된 인증코드를 전달받아 요청하기**
    - **요청 URL**: https://univcert.com/api/v1/certifycode

### Request 예시

```json
{
  “key” : “{부여받은 API KEY}”
  “univName” : “{대학교명}”,
  “email” : "{대학교 이메일}”,
  “code” : {대학 이메일로 받은 인증코드}
}
```

### Response 예시

```json
//정확한 인증번호를 입력했을 때
{
  “success” : true
  “univName” : “OO대학교”,
  “certified_email” : “abc@knu.ac.kr”,
  “certified_date” : “2023-01-03T09:30:22”
}
//실패한 경우
{
  “status” : 400,
  "success" : false,
  "message" : "{에러 메시지}"
}
```

3. **인증된 이메일인지 확인**
    - **요청 URL**: https://univcert.com/api/v1/status

### Request 예시

```json
{
  “key” : “{부여받은 API KEY}”,
  "email” : “{인증하고자 하는 이메일}”
}
```

### Response 예시

```json
//성공한 경우
{
  “success” : true,
  “certified_date” : “2023-01-03T09:30:22(인증받은 시간)”
}
//실패한 경우
{
  “success” : false,
  “message” : "{에러 메시지}"
}
```

### 2. Qnet 자격증 확인서 진위확인 테스트

Codef API를 통해 자격증 확인서를 검증하는 절차를 테스트하였습니다.

### API 개요

- **API 제공처**: Codef
- **API Endpoint**: https://development.codef.io/v1/kr/etc/hr/qnet-certificate/status
- **인증 방식**: OAuth 2.0 (클라이언트 자격 증명 방식)
- **요청 데이터**: 사용자 정보와 자격증 확인서 데이터

API 요청을 통해 사용자가 소지한 자격증 확인서의 유효성을 확인하고, Qnet에서 반환한 응답 데이터를 바탕으로 사용자가 주장하는 자격증 소지가 올바른지를 검증하였습니다.

API 응답의 검증을 통해 유효한 자격증을 가진 사용자로 인증되었을 경우, 인증기관 서버는 해당 사용자를 신뢰할 수 있는 자격증 소지자로 식별합니다.

### 테스트 절차

1. **액세스 토큰 요청**: Codef API를 사용하기 위해 액세스 토큰을 요청합니다.
    - **요청 URL**: https://oauth.codef.io/oauth/token
    - **요청 헤더**: Basic 인증 방식으로 클라이언트 ID와 클라이언트 시크릿을 포함합니다.
    - **요청 본문**: `grant_type=client_credentials&scope=read`
2. **확인서 정보 요청**: 획득한 액세스 토큰을 사용하여 확인서의 유효성을 확인합니다.
    - **요청 URL**: https://development.codef.io/v1/kr/etc/hr/qnet-certificate/status
    - **요청 헤더**:
        - `Authorization`: `Bearer {access_token}`
        - `Content-Type`: `application/json`
    - **요청 본문**: 사용자의 정보 및 자격증 문서 정보를 포함합니다.

### Request 예시

```json
{
    "organization": "0001",
    "userName": "{성명}",
    "docNo": "{문서확인번호}"
}
```

### Response 예시

```json
{
    "resIssueYN": "{발행여부}", (0이면 실패, 1이면 성공)
    "resResultDesc": "{결과메시지}",
    "resDocNo": "{문서확인번호}",
    "resPublishNo": "{발행번호}",
    "resDocType": "{확인서종류}",
    "resType": "{시험구분}",
    "resUserNm": "{성명}",
    "commBirthDate": "{생년월일}",
    "resItemName": "{종목}",
    "resExaminationNo": "{수험번호}",
    "resAcquisitionDate": "{시행일}",
    "resInquiryDate": "{조회일자}"
}
```

### 3. 주민등록 진위 여부 테스트

주민등록 진위 여부를 확인하기 위해 외부 API를 연동하여 사용자의 주민등록 진위를 검증하는 작업을 수행하였습니다.

- **API 제공처**: Codef
- **API Endpoint**: https://development.codef.io/v1/kr/public/mw/identity-card/check-status
- **인증 방식**: OAuth 2.0 (클라이언트 자격 증명 방식)
- **요청 데이터**: 사용자 데이터와 공동인증서

이 API는 사용자가 제공한 주민등록번호가 실제로 존재하는지 검토하는 기능을 제공합니다.

### 테스트 절차

1. **액세스 토큰 요청**: Codef API를 사용하기 위해 액세스 토큰을 요청합니다.
    - **요청 URL**: https://oauth.codef.io/oauth/token
    - **요청 헤더**: Basic 인증 방식으로 클라이언트 ID와 클라이언트 시크릿을 포함합니다.
    - **요청 본문**: `grant_type=client_credentials&scope=read`
2. **주민등록 정보 요청**: 획득한 액세스 토큰을 사용하여 사용자 사용자 정보의 유효성을 확인합니다.
    - **요청 URL**: https://development.codef.io/v1/kr/public/mw/identity-card/check-status
    - **요청 헤더**:
        - `Authorization`: `Bearer {access_token}`
        - `Content-Type`: `application/json`
    - **요청 본문**: 사용자의 정보 및 공동 인증서 정보를 포함합니다.

### Request 예시

```json
{
    "organization": "0002",
    "loginType": "0",
    "certType": "1",
    "certFile": "{BASE64로 Encoding된 인증서 der파일 문자열}",
    "keyFile": "{BASE64로 Encoding된 인증서 key파일 문자열}",
    "certPassword": "{RSA암호화된 인증서 비밀번호}",
    "birthDate": "{생년월일}",
    "identity": "{사용자 주민번호}",
    "userName": "{사용자 이름}",
    "issueDate": "{발급일자}", (YYYYMMDD 형식)
}
```

### Response 예시

```json
{
    "resUserNm": "{성명}",
    "resUserIdentiyNo": "{주민등록번호}", (뒤 7자리는 * 로 표시됨)
    "resAuthenticity": "{진위확인}", ("0": false, "1": true)
    "resAuthenticityDesc": "{진위확인 내용}"
}
```


### 4. 여권 유효성 및 인증 테스트

여권 유효성 및 인증 테스트는 사용자의 여권 정보가 유효한지를 확인하는 과정입니다. 이 과정은 외부 API를 통해 이루어지며, 사용자가 제공한 여권 정보가 실제로 유효한지 검증합니다.

#### API 개요

- **API 제공처**: Codef
- **API Endpoint**: [https://development.codef.io/v1/kr/public/mw/passport-data/status](https://development.codef.io/v1/kr/public/mw/passport-data/status)
- **인증 방식**: OAuth 2.0 (클라이언트 자격 증명 방식)
- **요청 데이터**: 여권 정보와 인증서 데이터

#### 테스트 절차

1. **액세스 토큰 요청**: Codef API를 사용하기 위해 액세스 토큰을 요청합니다.
   - **요청 URL**: [https://oauth.codef.io/oauth/token](https://oauth.codef.io/oauth/token)
   - **요청 헤더**: Basic 인증 방식으로 클라이언트 ID와 클라이언트 시크릿을 포함합니다.
   - **요청 본문**: `grant_type=client_credentials&scope=read`

2. **여권 정보 요청**: 획득한 액세스 토큰을 사용하여 여권의 유효성을 확인합니다.
   - **요청 URL**: [https://development.codef.io/v1/kr/public/mw/passport-data/status](https://development.codef.io/v1/kr/public/mw/passport-data/status)
   - **요청 헤더**:
     - `Authorization`: `Bearer {access_token}`
     - `Content-Type`: `application/json`
   - **요청 본문**: 사용자의 여권 정보 및 인증서 정보를 포함합니다.

#### 요청 본문 예시

```json
{
    "organization": "0002",
    "loginType": "2",
    "certType": "1",
    "certFile": "{cert_file_encoded}",
    "keyFile": "{key_file_encoded}",
    "certPassword": "{encrypted_cert_password}",
    "userName": "{user_name}",
    "identity": "{identity_number}",
    "passportNo": "{passport_number}",
    "issueDate": "{issue_date}",
    "expirationDate": "{expiration_date}",
    "birthDate": "{birth_date}"
}
```
"certFile": "{cert_file_encoded}", "keyFile": "{key_file_encoded}" 실제 공동인증서를 사용

<img width="425" alt="image" src="https://github.com/user-attachments/assets/6d4ffcfc-0af4-4a04-84d1-2c6f844ad814">

서버에서 인코딩 및 디코딩을 수행하여 해당 값을 추출하여 Codef에게 전달
<img width="748" alt="image" src="https://github.com/user-attachments/assets/ea870999-75c8-4b54-8eef-8996b2ee8335">


#### 여권 유효성 검사 성공 예시
```
{
    "result": {
        "code": "CF-00000", // 해당 코드의 성공 유무 가이드 https://developer.codef.io/common-guide/error-code
        "extraMessage": "",
        "message": "성공",
        "transactionId": "6715130b28e65e51c0d13262"
    },
    "data": {
        "resAuthenticity": 1,
        "resAuthenticityDesc": ""
    }
}
```
<img width="676" alt="image" src="https://github.com/user-attachments/assets/0497ad78-ea32-4e7d-9f3a-1e6ee4268bb8">


### 5. 운전면허 진위확인 테스트

외부 API를 연동하여 사용자의 운전면허증 진위를 검증하는 작업을 수행하였습니다.

### API 개요

- **API 제공처**: Codef
- **API Endpoint**: https://development.codef.io/v1/kr/public/ef/driver-license/status
- **인증 방식**: OAuth 2.0 (클라이언트 자격 증명 방식)
- **요청 데이터**: 사용자, 운전면허증 정보와 공동 인증서 데이터

### 테스트 절차

1. **액세스 토큰 요청**: Codef API를 사용하기 위해 액세스 토큰을 요청합니다.
    - **요청 URL**: https://oauth.codef.io/oauth/token
    - **요청 헤더**: Basic 인증 방식으로 클라이언트 ID와 클라이언트 시크릿을 포함합니다.
    - **요청 본문**: `grant_type=client_credentials&scope=read`
2. **운전면허 정보 요청**: 획득한 액세스 토큰을 사용하여 운전면허증의 유효성을 확인합니다.
    - **요청 URL**: https://development.codef.io/v1/kr/public/ef/driver-license/status
    - **요청 헤더**:
        - `Authorization`: `Bearer {access_token}`
        - `Content-Type`: `application/json`
    - **요청 본문**: 사용자의 운전면허증 정보 및 인증서 정보를 포함합니다.

### Request 예시

```json
{
    "organization": "0001",
    "loginType": "2",
    "certType": "1",
    "certFile": "{BASE64로 Encoding된 인증서 der파일 문자열}",
    "keyFile": "{BASE64로 Encoding된 인증서 key파일 문자열}",
    "certPassword": "{RSA암호화된 공동인증서 비밀번호}",
    "loginUserName": "{사용자이름}",
    "identity": "{사용자 주민등록번호}",
    "birthDate": "{생년월일YYYYMMDD}",
    "licenseNo01": "{운전 면허번호01 (지역)}",
    "licenseNo02": "{운전 면허번호02 (년도)}",
    "licenseNo03": "{운전 면허번호03}",
    "licenseNo04": "{운전 면허번호04}",
    "serialNo": "{암호일련번호}",
    "userName": "{사용자이름}",
}
```

### Response 예시

```json
{
    "resUserNm": "{성명}",
    "commBirthDate": "{생년월일}",
    "resAuthenticity": "{진위확인}", ("0": false, "1": true, "2": 전산정보만 일치)
    "resLicenseNumber": "{운전면허 번호}",
    "resAuthenticityDesc1": "{전산자료와일치합니다.}",
    "resAuthenticityDesc2": "{식별번호가일치합니다.}"
}
```

## 라이선스 정보

이 프로젝트는 다음의 오픈 소스 라이브러리를 사용합니다. 각 라이브러리는 Apache License 2.0에 따라 라이센스가 부여됩니다.

## 사용된 라이브러리

1. **Hyperledger Fabric**
   - 라이선스: Apache License 2.0
   - [프로젝트 링크](https://www.hyperledger.org/use/fabric)

2. **Apache PDFBox**
   - 라이선스: Apache License 2.0
   - [프로젝트 링크](https://pdfbox.apache.org)

3. **UnivCert (학생 인증)**
   - 라이선스: Apache License 2.0
   - [프로젝트 링크](https://github.com/univcert)

