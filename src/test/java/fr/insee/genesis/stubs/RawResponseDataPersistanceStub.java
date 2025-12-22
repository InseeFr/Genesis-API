package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import fr.insee.genesis.infrastructure.mappers.RawResponseDocumentMapper;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class RawResponseDataPersistanceStub implements RawResponsePersistencePort {
    List<RawResponseDocument> mongoStub = new ArrayList<>();

    @Override
    public List<RawResponse> findRawResponses(String collectionInstrumentId, Mode mode, List<String> interrogationIdList) {
        return List.of();
    }

    @Override
    public void updateProcessDates(String collectionInstrumentId, Set<String> interrogationIds) {

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
    public Page<RawResponse> findByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable) {
        List<RawResponseDocument> foundRaws = mongoStub.stream()
                .filter(rawData -> rawData.campaignId().equals(campaignId))
                .toList();
        return new PageImpl<>(RawResponseDocumentMapper.INSTANCE.listDocumentToListModel(foundRaws),
                pageable,
                foundRaws.size());
    }
}
