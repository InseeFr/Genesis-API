package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.exceptions.GenesisException;

import java.util.Map;

public interface LunaticModelApiPort {
    void save(String questionnaireId, Map<String, Object> lunaticModel);

    LunaticModelModel get(String questionnaireId) throws GenesisException;
}
