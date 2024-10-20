package io.springbatch.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class HelloJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job helloJob() {
        return new JobBuilder("helloJob", jobRepository)
                .start(helloStep())
                .build();
    }

    @Bean
    public Step helloStep() {
        return new StepBuilder("helloStep1", jobRepository)
                .tasklet(helloTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet helloTasklet() {
        return (contribution, chunkContext) -> {
            System.out.println(" ============================");
            System.out.println(" >> Hello Spring Batch");
            System.out.println(" ============================");
            return RepeatStatus.FINISHED;
        };
    }
}
