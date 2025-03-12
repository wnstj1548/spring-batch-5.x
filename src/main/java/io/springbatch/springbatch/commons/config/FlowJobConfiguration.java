package io.springbatch.springbatch.commons.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FlowJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job flowJob() {
        return new JobBuilder("flowJob", jobRepository)
                .start(flow())
                .next(flowStep3())
                .end()
                .build();
    }

    @Bean
    public Flow flow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
        return flowBuilder.start(flowStep1())
                .next(flowStep2())
                .end();
    }

    @Bean
    public Step flowStep1() {
        return new StepBuilder("flowStep1", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("flowStep1 was executed");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    public Step flowStep2() {
        return new StepBuilder("flowStep2", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("flowStep2 was executed");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    public Step flowStep3() {
        return new StepBuilder("flowStep3", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("flowStep3 was executed");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }
}
