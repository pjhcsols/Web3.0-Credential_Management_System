# :closed_lock_with_key: [경북멋쟁이] Web 3.0 신원/자격증명 관리 시스템 

## 주제
**개인이 데이터를 직접 소유하고 관리하는 Web3 전자지갑 시스템 : “Web 3.0 신원/자격증명 관리 시스템"**
<br>
![image](https://github.com/user-attachments/assets/0a312fe1-5f85-4f05-b948-eff5e3ccf505)

이 시스템은 Web3와 외부 인증 API를 통합하여 사용자의 지갑 생성 및 인증서 관리 기능을 강화하고, 추가적인 보안 및 신원 확인을 제공합니다.  
전체 구조는 사용자 인터페이스에서부터 블록체인 및 서버 측까지 각 단계별로 체계적으로 설계되어 있습니다.
<br>

### 🔗 Youtube 시연영상
[[https://www.youtube.com/Web3.0-Credential_Management_System](https://youtube.com/shorts/GkWBhS6io44?feature=share)]
<br>
<br>



## 주요 화면
### 온보딩 화면 및 로그인 화면
* 카카오 로그인 API를 사용하여 로그인을 진행합니다. 로그인이 완료되면 4자리 PIN 코드를 설정하고 생체 인증 여부를 확인하게 됩니다.
* 생체 인증 동의 여부 화면에서 ‘다음에’를 클릭할 경우, 설정 화면에서 생체 인증을 활성화할 수 있습니다.

![image](https://github.com/user-attachments/assets/2740d93b-b644-4246-bbcb-234e392df0cf)
![image](https://github.com/user-attachments/assets/64fd6d22-0486-44ca-984f-726388d46ebf)

![image](https://github.com/user-attachments/assets/b9a1a8fd-32c8-4451-a0da-cf21e46dfb86)
![image](https://github.com/user-attachments/assets/2b7d8c7d-a741-468d-a13c-5372f848eb51)
![image](https://github.com/user-attachments/assets/49e76893-f710-48af-94f5-c48b3cadf8c2)

<br>


### 메인 화면
* 로그인한 사용자의 신원 정보가 표시되는 화면입니다. 
* 사용자의 이름과 인증서 만료일이 표시되며, 인증서를 클릭하면 인증서 사용 기록을 조회할 수 있습니다.
* 사용 기록 조회 화면의 오른쪽 상단 내보내기 아이콘을 클릭 시 사용 기록을 전송할 수 있습니다.

![image](https://github.com/user-attachments/assets/76d7729d-c651-49bf-bace-4435072953e5)
![image](https://github.com/user-attachments/assets/de21b3b3-54a3-4f5e-943a-ed08b5469e1f)

<br>

### 전자 증명서 발급 화면
* 메인 화면의 오른쪽 상단에 위치한 ‘+’ 버튼을 클릭하면, 각 증명서와 해당 발급처가 표시됩니다.
* 본인이 발급을 원하는 증명서를 선택한 후, 동의 체크박스를 클릭하고 인증(생체 인증 또는 PIN 코드 인증)을 하면 사용자에게 PDF 형태로 인증서가 표시됩니다.

![image](https://github.com/user-attachments/assets/5ee28388-cd3f-490c-9ca9-05ef420aa525)
![image](https://github.com/user-attachments/assets/44cc151f-aba9-4b63-9c8c-dcd6673976f8)
![image](https://github.com/user-attachments/assets/43808704-c95f-4936-ae54-72c0a5d73a78)

<br>

![image](https://github.com/user-attachments/assets/d9c18a77-75f4-4ec1-82e0-a8574109bfeb)

* 해당 인증서의 목록을 S3 메타데이터로부터 받아와서 사용자에게 1차적으로 보여주고,  
인증서를 클릭하면 블록과 메타데이터의 정보로 외부 API를 통해 인증서가 유효한지 확인을 요청하는 흐름입니다.

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

* Android 기기에서는 BiometricPrompt API를, iOS 기기에서는 LocalAuthentication Framework를 사용하여  
지문, 얼굴 인식과 같은 생체 정보를 이용한 인증을 수행합니다.
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
3. 지갑에서 개인 디바이스의 블록이 생성되며 블록에는 추후 업로드하는 해당 증명에 관한 키 값을 저장하고 블록이 생성 및 추가되며 이를 통해 외부 인증과의 연동이 수행됩니다.
4. 지갑에서 해당되는 인증서를 업로드 가능하며 PDF의 페이지 별 메타데이터를 별도로 관리하여 1페이지(주민등록증)는 Verifiable Credential로 관리합니다.

<br>

## 인증서 발급 과정

### 인증서 등록

![image](https://github.com/user-attachments/assets/96e2e235-ea12-4360-8449-e30555901ae8)

1. 클라이언트는 인증서 등록을 위해 2차 인증을 수행합니다.
2. Web3 블록체인을 통해 인증서에 대한 Verifiable Credential 생성 및 신분증명 요청을 처리합니다.
3. 외부 인증 API를 활용해 인증서 Verifiable Credential에 대한 신분 증명을 요청합니다.
4. 서버는 등록된 인증서의 블록 URL을 저장하고 관리합니다.

<br>

### 인증서 접근

![image](https://github.com/user-attachments/assets/4f154c47-4ab3-4a45-b07a-50ed825e7bfb)

1. 클라이언트는 등록된 인증서에 접근하기 위해 2차 인증을 수행합니다.
2. Web3 블록체인을 통해 인증서에 대한 인증을 요청하고 블록 URL을 반환합니다.
3. 외부 인증 API를 활용해 인증서 Verifiable Credential에 대한 신분 증명을 요청하고 확인합니다.
4. 서버는 인증이 완료된 인증서에 접근할 수 있도록 지원합니다.

<br>

### 인증서 목록 보기

![image](https://github.com/user-attachments/assets/ca75af9d-9d04-456d-99c1-c713910ba3cb)

1. 클라이언트는 등록된 인증서에 접근하기 위해 2차 인증을 수행합니다.
2. Web3 블록체인을 통해 인증서에서 Verifiable Credential을 찾아오고, 인증서 URL을 반환합니다.
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

2. Web3 블록체인의 블록에 외부 인증 값 및 정보 저장을 처리합니다. 개인 디바이스의 블록에 외부 인증을 수행하는 Verifiable Credential, Credential Metadata, Claims, Proofs를 포함한 블록을 생성 관리합니다.
3. 외부 인증 API를 활용해 전자지갑의 인증서 사용 시, 외부 인증과의 연동을 수행합니다.

<br>
<br>

## 외부 API를 이용한 인증 (대학 재학 인증, 자격증, 주민등록증)

사용자 및 인증서의 신뢰성을 위해 외부 API를 연동하여 대학 재학 인증, Qnet 자격증 확인서 및 주민등록 진위 여부 인증을 테스트하는 작업을 수행합니다.


### 1. 재학 인증 테스트

![image](https://github.com/user-attachments/assets/1f153339-efc8-4fe4-9361-17b761c3e9bd)

사용자가 특정 대학에 재학 중인지를 확인하기 위해 외부 API를 통해 사용자의 학적 정보를 확인할 수 있는 기능을 통합하였습니다. API 요청을 통해 사용자의 재학 상태를 검증하고, 인증기관 서버는 이 정보를 바탕으로 사용자가 제공한 정보의 진위를 확인합니다. 성공적인 재학 인증 요청에 대해, 시스템은 사용자가 제공한 정보와 외부 API의 응답이 일치하는지를 확인하고, 재학 상태가 검증된 사용자로 표시합니다.

![image](https://github.com/user-attachments/assets/90e2ea2a-582c-4c90-bbbe-f9dc51e952b0)

<br>

### 2. Qnet 자격증 확인서 인증 테스트

Qnet 자격증 확인서는 특정 자격증 소지 여부를 검증하는 데 사용됩니다. 프로젝트에서는 Codef API를 통해 자격증 정보와 확인서를 검증하는 절차를 테스트하였습니다.
<br>
![image](https://github.com/user-attachments/assets/4ac278be-1d8a-4bce-af94-c8c0f88aa80b)
<br>
API 요청을 통해 사용자가 소지한 자격증의 유효성을 확인하고, Qnet에서 반환한 응답 데이터를 바탕으로 사용자가 주장하는 자격증 소지가 올바른지를 검증하였습니다.
API 응답의 검증을 통해 유효한 자격증을 가진 사용자로 인증되었을 경우, 인증기관 서버는 해당 사용자를 신뢰할 수 있는 자격증 소지자로 식별합니다.

<br>

### 3. 주민등록 진위 여부 테스트


![image](https://github.com/user-attachments/assets/394a2f99-55e1-408e-b5df-685dd9631c2f)
<br>
주민등록 진위 여부를 확인하기 위해 외부 API를 연동하여 사용자의 주민등록 진위를 검증하는 작업을 수행하였습니다.
<br>
이 API는 사용자가 제공한 주민등록번호가 실제로 존재하는지 검토하는 기능을 제공합니다.


### git commit message head
Feat : 새로운 기능 추가, 기존의 기능을 요구 사항에 맞추어 수정  
Fix : 기능에 대한 버그 수정  
Build : 빌드 관련 수정  
Chore : 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore  
Docs : 문서(주석) 수정  
Refactor : 기능의 변화가 아닌 코드 리팩터링 ex) 변수 이름 변경  
Test : 테스트 코드 추가/수정  
Init : 초기화  

