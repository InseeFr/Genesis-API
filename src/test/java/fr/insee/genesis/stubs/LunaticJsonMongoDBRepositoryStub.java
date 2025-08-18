package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.GroupedInterrogationDocument;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import lombok.Getter;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Getter
public class LunaticJsonMongoDBRepositoryStub implements LunaticJsonMongoDBRepository {

    List<LunaticJsonRawDataDocument> documents = new ArrayList<>();
    Map<String, Long> countByQuestionnaireIdMap = new HashMap<>();

    @Override
    public List<LunaticJsonRawDataDocument> findByNullProcessDate() {
        return documents.stream()
                .filter(doc -> doc.processDate() == null)
                .toList();
    }

    @Override
    public List<Mode> findModesByCampaignId(String campaignId) {
        return documents.stream()
                .filter(doc -> Objects.equals(doc.campaignId(), campaignId))
                .map(LunaticJsonRawDataDocument::mode)
                .distinct()
                .toList();
    }

    @Override
    public List<LunaticJsonRawDataDocument> findModesByCampaignIdAndByModeAndinterrogationIdIninterrogationIdList(
            String campaignName, Mode mode, List<String> interrogationIdList) {
        return documents.stream()
                .filter(doc -> Objects.equals(doc.campaignId(), campaignName)
                        && Objects.equals(doc.mode(), mode)
                        && interrogationIdList.contains(doc.interrogationId()))
                .toList();
    }

    @Override
    public Page<LunaticJsonRawDataDocument> findByCampaignIdAndRecordDateBetween(String campagneId, Instant  start, Instant end, Pageable pageable){
        return Page.empty(pageable);
    }

    @Override
    public long countByQuestionnaireId(String questionnaireId) {
        return countByQuestionnaireIdMap.getOrDefault(questionnaireId, 0L);
    }

    @Override
    public List<GroupedInterrogationDocument> aggregateRawGrouped(LocalDateTime since) {
        List<LunaticJsonRawDataDocument> recentDocs = documents.stream().filter(
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
        return result;
    }

    @Override
    public List<GroupedInterrogationDocument> aggregateRawGroupedWithNullProcessDate() {
        List<LunaticJsonRawDataDocument> recentDocs = documents.stream().filter(
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
        return result;
    }

    // Implémentations vides requises par MongoRepository
    @Override public <S extends LunaticJsonRawDataDocument> S save(S entity) { return null; }
    @Override public Optional<LunaticJsonRawDataDocument> findById(String s) { return Optional.empty(); }
    @Override public void delete(LunaticJsonRawDataDocument entity) {//empty impl for MongoRepository
    }

    @Override
    public void deleteAllById(Iterable<? extends String> strings) {
        //empty impl for MongoRepository
    }

    @Override
    public void deleteAll(Iterable<? extends LunaticJsonRawDataDocument> entities) {
        //empty impl for MongoRepository
    }

    @Override
    public void deleteAll() {
        //empty impl for MongoRepository
    }

    @Override
    public <S extends LunaticJsonRawDataDocument> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override public List<LunaticJsonRawDataDocument> findAll() { return new ArrayList<>(); }

    @Override
    public List<LunaticJsonRawDataDocument> findAllById(Iterable<String> strings) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String s) {
        //empty impl for MongoRepository
    }

    @Override public boolean existsById(String s) { return false; }

    @Override
    public <S extends LunaticJsonRawDataDocument> S insert(S entity) {
        documents.add(entity);
        return null;
    }

    @Override
    public <S extends LunaticJsonRawDataDocument> List<S> insert(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public <S extends LunaticJsonRawDataDocument> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends LunaticJsonRawDataDocument> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends LunaticJsonRawDataDocument> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends LunaticJsonRawDataDocument> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends LunaticJsonRawDataDocument> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends LunaticJsonRawDataDocument> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends LunaticJsonRawDataDocument, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public List<LunaticJsonRawDataDocument> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<LunaticJsonRawDataDocument> findAll(Pageable pageable) {
        return null;
    }
}
