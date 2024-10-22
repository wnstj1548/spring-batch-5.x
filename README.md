# Spring Batch 5.1.2 버전으로 옮기면서 변경사항

1. EnableBatchProcessing 사용하지 않음. <br>
-> 개발자가 명시적으로 해당 설정 해야한다.



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
new JobBuilder -> simpleJobBuilder -> start 실행되면 step 리스트에 tasklet추가 이후 next로 하나씩 추가 -> build() 실행할 때 step 리스트가지고 job에 세팅
실행은 BatchAutoConfiguration에서 JobLauncherApplicationRunner -> jobLauncher.execute(job, parameter)가 실행함
