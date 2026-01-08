package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.GroupedInterrogationDocument;
import fr.insee.genesis.infrastructure.mappers.GroupedInterrogationDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonRawDataDocumentMapper;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public Set<String> findDistinctQuestionnaireIdsByNullProcessDate() {
        Set<String> questionnaireIds = new HashSet<>();
        mongoStub.stream().filter(
                lunaticJsonDataDocument -> lunaticJsonDataDocument.processDate() == null
        ).forEach(doc -> {
            if(doc.questionnaireId() != null){
                questionnaireIds.add(doc.questionnaireId());
            }
        });
        return questionnaireIds;
    }

    @Override
    public Set<Mode> findModesByQuestionnaire(String questionnaireId) {
        return new HashSet<>(mongoStub.stream()
                .filter(doc -> Objects.equals(doc.questionnaireId(), questionnaireId))
                .map(LunaticJsonRawDataDocument::mode)
                .distinct()
                .toList());
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
    public List<LunaticJsonRawDataModel> findRawDataByInterrogationID(String interrogationId) {
        List<LunaticJsonRawDataDocument> docs = mongoStub.stream()
                .filter(doc -> interrogationId.equals(doc.interrogationId()))
                .toList();

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
    public long countRawResponsesByQuestionnaireId(String questionnaireId) {
        return mongoStub.size();
    }

    @Override
    public List<GroupedInterrogation> findProcessedIdsGroupedByQuestionnaireSince(LocalDateTime since) {
        List<LunaticJsonRawDataDocument> recentDocs = mongoStub.stream().filter(
                rawData -> rawData.processDate() != null && rawData.processDate().isAfter(since)
        ).toList();

        // Aggregation map: key = (questionnaireId, partitionOrCampaignId), value = Set of interrogationId
        Map<String, Map<String, Set<String>>> groupedMap = new HashMap<>();

        for (LunaticJsonRawDataDocument doc : recentDocs) {
            String questionnaireId = doc.questionnaireId();
            String partitionOrCampaignId = doc.campaignId();
            String interrogationId = doc.interrogationId();
            groupedMap
                    .computeIfAbsent(questionnaireId, q -> new HashMap<>())
                    .computeIfAbsent(partitionOrCampaignId, p -> new HashSet<>())
                    .add(interrogationId);
        }

        // Conversion to a list of GroupedInterrogationDocument
        List<GroupedInterrogationDocument> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Set<String>>> entry1 : groupedMap.entrySet()) {
            String questionnaireId = entry1.getKey();
            Map<String, Set<String>> innerMap = entry1.getValue();

            for (Map.Entry<String, Set<String>> entry2 : innerMap.entrySet()) {
                GroupedInterrogationDocument grouped = new GroupedInterrogationDocument();
                grouped.setQuestionnaireId(questionnaireId);
                grouped.setPartitionOrCampaignId(entry2.getKey());
                grouped.setInterrogationIds(new ArrayList<>(entry2.getValue()));
                result.add(grouped);
            }
        }
        return GroupedInterrogationDocumentMapper.INSTANCE.listDocumentToListModel(result);
    }

    @Override
    public List<GroupedInterrogation> findUnprocessedIds() {
        List<LunaticJsonRawDataDocument> recentDocs = mongoStub.stream().filter(
                rawData -> rawData.processDate() == null).toList();

        // Aggregation map: key = (questionnaireId, partitionOrCampaignId), value = Set of interrogationId
        Map<String, Map<String, Set<String>>> groupedMap = new HashMap<>();

        for (LunaticJsonRawDataDocument doc : recentDocs) {
            String questionnaireId = doc.questionnaireId();
            String partitionOrCampaignId = doc.campaignId();
            String interrogationId = doc.interrogationId();
            groupedMap
                    .computeIfAbsent(questionnaireId, q -> new HashMap<>())
                    .computeIfAbsent(partitionOrCampaignId, p -> new HashSet<>())
                    .add(interrogationId);
        }

        // Conversion to a list of GroupedInterrogationDocument
        List<GroupedInterrogationDocument> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Set<String>>> entry1 : groupedMap.entrySet()) {
            String questionnaireId = entry1.getKey();
            Map<String, Set<String>> innerMap = entry1.getValue();

            for (Map.Entry<String, Set<String>> entry2 : innerMap.entrySet()) {
                GroupedInterrogationDocument grouped = new GroupedInterrogationDocument();
                grouped.setQuestionnaireId(questionnaireId);
                grouped.setPartitionOrCampaignId(entry2.getKey());
                grouped.setInterrogationIds(new ArrayList<>(entry2.getValue()));
                result.add(grouped);
            }
        }
        return GroupedInterrogationDocumentMapper.INSTANCE.listDocumentToListModel(result);
    }

    @Override
    public Set<String> findUnprocessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId) {
       List<LunaticJsonRawDataDocument> unprocessedDocuments =
               mongoStub.stream().filter(
                       lunaticJsonDataDocument -> lunaticJsonDataDocument.processDate() == null
                       && lunaticJsonDataDocument.questionnaireId().equals(collectionInstrumentId)
               ).toList();
       Set<String> interrogationIds = new HashSet<>();
       unprocessedDocuments.forEach(doc -> interrogationIds.add(doc.interrogationId()));
       return interrogationIds;
    }
}
