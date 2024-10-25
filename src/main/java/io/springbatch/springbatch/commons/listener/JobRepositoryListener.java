package io.springbatch.springbatch.commons.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobRepositoryListener implements JobExecutionListener {

    private final JobRepository jobRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        String jobName = jobExecution.getJobInstance().getJobName();
        JobParameters jobParameter = new JobParametersBuilder()
                        .addString("requestDate", "20210102")
                        .toJobParameters();

        JobExecution lastJobExecution = jobRepository.getLastJobExecution(jobName, jobParameter);

        if(lastJobExecution != null) {
            for(StepExecution stepExecution : lastJobExecution.getStepExecutions()) {
                BatchStatus status = stepExecution.getStatus();
                ExitStatus exitStatus = stepExecution.getExitStatus();
                String stepName = stepExecution.getStepName();
                log.info("status = {}, exitStatus = {}, stepName = {}", status, exitStatus, stepName);
            }
        }
    }
}
