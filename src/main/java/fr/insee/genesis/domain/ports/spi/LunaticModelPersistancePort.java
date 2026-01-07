package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;

import java.util.List;

public interface LunaticModelPersistancePort {
    void save(LunaticModelModel lunaticModelModel);

    List<LunaticModelDocument> find(String collectionInstrumentId);
}
