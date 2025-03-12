package io.springbatch.springbatch.controller;

import io.springbatch.springbatch.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
public class JobLauncherController {

//    private final JobLauncher jobLauncher; -> 여기서 TaskExecutorJobLauncher 주입받으면 proxy객체로 나와서 타입 캐스팅이 안됨
    private final Job job;
    private final DefaultBatchConfiguration defaultBatchConfiguration;

    @PostMapping("/batch")
    public ResponseEntity<String> launch(@RequestBody Member member) throws Exception {

        TaskExecutorJobLauncher jobLauncher = (TaskExecutorJobLauncher) defaultBatchConfiguration.jobLauncher();
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor()); // 비동기 설정

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("id", member.getId())
                .addDate("date", new Date())
                .toJobParameters();

        jobLauncher.run(job, jobParameters);

        return ResponseEntity.ok("batch completed");
    }
}
