# Spring Batch 5.1.2 버전으로 옮기면서 변경사항

1. EnableBatchProcessing 사용하지 않음. <br>
- EnableBatchProcessing 을 쓰게되면 BatchAutoConfiguration 작동 안함. <br>
-> 자동 구성과 수동 구성 요소가 동시에 존재할 때 발생할 수 있는 모호성, 충돌 피하기 위함.

2. BuilderFactory 사용 x
- 직접 사용해서 더 많은 제어 권한 획득 / Tasklet 사용시, PlatformTransactionManager 함께 사용해야함

3. 배치 설정 클래스
- BasicBatchConfigurer -> DefaultBatchConfiguration으로 변경

4. JobLauncher가 프록시 객체가 아닌 실제 객체라 DefaultBatchConfiguration에서 얻어 올 필요없이 바로 타입 캐스팅 가능 <br><br>

5. 실행 할 job을 찾지 못하면 예외 발생<br><br>

6. Spring Batch4에서는 완료 된 job에서 JobInstanceAlreadyCompleteException이 발생했지만 5에서는 exception은 발생하지 않고 execution에 status는 complete / exitcode는 noop으로 생성됨 + 완료 안됐으면 정상처리 <br><br>

7. SimpleJobExecutor 사라짐 -> TaskExecutorJobLauncher 사용


# 공부 내용 정리

## Job?
- 배치 계층 구조에서 가장 상위의 개념 (하나의 배치작업 자체)
- Job Configuration을 통해 생성되는 객체 단위로서 배치 작업을 어떻게 구성하고 실행할 것인지 전체적으로 설명하고 명세해 놓은 객체
  (설계도)
- 한개 이상의 Step 포함 / Spring Batch가 기본 구현체 제공

  
### 기본 구현체
1. SimpleJob
   - 순차적으로 step을 실행


2. FlowJob
    - 특정한 조건과 흐름에 따라 Step을 구성하여 실행(Flow 객체 실행)

### Job 실행 흐름
![스크린샷 2024-10-22 오후 5 00 35](https://github.com/user-attachments/assets/6e3cc2a2-4f60-429e-8c38-4f169419b49e)

내부 흐름
<br>

new JobBuilder -> simpleJobBuilder -> start 실행되면 step 리스트에 tasklet추가 이후 next로 하나씩 추가 -> build() 실행할 때 step 리스트가지고 job에 세팅
실행은 BatchAutoConfiguration에서 JobLauncherApplicationRunner -> jobLauncher.execute(job, parameter)가 실행함

상태정보나 실행정보 -> 메타 데이터(JobInstance, JobExecution, StepExecution)

Job, Step, Flow는 실제 배치 job을 실행하고, 구성하기 위한 용도


### JobInstance

- Job이 실행될 때 생성된 Job의 논리적 실행 단위 객체 -> 고유하게 식별 가능한 작업 실행
- Job의 설정과 구성은 동일해도 처리하는 내용이 다르기 떄문
> 하루에 한 번 실행되는 Job이 있다면 매일 실행되는 각각의 Job을 JobInstance로 표현한다.
- 처음 실행되는 Job + JobParameter일 경우 새로운 JobInstance 생성
- 이전과 동일한 Job + JobParameter로 실행할 경우는 이미 존재하는 JobInstance 리턴 / Job과는 1:다 관계
- 기존 JobInstance 리턴이 되면 더이상 수행되지않고 중단됨. -> error
  ![스크린샷 2024-10-23 오후 2 16 39](https://github.com/user-attachments/assets/cfd9e939-1d3f-4407-8284-db25421b33f9)
  ![스크린샷 2024-10-23 오후 2 16 49](https://github.com/user-attachments/assets/c046d306-9ffd-42a5-905f-0c2429bd1998)

<br>

### JobParameter

- JobInstance 와 JobParameter는 1:1 관계
- Job 실행 시 함께 포함되어 사용되는 파라미터를 가진 도메인 객체
- JobInstance 구분하기 위한 용도 <br><br>
- JobExecution과는 1:다 관계

- 생성 방법

  - 어플리케이션 실행 시 주입 (java -jar LogBatch.jar requestDate=20210101)
  - 코드 생성 (JobParameterBuilder, DefaultJobParametersConverter) -> 처음꺼 많이씀
  - SpEL 이용 (@Value("#{jobParameter[requestDate]})), @JobScope, @StepScope 선언 필수<br><br>

- Step단계에서 parameter 참조 가능 (tasklet의 StepContribution, ChunkContext 둘다 참조 가능)
  - stepContribution -> stepExecution -> jobExecution에서 parameter 가져옴

- 어플리케이션 실행 시 주입
>kimjunseo@Junseos-MacBook-Pro target % java -jar spring-batch-0.0.1-SNAPSHOT.jar name=user1,java.lang.String seq=2,java.lang.Long date=2021-01-01,java.util.Date age=16.5,java.lang.Double
- 형식 지정을 이런식으로 해야된다. 근데 Date는 아직 안되서 해결 방법 찾는중

### JobExecution

- JobInstance에 대한 한번의 시도를 의미 -> Job 실행 중에 발생한 정보들 저장(시작시간, 종료시간, 상태, 종료상태)
- JobExecution은 FAILED or COMPLETED 등의 실행 결과 상태를 가짐 -> COMPLETE면 재실행 불가능 / FAILED면 재실행 가능
- JobExecution의 실행상태 결과가 COMPLETED 될 때까지 하나의 JobInstance 내에서 여러번 시도 가능
- Instance : Exeuction = 1:다

![스크린샷 2024-10-24 오전 10 03 01](https://github.com/user-attachments/assets/aabb5e77-f7a5-4043-9c47-b9d4443922b0)

## Step

- job을 구성하는 독립적인 하나의 단계 / 실제 배치 처리 정의 및 컨트롤 하는데 모든 정보를 가지고 있는 도메인 객체
- 입력 - 처리 - 출력의 비즈니스 로직 및 설정
- task기반 설정

### 기본 구현체 
1. TaskletStep : 기본, tasklet타입 제어
2. PartitionStep : 멀티 스레드 / step을 여러개로 분리해서 실행
3. JobStep : step 내에서 job 실행
4. FlowStep : step 내에서 flow 실행

### StepExecution
- Step에 대한 한번의 시도 (jobExecution과 jobInstance와의 관계와 유사)
- step이 실행될 때마다 생성(실제로 실행될 때만) / step별로 생성
- job 재시작하더라도 완료된 step은 재실행되지 않음
- JobExecution : StepExecution = 1:다 관계

### StepContribution 
- 청크 프로세스의 변경사항을 버퍼링 한 후 StepExecution 상태 업데이트
- 청크 커밋 직전에 StepExecution 의 apply 메서드를 호출하여 상태 업데이트
- ExitStatus의 기본 종료코드 외 사용자 정의 종료코드 생성 및 적용 가능
- 단계의 기여도를 나타내고, 특정 단계에 얼마나 많은 데이터를 읽고 처리했는지 등의 통계

![스크린샷 2024-10-25 오전 11 37 38](https://github.com/user-attachments/assets/3e49bb87-68db-4536-b94b-aaec2d2ef8d5)

step execution이 생성(tasklet이 수행되는 과정) / db에 커밋하기 직전에 끝

### ExecutionContext
- 프레임워크에서 유지 및 관리한다.
- Map을 가지고 있으며 StepExecution, JobExecution 객체의 상태를 저장하는 공유 객체
- Job 재시작 시 이미 처리한 Row 데이터는 건너뛰고 이후로 수행하도록 할 때 상태정보 활용

### JobRepository
- 배치 작업 중 정보를 저장하는 저장소 역할
- Job이 언제 수행되고, 언제 끝났고, 몇번 실행되었고, 실행 결과 등의 배치 작업 수행과 관련된 모든 meta data를 저장
- JobLauncher, Job, Step 구현체 내부에서 CRUD 기능
- BatchConfigurer 인터페이스 구현 or 상속해서 JobRepository 설정 커스터마이징 가능 -> jdbc : jobRepositoryFactoryBean / in memory : MapJobRepositoryFactoryBean
- jdbc로 할 때는 내부적으로 트랜잭션 처리해주고 isolation의 기본값은 serialize / prefix 변경 가능 default는 BATCH_

### JobLauncher
- 배치 job을 실행시키는 역할 / job, jobParameter를 인자로 받 / 배치 작업 후 client에게 job execution 반환
- 스프링 부트 배치가 구동되면 jobLauncher 빈 자동생성
- job 실행 (jobLauncher.run(job, jobParameters)) / spring boot에서는 JobLauncherApplicationRunner가 알아서 실

### 동기적 실행
- taskExecutor를 SyncTaskExecutor로 설정 (기본값)
- 처리 완료 후 client에게 반환
- 스케줄러에 따른 배치처리에 적합 (길어도 됨)

### 비동기적 실행
- taskExecutor를 SimpleAsyncTaskExecutor로 설정한 경우
- jobExecution 획득 후 클라이언트 반환, 이후 배치처리 완료
- http 요청에 대한 배치처리에 적합

<img width="1317" alt="스크린샷 2024-10-26 오후 12 21 29" src="https://github.com/user-attachments/assets/0b050a14-410a-4528-b49f-a2bfb135af35">

## 배치 초기화 설정

1. JobLauncherApplicationRunner -> Batch 작성을 실행 / ApplicationRunner의 구현체 / 기본적으로 빈으로 등록된 모든 job을 실행
   
2. BatchProperties (application.properties / application.yml) -> BatchAutoConfiguration에서 함
    - batch 환경 설정 / job 이름 및 스키마 초기화 설정, 테이블 prefix 등 값 설정 가능
   
3. job 실행 옵션
    - 지정한 Batch Job만 실행하도록 할 수 있음 / spring.batch.job.names: ${job.name:NONE}
    - application 실행 시 Program argument로 job이름 입력 (--job.name = helloJob / --job.name = helloHob, simpleJob)

## 스프링 배치 실행

1. jobBuilderFactory는 사라짐 (5.0 이상)
2. jobBuilder, repository넘기면 다음 메서드 (start)의 파라미터에 따라 jobBuilder가 SimpleJob / FlowJob을 생성한다.

## SimpleJob
1. job의 구현체 -> simpleJobBuilder에서 생성
2. 여러 단계의 step으로 구성되어있으며, step을 순차적으로 실행한다.
3. 모든 step이 완료되어야 job이 성공 / 마지막에 실행한 step의 상태가 job의 상태가 된다.

### simpleJob 구현체에서의 함수들
1. start(Step step) : 최초 한 번 설정
2. next(Step step) : 횟수 제한 x
3. incrementer(JobParametersIncrementer) : JobParameter 값 자동 증가 -> 재실행 가능
4. preventRestart(boolean) : 기본값 true, false로 설정 시 실패해도 재실행 불가능함
5. validator(JobParameterValidator) : JobParameter 실행 전 검증
6. listener(JobExecutionListener) : job, step 등 전후에 리스너 설정 가능

#### validator <br>
-> 기본적으로 DefaultJobParametersValidator를 지원, 복잡한 구조가 필요하면 implements해서 사용 <br>
requiredKeys : 필수적으로 넣어야되는 파라미터의 키 값 <br>
optionalKeys : 선택적 파라미터 키 값 <br>
<br>
필수 값 없으면 JobParametersInvalidException 발생 <br>
jobRepository 기능 실행 전 한번 검증 / job 실행 전 한번 더 검증

#### preventRestart <br>

-> job의 재시작 여부 결정 / false일 시 실패해도 재시작을 지원하지 않음.
<br>
JobExecution 존재하는지 검사 -> 존재 x -> 실행여부 상관없음 <br>
JobExecution 존재하는지 검사 -> 존재 o -> JobRestartException <br>
.preventRestart() 하면 false로 설정

#### incrementer <br>

-> rdb에서 identity로 올리는 것과 비슷함 / .incrementer(new RunIdIncrementer)

### SimpleJob 흐름도 <br>

![배치실행+흐름도](https://github.com/user-attachments/assets/880807e9-a052-47e6-8835-09b8fec6e1b2)

## StepBuilder <br>

1. stepBuilderFactory는 사라짐, JobRepository 바로 파라미터로 넣는 형식

### 종류

1. taskletStepBuilder -> tasklet 구현체 실행
2. SimpleStepBuilder -> chunkOrientedTasklet 실행
3. PartitionStepBuilder -> partitionStep 생성, 멀티쓰레드
4. JobStepBuilder -> step 안에 job을 실행한다
5. FlowStepBuilder -> step 안에 flow를 실행한다.

![배치실행+흐름도 2](https://github.com/user-attachments/assets/109140b1-8b0a-46c8-9749-17a46f787b88)

### TaskletStep

1. TaskletStep이 tasklet을 실행시, RepeatTemplate을 사용해서 Tasklet 반복, Tasklet은 내부적으로 Transaction 처리함

#### task 기반 vs chunk 기반

- chunk 기반 : 하나의 큰 덩어리를 n개씩 나눠서 실행한다는 의미, 대량 처리에 효과적
- task 기반 : 단일 작업 기반으로 처리, 주로 tasklet 구현체 만들어서 사용, 대량 처리 시 chunk 기반보다 복잡함

<img width="1079" alt="스크린샷 2025-03-18 오전 11 21 29" src="https://github.com/user-attachments/assets/be8c57be-37c5-4c3c-8c41-036e58d5eac6" />

#### 함수

1. tasklet(Tasklet)
2. startLimit(Integer) -> 실행 횟수 조정, step마다 설정, 설정 값 초과하면 exception, default는 Integer.MAX_VALUE
3. allowStartIfComplete(true) -> 완료된 step이어도 실행
4. listener(StepExecutionListener) -> 리스너 추가

## Tasklet

- step 내에서 구성되고 실행되는 도메인 객체로서 단일 테스크를 수행하기 위한 것
- taskletStep에 의해 반복적으로 수행 or 종료된다. 
-> RepeatStatus (FINISHED, CONTINUABLE) / null로 반환하면 FINISHED
- 익명 클래스 or 구현 클래스 만들어서 사용 -> 메서드 실행 시 taskletStepBuilder가 반환되어 관련 설정 가능
- step에는 하나의 tasklet만 가질 수 있으며, 많을 경우 마지막만 적

## JobStep

- Job안에 Step이 있고 step안에 job이 있는 구조
- 테이블 자체는 다른 하나의 job으로 처리된다.
- job을 실행할 launcher 필요
- .launcher(JobLauncher) -> 실행할 jobLauncher 설정
- .parametersExtractor(ParametersExtractor) -> step의 ExecutionContext를 Job이 실행되는데 필요한 JobParamters로 변환

## FlowJob

- 상태에 따라 흐름을 전환하도록 구성가능
  - step이 실패해도 job은 실패하지 않아야되는 경우
  - step 성공 시 다음에 실행해야 될 step을 구분해야되는 경우
  - 특정 step은 실행하지 않도록 구성해야하는 경우
- Flow와 Job의 흐름을 구성하는데만 관여하고 실제 비즈니스 로직은 step에서 실행된다.
- 내부적으로 SimpleFlow 객체를 포함하고 있으며 Job 실행 시 호출된다.

### Builder

- .start(Step, Flow)
- .on(String pattern) 
-> step의 실행 결과로 돌려받는 종료 상태(ExitStatus)를 캐치하여 매칭, TransitionBuilder 반환
- .to(Step) -> 다음으로 이동할 step 지정
- .stop() / .fail() / .end() / stopAndRestart() -> 중지/실패/종료하도록 Flow 종료
- .from(Step) -> 이전 단계의 step의 flow를 추가 정의
- .next(Step, Flow, JobExecutionDecider) 
- .end() -> build 앞에 위치하면 FlowBuilder 종료 및 SimpleFlow 객체 생성

## Transition

1. 배치 상태 유형 -> BatchStatus / ExitStatus / FlowExecutionStatus

   ### BatchStatus -> jobExecution / stepExecution의 속성으로 종료 후 최종 결과 상태 정의
    - simpleJob -> 마지막 step의 ExitStatus (Step이 실패 시 그게 마지막 step)
    - flowJob -> flow내 step의 ExitStatus , 마지막 Flow값이 상태값
    - enum타입 (COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN)
    - ABANDONED는 처리는 완료했지만 성공하지 못한 단계와 재시작 시 건너뛰어야 하는 단

   ### ExitStatus -> JobExecution, StepExecution이 어떤 상태로 종료되었는지
    - BatchStatus와 동일한 값으로 설정
    - UNKNOWN, EXECUTING, COMPLETED, NOOP, FAILED, STOPPED<br>
   

2. Transition Builder

- .on(String pattern) -> transitionBuilder 반환 / 특수문자는 *, ?만 허용
- .to(Step, Flow, JobExecutionDecider) -> 매칭되면 이동
- .stop() / fail() / end() / stopAndRestart(Step, Flow, JobExecutionDecider) -> job의 BatchStatus, ExitStatus 반영
- .from()

stop() -> jobExecution, ExitStatus, FlowExecutionStatus -> STOPPED
fail() -> jobExecution, ExitStatus, FlowExecutionStatus -> FAILED
end() -> jobExecution, ExitStatus,FlowExecutionStatus -> COMPLETED / step이 FAILED여도 COMPLETED로 종료, 재시작 불가
stopAndRestart() -> 중단 이전에만 COMPLETED, 이후는 STOPPED 재시작 시 이후부터 시작