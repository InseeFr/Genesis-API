package fr.insee.genesis.domain.service.variabletype;

import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.variabletype.VariableTypeModel;
import fr.insee.genesis.domain.ports.api.VariableTypeApiPort;
import fr.insee.genesis.domain.ports.spi.VariableTypePersistancePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
public class VariableTypeService implements VariableTypeApiPort {
    @Qualifier("variableTypeMongoAdapter")
    private final VariableTypePersistancePort variableTypePersistancePort;

    @Autowired
    public VariableTypeService(VariableTypePersistancePort variableTypePersistancePort) {
        this.variableTypePersistancePort = variableTypePersistancePort;
    }

    @Override
    public void saveMetadatas(String campaignId, String questionnaireId, Mode mode, VariablesMap variablesMap) {
        VariableTypeModel variableTypeModel = VariableTypeModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .mode(mode)
                .variables(new LinkedHashMap<>())
                .build();

        for(String variableName : variablesMap.getVariables().keySet()){
            Variable bpmVariable = variablesMap.getVariable(variableName);
            variableTypeModel.getVariables().put(variableName,bpmVariable.getType());
        }

        variableTypePersistancePort.save(variableTypeModel);
    }
}
