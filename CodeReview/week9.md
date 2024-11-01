# 9주차 코드리뷰 질문사항

## 동시성 문제 관련
`PersonalExpenseService`의 `savePersonalExpense` 작업 시 동시성 문제가 있었습니다.
해당 메서드에서는 기존에 관련 데이터가 없다면 새로 추가, 있다면 해당 데이터들을 업데이트하고 새로운 데이터도 저장합니다.
처음에는 DB 레벨에서 `Pessimistic Lock` 을 통해 제어하려고 했지만 원하는 결과가 나오지 않았습니다.
여러가지를 시도 해 보다가 메서드에 `syncronized`를 적용해서 한 스레드만 메서드에 접근할 수 있도록 해두었다가,
현재는 ConcurrentHashMap과 synchronized 블록을 사용해 객체 기반 락을 사용한 상태입니다.
관련 테스트는 `PersonalExpenseServiceTest`에 있어 참고하셔도 좋을 것 같습니다.

### 질문
1. `Pessimistic Lock`으로 제대로 제어가 되지 않았는데, 이유를 아직 찾지 못했습니다. 혹시 예상되는 문제점이 있을까요?
2. 다른 동시성 문제 처리 방법들이 궁금합니다!
