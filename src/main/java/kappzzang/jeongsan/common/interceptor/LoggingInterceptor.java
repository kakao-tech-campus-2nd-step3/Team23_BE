package kappzzang.jeongsan.common.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final int MAX_BODY_LENGTH = 1000;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        return execution.execute(request, body);
    }

    private void logRequest(HttpRequest request, byte[] body) {
        String bodyPreview = new String(body, StandardCharsets.UTF_8);
        if (bodyPreview.length() > MAX_BODY_LENGTH) {
            bodyPreview = bodyPreview.substring(0, MAX_BODY_LENGTH) + "... (truncated)";
        }
        log.info("=== REQUEST ===\nMethod: {}\nURI: {}\nHeaders: {}\nBody preview: {}",
            request.getMethod(),
            request.getURI(),
            request.getHeaders(),
            bodyPreview);
    }
}
