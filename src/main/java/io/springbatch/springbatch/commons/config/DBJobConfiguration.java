package io.springbatch.springbatch.commons.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DBJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("job", jobRepository)
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
                    //LinkedHashMap이었는데 HashMap으로 변경

                    log.info("contribution parameter String : {}", jobParameters.getString("name"));
                    log.info("contribution parameter Long : {}", jobParameters.getLong("seq"));
                    log.info("contribution parameter Date : {}", jobParameters.getDate("date"));
                    log.info("contribution parameter Double : {}", jobParameters.getDouble("age"));

                    Map<String, Object> jobParameters1 = chunkContext.getStepContext().getJobParameters();
                    //parameter 변경하면 map은 변경 안됨

                    log.info("step1 was executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("step2", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("step2 was executed");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }
}
