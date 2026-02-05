package integration_tests.stubs;

import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class SurveyUnitQualityToolPerretAdapterStub implements SurveyUnitQualityToolPort {
    List<Map<String,Set<String>>> receivedMaps = new ArrayList<>();

    @Override
    public ResponseEntity<Object> sendProcessedIds(Map<String, Set<String>> processedIdsMap) {
        receivedMaps.add(processedIdsMap);
        return ResponseEntity.ok().build();
    }
}
