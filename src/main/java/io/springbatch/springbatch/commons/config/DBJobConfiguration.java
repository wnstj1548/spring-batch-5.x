package io.springbatch.springbatch.commons.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class DBJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("job", jobRepository)
//                .preventRestart()
                .start(step6())
                .next(step7())
                .build();
    }

    @Bean
    public Step step6() {
        return new StepBuilder("step6", jobRepository)
                .tasklet((contribution, chunkContext) -> {

//                    JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
//                    //LinkedHashMap이었는데 HashMap으로 변경
//
//                    log.info("contribution parameter String : {}", jobParameters.getString("name"));
//                    log.info("contribution parameter Long : {}", jobParameters.getLong("seq"));
//                    log.info("contribution parameter Date : {}", jobParameters.getDate("date"));
//                    log.info("contribution parameter Double : {}", jobParameters.getDouble("age"));
//
//                    Map<String, Object> jobParameters1 = chunkContext.getStepContext().getJobParameters();
                    //parameter 변경하면 map은 변경 안됨

                    log.info("step1 was executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step7() {
        return new StepBuilder("step7", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("step2 was executed");
//                    throw new RuntimeException("step2 has failed");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }
}
