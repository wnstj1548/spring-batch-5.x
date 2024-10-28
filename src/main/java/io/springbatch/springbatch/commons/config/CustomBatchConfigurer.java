package io.springbatch.springbatch.commons.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class CustomBatchConfigurer {

    private final DataSource dataSource;

    //4점대에서는 BasicBatchConfigurer createJobRepository를 상속받아서 했지만 BasicBatchConfigurer 없어져서 변경
    @Bean
    public JobRepository customJobRepository(PlatformTransactionManager transactionManager) throws Exception {

        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setTablePrefix("BATCH_");
        factory.afterPropertiesSet();
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");

        return factory.getObject();
    }
}
