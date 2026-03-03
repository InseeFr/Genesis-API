package fr.insee.genesis.controller.utils.platine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class PlatinePermissionHelper {
    private final RestClient platineRestClient;

    public PlatinePermissionHelper(RestClient.Builder platineRestClientBuilder) {
        this.platineRestClient = platineRestClientBuilder.build();
    }

    public boolean hasExportDataPermission(String interrogationId) {
        try {
            platineRestClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/permissions/check")
                            .queryParam("permission", PlatinePermissionEnum.INTERROGATION_DATA_EXPORT.name())
                            .queryParam("id", interrogationId)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
            log.info("Platine permission granted for interrogation id {} and permission {}",
                    interrogationId,
                    PlatinePermissionEnum.INTERROGATION_DATA_EXPORT.name());
            return true;
        }
        catch (RestClientResponseException e) {
            HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());

            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                log.warn("Platine permission denied for interrogation id {} and permission {}, returned http code: {}",
                        interrogationId,
                        PlatinePermissionEnum.INTERROGATION_DATA_EXPORT.name(),
                        status.value());
                return false;
            }
            throw e;
        }
        catch (RestClientException e) {
            log.error("RestClient failure calling Platine: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            log.error("Unexpected error checking Platine permission: {}", e.getMessage(), e);
            throw e;
        }
    }
}
