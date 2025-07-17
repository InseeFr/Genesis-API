package fr.insee.genesis.domain.service.editedresponse;

import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.editedresponse.EditedExternalResponseModel;
import fr.insee.genesis.domain.model.editedresponse.EditedPreviousResponseModel;
import fr.insee.genesis.domain.model.editedresponse.EditedResponseModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.ports.api.EditedResponseApiPort;
import fr.insee.genesis.domain.ports.spi.EditedExternalResponsePersistancePort;
import fr.insee.genesis.domain.ports.spi.EditedPreviousResponsePersistancePort;
import fr.insee.genesis.infrastructure.mappers.EditedExternalResponseDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.EditedPreviousResponseDocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EditedResponseJsonService implements EditedResponseApiPort {
    private final EditedPreviousResponsePersistancePort editedPreviousResponsePersistancePort;
    private final EditedExternalResponsePersistancePort editedExternalResponsePersistancePort;

    @Autowired
    public EditedResponseJsonService(EditedPreviousResponsePersistancePort editedPreviousResponsePersistancePort, EditedExternalResponsePersistancePort editedExternalResponsePersistancePort) {
        this.editedPreviousResponsePersistancePort = editedPreviousResponsePersistancePort;
        this.editedExternalResponsePersistancePort = editedExternalResponsePersistancePort;
    }

    @Override
    public EditedResponseModel getEditedResponse(String questionnaireId, String interrogationId) {
        EditedResponseModel editedResponseModel = EditedResponseModel.builder()
                .interrogationId(interrogationId)
                .editedPrevious(new ArrayList<>())
                .editedExternal(new ArrayList<>())
                .build();

        EditedPreviousResponseModel editedPreviousResponseModel =
                EditedPreviousResponseDocumentMapper.INSTANCE.documentToModel(
                        editedPreviousResponsePersistancePort.findByQuestionnaireIdAndInterrogationId(
                                questionnaireId,
                                interrogationId
                        ));

        if(editedPreviousResponseModel != null) {
            for (Map.Entry<String, Object> variable : editedPreviousResponseModel.getVariables().entrySet()) {
                editedResponseModel.editedPrevious().addAll(extractVariables(variable.getValue(), variable.getKey()));
            }
        }

        EditedExternalResponseModel editedExternalResponseModel =
                EditedExternalResponseDocumentMapper.INSTANCE.documentToModel(
                        editedExternalResponsePersistancePort.findByQuestionnaireIdAndInterrogationId(
                                questionnaireId,
                                interrogationId
                        ));

        if(editedExternalResponseModel != null) {
            for (Map.Entry<String, Object> variable : editedExternalResponseModel.getVariables().entrySet()) {
                editedResponseModel.editedExternal().addAll(extractVariables(variable.getValue(), variable.getKey()));
            }
        }

        return editedResponseModel;
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
