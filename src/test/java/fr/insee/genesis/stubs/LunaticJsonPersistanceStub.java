package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonPersistancePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonDocumentMapper;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Override
    public List<LunaticJsonDataDocument> findRawData(String campaignName, Mode mode, List<String> idUEList) {
        return
            mongoStub.stream().filter(lunaticJsonDataDocument ->
                    lunaticJsonDataDocument.getCampaignId().equals(campaignName)
                            && lunaticJsonDataDocument.getMode().equals(mode)
                            && idUEList.contains(lunaticJsonDataDocument.getIdUE())
            ).toList();
    }

    @Override
    public void updateProcessDates(String campaignId, Set<String> idUEs) {
        for(LunaticJsonDataDocument document : mongoStub.stream().filter(
                lunaticJsonDataDocument -> lunaticJsonDataDocument.getCampaignId().equals(campaignId)
                && idUEs.contains(lunaticJsonDataDocument.getIdUE())
        ).toList()
        ){
            document.setProcessDate(LocalDateTime.now());
        }
    }
}
