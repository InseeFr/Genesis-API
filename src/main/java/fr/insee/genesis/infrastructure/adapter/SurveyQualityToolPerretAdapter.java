package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.infrastructure.utils.http.HttpUtils;
import fr.insee.genesis.infrastructure.utils.http.OidcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@Qualifier("surveyQualityToolPerretAdapter")
@Slf4j
public class SurveyQualityToolPerretAdapter implements SurveyUnitQualityToolPort {

    private final Config config;
    private final OidcService oidcService;

    @Autowired
    public SurveyQualityToolPerretAdapter( Config config,OidcService oidcService) {
        this.config = config;
        this.oidcService = oidcService;
    }


    @Override
    public ResponseEntity<Object> sendProcessedIds(Map<String, Set<String>> processedIdsMap) throws IOException {
        return HttpUtils.makeApiCall(config.getSurveyQualityToolUrl(), "/interrogations/",HttpMethod.POST, processedIdsMap,
                Object.class, oidcService);
    }
}
