package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.variabletype.VariableTypeModel;
import fr.insee.genesis.domain.ports.spi.VariableTypePersistancePort;
import fr.insee.genesis.infrastructure.document.variabletype.VariableTypeDocument;
import fr.insee.genesis.infrastructure.mappers.VariableTypeDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class VariableTypePersistanceStub implements VariableTypePersistancePort {
    List<VariableTypeDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(VariableTypeModel variableTypeModel) {
        mongoStub.add(VariableTypeDocumentMapper.INSTANCE.modelToDocument(variableTypeModel));
    }

    @Override
    public VariableTypeDocument find(String campaignId, String questionnaireId, Mode mode) {
        return mongoStub.stream().filter(variableTypeDocument ->
                variableTypeDocument.getCampaignId().equals(campaignId)
                && variableTypeDocument.getQuestionnaireId().equals(questionnaireId)
                && variableTypeDocument.getMode().equals(mode)
        ).toList().getFirst();
    }
}
