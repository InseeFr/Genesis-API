package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonRawDataDocumentMapper;
import lombok.Getter;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class LunaticJsonRawDataPersistanceStub implements LunaticJsonRawDataPersistencePort {
    List<LunaticJsonRawDataDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(LunaticJsonRawDataModel lunaticJsonRawDataModel) {
        mongoStub.add(LunaticJsonRawDataDocumentMapper.INSTANCE.modelToDocument(lunaticJsonRawDataModel));
    }

    @Override
    public List<LunaticJsonRawDataModel> getAllUnprocessedData() {
        return LunaticJsonRawDataDocumentMapper.INSTANCE.listDocumentToListModel(
                mongoStub.stream().filter(
                        lunaticJsonDataDocument -> lunaticJsonDataDocument.processDate() == null
                        )
                .toList()
        );
    }

    @Override
    public List<LunaticJsonRawDataModel> findRawData(String campaignName, Mode mode, List<String> interrogationIdList) {
        List<LunaticJsonRawDataDocument> docs = mongoStub.stream().filter(lunaticJsonRawDataDocument ->
                lunaticJsonRawDataDocument.campaignId().equals(campaignName)
                        && lunaticJsonRawDataDocument.mode().equals(mode)
                        && interrogationIdList.contains(lunaticJsonRawDataDocument.interrogationId())
        ).toList();
        return LunaticJsonRawDataDocumentMapper.INSTANCE.listDocumentToListModel(docs);
    }


    @Override
    public void updateProcessDates(String campaignId, Set<String> interrogationIds) {
        for(LunaticJsonRawDataDocument document : mongoStub.stream().filter(
                lunaticJsonDataDocument -> lunaticJsonDataDocument.campaignId().equals(campaignId)
                && interrogationIds.contains(lunaticJsonDataDocument.interrogationId())
        ).toList()
        ){
            LunaticJsonRawDataDocument newDocument = LunaticJsonRawDataDocument.builder()
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

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        Set<String> questionnaireIds = new HashSet<>();
        for(LunaticJsonRawDataDocument rawDataDoc : mongoStub){
            questionnaireIds.add(rawDataDoc.questionnaireId());
        }
        return questionnaireIds;
    }

    @Override
    public Page<LunaticJsonRawDataModel> findByCampaignIdAndDate(String campaignId, Instant startDt, Instant endDt, Pageable pageable) {
        List<LunaticJsonRawDataDocument> foundRaws = mongoStub.stream()
                .filter(rawData -> rawData.campaignId().equals(campaignId))
                .filter(rawData -> rawData.recordDate().isAfter(LocalDateTime.ofInstant(startDt,ZoneOffset.UTC)))
                .filter(rawData -> rawData.recordDate().isBefore(LocalDateTime.ofInstant(endDt,ZoneOffset.UTC)))
                .toList();
        return new PageImpl<>(LunaticJsonRawDataDocumentMapper.INSTANCE.listDocumentToListModel(foundRaws),
                pageable,
                foundRaws.size());
    }

    @Override
    public long countResponsesByQuestionnaireId(String questionnaireId) {
        return mongoStub.size();
    }
}
