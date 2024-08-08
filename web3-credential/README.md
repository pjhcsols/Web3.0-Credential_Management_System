# spring-gift-point

# step3
Feat: 포인트 기능 구현 [박한솔]
* Point 도메인 클래스 생성 및 포인트 충전, 사용, 적립 기능 구현
* PointService 클래스에서 포인트 관련 서비스 메소드 추가
* PointRepository를 통해 사용자에 대한 포인트 정보를 조회 및 저장
* 사용자 등록 시 포인트 자동 생성 기능 추가
  
Fix: 포인트 데이터 누락 오류 수정 [박한솔]
* 사용자 생성 시 포인트 데이터가 누락되는 문제 수정
* 사용자 생성 시 Point 객체를 자동으로 생성하여 PointRepository에 저장
* 포인트 데이터 초기화 로직 개선
  PointService 클래스 개선
* 포인트 충전 및 사용 기능 개선
* 불필요한 로직 제거 및 코드 정리
* 포인트 충전 최소 금액 조건 추가 (10,000원 이상 충전 필요)
  
Refactor: KakaoService에서 포인트 사용 및 적립 로직 추가 & Order 포인트 사용 기능 추가 [박한솔]
* createOrder 메소드에서 포인트 사용 및 적립 기능 추가
* 주문 시 포인트 차감 및 적립 처리
* 잔여 포인트에 대한 로직 개선 및 테스트 추가
* OrderRequest 클래스에 포인트 사용 필드 (pointsToUse) 추가
* 주문 생성 시 포인트 사용 및 적립 로직 추가

# step2
- AWS ec2 배포 
- 지속적인 배포를 위한 배포 스크립트(sh)를 작성한다.
- 클라이언트와 API 연동 시 발생하는 보안 문제에 대응한다.
  
# step1
- 요구사항 명세에 따른 컨트롤러 엔드포인트 수정 & 스웨거 API 문서 설정
## Refactor: 회원 기능 API 수정

- **회원 가입 API 구현**  
  Endpoint: `/api/members/register`

- **로그인 API 구현**  
  Endpoint: `/api/members/login`

## Refactor: 카테고리 기능 API 수정

- **카테고리 생성 API 구현**  
  Endpoint: `/api/categories`

- **카테고리 수정 API 구현**  
  Endpoint: `/api/categories/{categoryId}`

- **카테고리 목록 조회 API 구현**  
  Endpoint: `/api/categories`

## Refactor: 상품 기능 API 수정

- **상품 생성 API 구현**  
  Endpoint: `/api/products`

- **상품 조회 API 구현**  
  Endpoint: `/api/products/{productId}`

- **상품 수정 API 구현**  
  Endpoint: `/api/products/{productId}`

- **상품 삭제 API 구현**  
  Endpoint: `/api/products/{productId}`

- **상품 목록 조회 API 구현 (페이지네이션 적용)**  
  Endpoint: `/api/products?page=0&size=10&sort=name,asc&categoryId=1`

## Refactor: 상품 옵션 기능 API 수정

- **상품 옵션 추가 API 구현**  
  Endpoint: `/api/products/{productId}/options`

- **상품 옵션 수정 API 구현**  
  Endpoint: `/api/products/{productId}/options/{optionId}`

- **상품 옵션 삭제 API 구현**  
  Endpoint: `/api/products/{productId}/options/{optionId}`

- **상품 옵션 목록 조회 API 구현**  
  Endpoint: `/api/products/{productId}/options`

## Refactor: 위시 리스트 기능 API 수정

- **위시 리스트 상품 추가 API 구현**  
  Endpoint: `/api/wishes`

- **위시 리스트 상품 삭제 API 구현**  
  Endpoint: `/api/wishes/{wishId}`

- **위시 리스트 상품 조회 API 구현 (페이지네이션 적용)**  
  Endpoint: `/api/wishes?page=0&size=10&sort=createdDate,desc`

## Refactor: 주문 기능 API 수정

- **주문하기 API 구현**  
  Endpoint: `/api/orders`

- **주문 목록 조회 API 구현 (페이지네이션 적용)**  
  Endpoint: `/api/orders?page=0&size=10&sort=orderDateTime,desc`
