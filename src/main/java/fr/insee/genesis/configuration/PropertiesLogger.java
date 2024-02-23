package fr.insee.genesis.configuration;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Set<String> hiddenWords = Set.of("password", "pwd", "jeton", "token", "secret","uri");

    @Override
    public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {
        Environment environment = event.getEnvironment();

        log.info("===============================================================================================");
        log.info("                                     Properties                                                ");

        ((AbstractEnvironment) environment).getPropertySources().stream()
                .filter(EnumerablePropertySource.class::isInstance)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames()).flatMap(Arrays::stream).distinct()
                .filter(Objects::nonNull)
                .filter(ps -> ps.startsWith("fr.insee") || ps.startsWith("spring")).forEach(key -> log
                        .info(key + " = " + hideProperties(key, environment)));
        log.info("===============================================================================================");

    }

    private static Object hideProperties(String key, Environment environment) {
        if (hiddenWords.stream().anyMatch(key::contains)) {
            return "******";
        }
        return environment.getProperty(key);

    }
}

