package io.springbatch.springbatch.commons.config;

import io.springbatch.springbatch.commons.incrementer.CustomJobParametersIncrementer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
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
                .start(helloStep1())
                .next(helloStep2())
//                .incrementer(new RunIdIncrementer())
                .incrementer(new CustomJobParametersIncrementer())
                .validator(new JobParametersValidator() {
                    @Override
                    public void validate(JobParameters parameters) throws JobParametersInvalidException {

                    }
                })
//                .preventRestart()
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {

                    }
                })
                .build();
    }

    @Bean
    public Step helloStep1() {
        return new StepBuilder("helloStep1", jobRepository)
                .tasklet(helloTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step helloStep2() {
        return new StepBuilder("helloStep2", jobRepository)
                .tasklet(helloTasklet2(), transactionManager)
                .build();
    }

    //tasklet은 기본적으로 무한반복이므로 RepeatStatus 필수
    @Bean
    public Tasklet helloTasklet() { //Tasklet은 단일 태스크로 수행되는 로직 구현(비즈니스 로직 구현)
        return (contribution, chunkContext) -> {
            System.out.println(" ============================");
            System.out.println(" >> Hello Spring Batch");
            System.out.println(" ============================");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet helloTasklet2() {
        return (contribution, chunkContext) -> {
            System.out.println(" ============================");
            System.out.println(" >> Step 2");
            System.out.println(" ============================");
            return RepeatStatus.FINISHED;
        };
    }
}
