package kappzzang.jeongsan.global.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.interceptor.Context.AfterExecution;
import software.amazon.awssdk.core.interceptor.Context.BeforeExecution;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;

@Slf4j
@Component
public class AwsClientLoggingInterceptor implements ExecutionInterceptor {

    @Override
    public void beforeExecution(BeforeExecution context, ExecutionAttributes executionAttributes) {
        try {
            log.info("AWS Request: {}", context.request().toString());
        } catch (NullPointerException e) {
            log.warn("NPE occur in AWS logging: {}", e.getMessage());
        }
    }

    @Override
    public void afterExecution(AfterExecution context, ExecutionAttributes executionAttributes) {
        try {
            log.info("AWS Response: {}", context.response().toString());
        } catch (NullPointerException e) {
            log.warn("NPE occur in AWS logging: {}", e.getMessage());
        }
    }
}
