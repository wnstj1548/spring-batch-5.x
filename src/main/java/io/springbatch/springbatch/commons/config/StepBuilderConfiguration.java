package io.springbatch.springbatch.commons.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.SimplePartitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StepBuilderConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job stepBuilderJob() {
        return new JobBuilder("stepBuilderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(stepBuilderStep1())
                .next(stepBuilderStep2())
                .next(stepBuilderStep3())
                .build();
    }

    @Bean
    public Step stepBuilderStep1() {
        return new StepBuilder("stepBuilderStep1", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("stepBuilderStep1");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    public Step stepBuilderStep2() {
        return new StepBuilder("stepBuilderStep2", jobRepository)
                .chunk(3, transactionManager)
                .reader(new ItemReader<Object>() {
                    @Override
                    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        return null;
                    }
                })
                .processor(new ItemProcessor<Object, Object>() {
                    @Override
                    public Object process(Object item) throws Exception {
                        return null;
                    }
                })
                .writer(new ItemWriter<Object>() {

                    @Override
                    public void write(Chunk<?> chunk) throws Exception {

                    }
                })
                .build();
    }

    @Bean
    public Step stepBuilderStep3() {
        return new StepBuilder("stepBuilderStep3", jobRepository)
                .partitioner("stepBuilderPart", new SimplePartitioner())
                .step(stepBuilderStep1())
                .gridSize(2)
                .build();
    }

    @Bean
    public Step stepBuilderStep4() {
        return new StepBuilder("stepBuilderStep4", jobRepository)
                .job(newJob())
                .build();
    }

    @Bean
    public Step stepBuilderStep5() {
        return new StepBuilder("stepBuilderStep5", jobRepository)
                .flow(flow())
                .build();
    }

    @Bean
    public Flow flow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
        flowBuilder.start(stepBuilderStep2()).end();
        return flowBuilder.build();
    }

    @Bean
    public Job newJob() {
        return new JobBuilder("newJob", jobRepository)
                .start(stepBuilderStep1())
                .next(stepBuilderStep2())
                .next(stepBuilderStep3())
                .build();
    }
}
