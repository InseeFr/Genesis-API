package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonPersistancePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LunaticJsonPersistanceStub implements LunaticJsonPersistancePort {
    List<LunaticJsonDataDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(LunaticJsonDataModel lunaticJsonDataModel) {
        mongoStub.add(LunaticJsonDocumentMapper.INSTANCE.modelToDocument(lunaticJsonDataModel));
    }

    @Override
    public List<LunaticJsonDataModel> getAllUnprocessedData() {
        return LunaticJsonDocumentMapper.INSTANCE.listDocumentToListModel(
                mongoStub.stream().filter(
                        lunaticJsonDataDocument -> lunaticJsonDataDocument.getProcessDate() == null
                        )
                .toList()
        );
    }
}
