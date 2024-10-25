# Spring Batch 5.1.2 버전으로 옮기면서 변경사항

1. EnableBatchProcessing 사용하지 않음. <br>
- EnableBatchProcessing 을 쓰게되면 BatchAutoConfiguration 작동 안함. <br>
-> 자동 구성과 수동 구성 요소가 동시에 존재할 때 발생할 수 있는 모호성, 충돌 피하기 위함.

2. BuilderFactory 사용 x
- 직접 사용해서 더 많은 제어 권한 획득 / Tasklet 사용시, PlatformTransactionManager 함께 사용해야함

3. 배치 설정 클래스
- BasicBatchConfigurer -> DefaultBatchConfiguration으로 변경

4. JobLauncher가 프록시 객체가 아닌 실제 객체라 DefaultBatchConfiguration에서 얻어 올 필요없이 바로 타입 캐스팅 가능 <br><br>

5. 실행 할 job을 찾지 못하면 예외 발생 

6. Spring Batch4에서는 완료 된 job에서 JobInstanceAlreadyCompleteException이 발생했지만 5에서는 exception은 발생하지 않고 execution에 status는 complete / exitcode는 noop으로 생성됨 + 완료 안됐으면 정상처리



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