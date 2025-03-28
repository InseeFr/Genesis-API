package fr.insee.genesis.domain.ports.api;

import java.util.Map;

public interface LunaticModelApiPort {
    void save(String questionnaireId, Map<String, Object> lunaticModel);
}
