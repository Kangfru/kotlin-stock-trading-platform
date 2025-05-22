1. 분산락
   1. Redis를 활용해 분산락을 구현하기 위해 3가지 특성이 보장되어야 한다.
      - 오직 한 순간에 하나의 작업자만이 락을 걸 수 있다.
      - 락 이후, 어떠한 문제로 인해 락을 풀지 못하고 종료된 경우라도 다른 작업자가 락을 획득할 수 있어야 한다.
      - 레디스 노드가 작동하는 한 모든 작업자는 락을 걸고, 해체할 수 있어야 한다.
   2. 싱글 인스턴스 Redis를 활용해 SET NX(IF NOT EXISTS SET) - setIfAbsent 활용
      - Spin lock 을 구현 하는 방법의 하나로 setIfAbsent 호출 시 키가 없어 저장에 성공(락 획득에 성공) 한다면 true를 반환한다.
      - false를 반환받게 된다면, 이미 다른 쓰레드나 트랜잭션에서 락을 획득해 사용 중인 것으로 간주.
      - 이때 true가 리턴될 때까지 재시도 하는 방식의 분산락을 spin lock 이라고 하며 lock을 획득할 때까지 지속 시도하므로 redis에 부하를 주게 된다.
   3. Pub/Sub 기반의 분산락 처리 - Redisson library 활용.
   4. 

2. Redis 의 Transactional 지원에 대해
   1. redisTemplate bean 생성 시 setEnableTransactionSupport(true) 기능을 통해 Redis 작업을 Spring의 트랜잭션 관리 범위 내에서 수행할 수 있게 해준다.
      - 이 설정이 활성화되면 Redis 명령은 곧바로 실행되지 않고 트랜잭션이 커밋될 때까지 큐에 쌓이게 된다.
      - `setIfAbsent`는 트랜잭션 모드에서 null을 반환한다.
      - Redis 의 트랜잭션은 아래와 같다.
      - MULTI(트랜잭션 시작) : Redis의 트랜잭션을 시작하는 커맨드. 트랜잭션을 시작하면 Redis 커맨드는 바로 실행되는게 아닌 queue에 쌓인다.
      - EXEC(정상 커밋) : 정상적으로 처리되어 queue에 쌓여있는 명령어를 일괄적으로 실행
      - DISCARD(롤백) : queue에 쌓여있는 명령어를 일괄적으로 폐기.
      - WATCH(락) : Redis에서 Lock을 담당하는 명령어로 낙관적 락(Optimistic Lock) 기반.
        WATCH 명령어를 사용하면 이 후 UNWATCH 되기전에는 1번의 EXEC 또는 TRANSACTION이 아닌 커맨드만 허용 됨.
