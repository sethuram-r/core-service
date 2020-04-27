package smarshare.coreservice.write.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Slf4j
@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "sagaOrchestratorTaskExecutor")
    public Executor taskExecutor() {
        log.debug( "Creating Async Task Executor" );
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize( 2 );
        executor.setMaxPoolSize( 2 );
        executor.setQueueCapacity( 100 );
        executor.setThreadNamePrefix( "SagaOrchestratorThread-" );
        executor.initialize();
        return executor;
    }
}