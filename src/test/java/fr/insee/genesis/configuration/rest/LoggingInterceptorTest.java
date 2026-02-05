package fr.insee.genesis.configuration.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingInterceptorTest {

    @Test
    void shouldLogRequestAndResponse_onDebug() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
        Level old = logger.getLevel();
        logger.setLevel(Level.DEBUG);

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            LoggingInterceptor interceptor = new LoggingInterceptor();

            MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/test"));
            byte[] body = "hi".getBytes();

            try (var response = interceptor.intercept(request, body, (req, b) ->
                    new MockClientHttpResponse(new byte[0], HttpStatus.OK))) {

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            }

            assertThat(appender.list)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .anyMatch(m -> m.contains("REQUEST: GET http://localhost/test"))
                    .anyMatch(m -> m.contains("RESPONSE STATUS: 200 OK"));
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(old);
        }
    }
}
