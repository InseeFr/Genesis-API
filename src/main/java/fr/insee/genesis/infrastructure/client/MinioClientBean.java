package fr.insee.genesis.infrastructure.client;

import fr.insee.genesis.configuration.MinioConfig;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "fr.insee.genesis.minio.enable", havingValue = "true")
@Component
public class MinioClientBean extends MinioClient {
    protected MinioClientBean(MinioConfig minioConfig) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
        super(minioClient);
    }
}
