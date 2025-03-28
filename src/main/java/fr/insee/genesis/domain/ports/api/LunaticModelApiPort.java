package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;

public interface LunaticModelApiPort {
    void save(LunaticModelModel model);
}
