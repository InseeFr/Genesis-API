package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import fr.insee.genesis.infrastructure.mappers.RawResponseDocumentMapper;
import fr.insee.modelefiliere.ModeDto;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class RawResponseDataPersistanceStub implements RawResponsePersistencePort {
    List<RawResponseDocument> mongoStub = new ArrayList<>();

    @Override
    public List<RawResponseModel> findRawResponses(String collectionInstrumentId, Mode mode, List<String> interrogationIdList) {
        return RawResponseDocumentMapper.INSTANCE.listDocumentToListModel(mongoStub.stream().filter(
                doc -> doc.collectionInstrumentId().equals(collectionInstrumentId)
                        && Mode.valueOf(doc.mode()).equals(mode)
                        && interrogationIdList.contains(doc.interrogationId())
        ).toList());
    }

    @Override
    public List<RawResponseModel> findRawResponsesByInterrogationID(String interrogationId) {
        return RawResponseDocumentMapper.INSTANCE.listDocumentToListModel(mongoStub.stream().filter(
                doc -> doc.interrogationId().equals(interrogationId)
        ).toList());
    }

    @Override
    public void updateProcessDates(String collectionInstrumentId, Set<String> interrogationIds) {
        return;
    }

    @Override
    public List<String> getUnprocessedCollectionIds() {
        return List.of();
    }

    @Override
    public Set<String> findUnprocessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId) {
        return Set.of();
    }

    @Override
    public List<ModeDto> findModesByCollectionInstrument(String collectionInstrumentId) {
        List<ModeDto> modes = new ArrayList<>();
        mongoStub.stream().filter(
                doc -> doc.collectionInstrumentId().equals(collectionInstrumentId)
        ).forEach(
                rawResponseDocument -> modes.add(ModeDto.valueOf(rawResponseDocument.mode()))
        );
        return modes;
    }

    @Override
    public Page<RawResponseModel> findByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable) {
        List<RawResponseDocument> foundRaws = mongoStub.stream()
                .filter(rawData -> rawData.campaignId().equals(campaignId))
                .toList();
        return new PageImpl<>(RawResponseDocumentMapper.INSTANCE.listDocumentToListModel(foundRaws),
                pageable,
                foundRaws.size());
    }

    @Override
    public long countByCollectionInstrumentId(String collectionInstrumentId) {
        return 0;
    }
    @Override
    public Set<String> findDistinctCollectionInstrumentIds() {
        return new HashSet<>();
    }
}
