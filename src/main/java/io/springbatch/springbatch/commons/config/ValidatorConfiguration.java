package io.springbatch.springbatch.commons.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class ValidatorConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job validatorJob() {
        return new JobBuilder("validatorJob", jobRepository)
                .start(validatorStep1())
                .next(validatorStep2())
                .next(validatorStep3())
//                .validator(new CustomJobParametersValidator())
                .validator(new DefaultJobParametersValidator(new String[] {"name", "date"}, new String[] {"count"}))
                //처음 배열은 requiredKey, 두번째 배열은 optionalKey
                .build();
    }

    @Bean
    public Step validatorStep1() {
        return new StepBuilder("validatorStep1", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("validatorStep1 execute");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    public Step validatorStep2() {
        return new StepBuilder("validatorStep2", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("validatorStep2 execute");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    public Step validatorStep3() {
        return new StepBuilder("validatorStep3", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("validatorStep3 execute");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }
}
