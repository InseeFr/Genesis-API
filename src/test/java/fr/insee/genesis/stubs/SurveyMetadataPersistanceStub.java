package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;
import fr.insee.genesis.domain.ports.spi.SurveyMetadataPersistancePort;
import fr.insee.genesis.infrastructure.document.surveymetadata.SurveyMetadataDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyMetadataDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SurveyMetadataPersistanceStub implements SurveyMetadataPersistancePort {
    List<SurveyMetadataDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(SurveyMetadataModel surveyMetadataModel) {
        mongoStub.add(SurveyMetadataDocumentMapper.INSTANCE.modelToDocument(surveyMetadataModel));
    }

    @Override
    public SurveyMetadataDocument find(String campaignId, String questionnaireId, Mode mode) {
        return mongoStub.stream().filter(variableTypeDocument ->
                variableTypeDocument.getCampaignId().equals(campaignId)
                && variableTypeDocument.getQuestionnaireId().equals(questionnaireId)
                && variableTypeDocument.getMode().equals(mode)
        ).toList().getFirst();
    }
}
