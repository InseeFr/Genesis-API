package fr.insee.genesis.infrastructure.client;

import fr.insee.genesis.configuration.MinioConfig;
import io.minio.MinioClient;
import org.springframework.stereotype.Component;

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
