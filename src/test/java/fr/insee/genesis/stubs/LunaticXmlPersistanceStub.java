package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticXmlDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticXmlPersistancePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.xml.LunaticXmlDataDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticXmlDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class LunaticXmlPersistanceStub implements LunaticXmlPersistancePort {
    List<LunaticXmlDataDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(LunaticXmlDataModel lunaticXmlDataModel) {
        mongoStub.add(LunaticXmlDocumentMapper.INSTANCE.modelToDocument(lunaticXmlDataModel));
    }
}