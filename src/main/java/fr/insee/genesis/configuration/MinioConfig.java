package fr.insee.genesis.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Getter
public class MinioConfig {
    @Value("${fr.insee.genesis.minio.endpoint}")
    private String endpoint;

    @Value("${fr.insee.genesis.minio.access_key}")
    private String accessKey;

    @Value("${fr.insee.genesis.minio.secret_key}")
    private String secretKey;

    @Value("${fr.insee.genesis.minio.enable}")
    private boolean enable;

    @Value("${fr.insee.genesis.minio.bucket_name}")
    private String bucketName;
}
