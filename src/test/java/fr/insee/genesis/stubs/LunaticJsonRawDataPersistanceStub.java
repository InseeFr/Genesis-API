package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistancePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonDocumentMapper;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class LunaticJsonRawDataPersistanceStub implements LunaticJsonRawDataPersistancePort {
    List<LunaticJsonDataDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(LunaticJsonRawDataModel lunaticJsonRawDataModel) {
        mongoStub.add(LunaticJsonDocumentMapper.INSTANCE.modelToDocument(lunaticJsonRawDataModel));
    }

    @Override
    public List<LunaticJsonRawDataModel> getAllUnprocessedData() {
        return LunaticJsonDocumentMapper.INSTANCE.listDocumentToListModel(
                mongoStub.stream().filter(
                        lunaticJsonDataDocument -> lunaticJsonDataDocument.processDate() == null
                        )
                .toList()
        );
    }

    @Override
    public List<LunaticJsonDataDocument> findRawData(String campaignName, Mode mode, List<String> interrogationIdList) {
        return
            mongoStub.stream().filter(lunaticJsonDataDocument ->
                    lunaticJsonDataDocument.campaignId().equals(campaignName)
                            && lunaticJsonDataDocument.mode().equals(mode)
                            && interrogationIdList.contains(lunaticJsonDataDocument.interrogationId())
            ).toList();
    }

    @Override
    public void updateProcessDates(String campaignId, Set<String> interrogationIds) {
        for(LunaticJsonDataDocument document : mongoStub.stream().filter(
                lunaticJsonDataDocument -> lunaticJsonDataDocument.campaignId().equals(campaignId)
                && interrogationIds.contains(lunaticJsonDataDocument.interrogationId())
        ).toList()
        ){
            LunaticJsonDataDocument newDocument = LunaticJsonDataDocument.builder()
                    .id(document.id())
                    .campaignId(document.campaignId())
                    .questionnaireId(document.questionnaireId())
                    .interrogationId(document.interrogationId())
                    .mode(document.mode())
                    .data(document.data())
                    .processDate(LocalDateTime.now())
                    .recordDate(document.recordDate())
                    .build();

            mongoStub.removeIf((
                    lunaticJsonDataDocument -> lunaticJsonDataDocument.campaignId().equals(campaignId)
                            && interrogationIds.contains(lunaticJsonDataDocument.interrogationId())));

            mongoStub.add(newDocument);
        }
    }
}
