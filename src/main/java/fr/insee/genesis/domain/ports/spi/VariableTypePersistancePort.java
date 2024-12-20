package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.variabletype.VariableTypeModel;

public interface VariableTypePersistancePort {
    void save(VariableTypeModel variableTypeModel);
}
