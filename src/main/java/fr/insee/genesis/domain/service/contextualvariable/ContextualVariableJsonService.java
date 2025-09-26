package fr.insee.genesis.domain.service.contextualvariable;

import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.ports.api.ContextualVariableApiPort;
import fr.insee.genesis.domain.ports.spi.ContextualExternalVariablePersistancePort;
import fr.insee.genesis.domain.ports.spi.ContextualPreviousVariablePersistancePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ContextualVariableJsonService implements ContextualVariableApiPort {
    private final ContextualPreviousVariablePersistancePort contextualPreviousVariablePersistancePort;
    private final ContextualExternalVariablePersistancePort contextualExternalVariablePersistancePort;

    @Autowired
    public ContextualVariableJsonService(ContextualPreviousVariablePersistancePort contextualPreviousVariablePersistancePort, ContextualExternalVariablePersistancePort contextualExternalVariablePersistancePort) {
        this.contextualPreviousVariablePersistancePort = contextualPreviousVariablePersistancePort;
        this.contextualExternalVariablePersistancePort = contextualExternalVariablePersistancePort;
    }

    @Override
    public ContextualVariableModel getContextualVariable(String questionnaireId, String interrogationId) {
        ContextualVariableModel contextualVariableModel = ContextualVariableModel.builder()
                .interrogationId(interrogationId)
                .contextualPrevious(new ArrayList<>())
                .contextualExternal(new ArrayList<>())
                .build();

        ContextualPreviousVariableModel contextualPreviousVariableModel =
                        contextualPreviousVariablePersistancePort.findByQuestionnaireIdAndInterrogationId(
                                questionnaireId,
                                interrogationId
                        );

        if(contextualPreviousVariableModel != null) {
            for (Map.Entry<String, Object> variable : contextualPreviousVariableModel.getVariables().entrySet()) {
                contextualVariableModel.contextualPrevious().addAll(extractVariables(variable.getValue(), variable.getKey()));
            }
        }

        ContextualExternalVariableModel contextualExternalVariableModel =
                        contextualExternalVariablePersistancePort.findByQuestionnaireIdAndInterrogationId(
                                questionnaireId,
                                interrogationId
                        );

        if(contextualExternalVariableModel != null) {
            for (Map.Entry<String, Object> variable : contextualExternalVariableModel.getVariables().entrySet()) {
                contextualVariableModel.contextualExternal().addAll(extractVariables(variable.getValue(), variable.getKey()));
            }
        }

        return contextualVariableModel;
    }

    @SuppressWarnings("unchecked")
    private List<VariableQualityToolDto> extractVariables(Object variable, String variableName) {
        List<VariableQualityToolDto> variableQualityToolDtos = new ArrayList<>();

        if(!(variable instanceof List<?>)){
            variableQualityToolDtos.add(extractValue(variable, variableName, 1));
            return variableQualityToolDtos;
        }

        int i = 1;
        for(Object element : (List<Object>)variable){
            variableQualityToolDtos.add(extractValue(element, variableName, i));
            i++;
        }
        return variableQualityToolDtos;
    }

    private VariableQualityToolDto extractValue(Object variable, String variableName, int iteration) {
        VariableQualityToolDto variableQualityToolDto = VariableQualityToolDto.builder()
                .variableName(variableName)
                .iteration(iteration)
                .variableStateDtoList(new ArrayList<>())
                .build();
        variableQualityToolDto.getVariableStateDtoList().add(
                VariableStateDto.builder()
                        .state(DataState.COLLECTED)
                        .active(true)
                        .value(variable)
                        .date(LocalDateTime.now())
                        .build()
        );
        return variableQualityToolDto;
    }

}
