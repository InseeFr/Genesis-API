package fr.insee.genesis.domain.service.surveymetadata;

import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.SurveyMetadataApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyMetadataPersistancePort;
import fr.insee.genesis.infrastructure.document.surveymetadata.VariableDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyMetadataDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.VariableDocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class SurveyMetadataService implements SurveyMetadataApiPort {
    @Qualifier("surveyMetadataMongoAdapter")
    private final SurveyMetadataPersistancePort surveyMetadataPersistancePort;

    @Autowired
    public SurveyMetadataService(SurveyMetadataPersistancePort surveyMetadataPersistancePort) {
        this.surveyMetadataPersistancePort = surveyMetadataPersistancePort;
    }

    @Override
    public void saveMetadatas(String campaignId, String questionnaireId, Mode mode, VariablesMap variablesMap) {
        SurveyMetadataModel surveyMetadataModel = SurveyMetadataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .mode(mode)
                .variableDocumentMap(new HashMap<>())
                .build();

        for(String variableName : variablesMap.getVariables().keySet()){
            Variable bpmVariable = variablesMap.getVariable(variableName);
            surveyMetadataModel.variableDocumentMap().put(variableName,
                    VariableDocumentMapper.INSTANCE.bpmToDocument(bpmVariable));
        }

        surveyMetadataPersistancePort.save(surveyMetadataModel);
    }

    @Override
    public SurveyMetadataModel getMetadatas(String campaignId, String questionnaireId, Mode mode) {
        return SurveyMetadataDocumentMapper.INSTANCE.documentToModel(
                surveyMetadataPersistancePort.find(campaignId, questionnaireId, mode)
        );
    }

    @Override
    public VariablesMap getVariableMapFromMetadatas(String campaignId, String questionnaireId, Mode mode) {
        SurveyMetadataModel surveyMetadataModel = SurveyMetadataDocumentMapper.INSTANCE.documentToModel(
                surveyMetadataPersistancePort.find(campaignId, questionnaireId, mode)
        );

        VariablesMap variablesMap = new VariablesMap();
        for(Map.Entry<String, VariableDocument> entry :surveyMetadataModel.variableDocumentMap().entrySet()){
            VariableDocument variableDocument = entry.getValue();
            Variable variable = new Variable(
                    variableDocument.name(),
                    variableDocument.group(),
                    variableDocument.type(),
                    variableDocument.sasFormat()
            );
            variable.setMaxLengthData(variableDocument.maxLengthData());
            variable.setQuestionName(variableDocument.questionName());
            variable.setInQuestionGrid(variableDocument.isInQuestionGrid());

            variablesMap.putVariable(variable);
        }
        return variablesMap;
    }
}
