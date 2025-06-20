package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@Qualifier("surveyQualityToolPerretAdapter")
@Slf4j
public class SurveyQualityToolPerretAdapter implements SurveyUnitQualityToolPort {

    @Autowired
    Config config;


    @Override
    public ResponseEntity<Object> sendProcessedIds(Map<String, Set<String>> processedIdsMap) {
        //TODO Add the real path once service is set up
        return HttpUtils.makeApiCall(config.getSurveyQualityToolUrl(), "/interrogations/",HttpMethod.POST, processedIdsMap,
                Object.class);
    }
}
