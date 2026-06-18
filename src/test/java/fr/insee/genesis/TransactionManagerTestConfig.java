package fr.insee.genesis;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@TestConfiguration
public class TransactionManagerTestConfig {

    @Bean
    PlatformTransactionManager transactionManager() {
        return Mockito.mock(PlatformTransactionManager.class);
    }
}
