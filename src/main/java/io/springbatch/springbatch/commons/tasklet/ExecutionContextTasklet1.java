package io.springbatch.springbatch.commons.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExecutionContextTasklet1 implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("step1 was executed");

        ExecutionContext jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
        ExecutionContext stepExecutionContext = contribution.getStepExecution().getExecutionContext();

        String jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobInstance().getJobName();
        String stepName = chunkContext.getStepContext().getStepExecution().getStepName();

        if(jobExecutionContext.get("jobName") == null) {
            jobExecutionContext.put("jobName", jobName);
        }

        if(stepExecutionContext.get("stepName") == null) {
            stepExecutionContext.put("stepName", stepName);
        }

        log.info("job Name : {}, stepName : {}", jobExecutionContext.get("jobName"), stepExecutionContext.get("stepName"));

        return RepeatStatus.FINISHED;
    }
}
