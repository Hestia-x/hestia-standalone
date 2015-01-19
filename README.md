# hestia-standalone
finance manager for family

## 개발 문서(?)
### 환경
* Java 8 이상 버젼 필요.
* 이클립스 프로젝트.

### 규칙
* asset: 은행 계좌, 현금, 외상 등 직접적인 재산에 한정.
* shop: 거래 대상.
* debit_code: 구매 내역 코드. (차변 코드)
  * asset에 연결 된 코드는 해당 asset을 + 시키는 코드.
  * asset_id가 없는 코드는 모두 지출로 취급.
* credit_code: 지불 내역 코드. (대변 코드)
  * asset에 연결 된 코드는 해당 asset을 - 시키는 코드.
  * asset_id가 없는 코드는 모두 수입으로 취급
* slip: 전표. 한 상점에서 한번에 구매한 내역 모음. 
  * debit list와 credit list를 가지고 있음.
  * 원칙적으로는 debit의 총합과 credit의 총합이 같아야 하지만(대차평형 원리) 여기서는 깔끔히 무시함.
  

### 구현 된 URL
* /account_book/view/asset/
* /account_book/view/asset/${assetId}
* /account_book/view/slip/${slipId}
* /account_book/view/cashflow/${year}_${month}/
* /account_book/view/cashflow/${year}_${month}/debit/${debit_code_id}
* /account_book/view/cashflow/${year}_${month}/credit/${credit_code_id}
  * 음수 표기는 수정해야 함.
### 구현 할 URL
* view
  * /account_book/view/cashflow/
  * /account_book/view/budget/${year}_${month}
* update - GET이면 form, POST면 update. 데이터가 잘못 되었으면 form + 메세지.
  * /account_book/edit/slip/${slipId}
  * /account_book/add/slip
  * /account_book/config/asset
  * /account_book/config/shop
  * /account_book/config/debit_code
  * /account_book/config/credit_code
  * /account_book/config/budget_months
  * /account_book/config/budget/${year}_${month}
