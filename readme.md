1. 분산락
   1. Redis를 활용해 분산락을 구현하기 위해 3가지 특성이 보장되어야 한다.
      - 오직 한 순간에 하나의 작업자만이 락을 걸 수 있다.
      - 락 이후, 어떠한 문제로 인해 락을 풀지 못하고 종료된 경우라도 다른 작업자가 락을 획득할 수 있어야 한다.
      - 레디스 노드가 작동하는 한 모든 작업자는 락을 걸고, 해체할 수 있어야 한다.
   2. 싱글 인스턴스 Redis를 활용해 SET NX(IF NOT EXISTS SET) - setIfAbsent 활용

2. Redis 의 Transactional 지원에 대해
   1. redisTemplate bean 생성 시 setEnableTransactionSupport(true) 기능을 통해 Redis 작업을 Spring의 트랜잭션 관리 범위 내에서 수행할 수 있게 해준다.
      - 이 설정이 활성화되면 Redis 명령은 곧바로 실행되지 않고 트랜잭션이 커밋될 때까지 큐에 쌓이게 된다.
      - `setIfAbsent`는 트랜잭션 모드에서 null을 반환한다.
