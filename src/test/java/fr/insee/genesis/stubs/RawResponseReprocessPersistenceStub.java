package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RawResponseReprocessPersistenceStub implements RawResponseReprocessPersistencePort {

    List<LunaticJsonRawDataDocument> mongoStub = new ArrayList<>();

    @Override
    public Set<String> findProcessedInterrogationIdsByCollectionInstrumentId(String questionnaireId) {
        return Set.of();
    }

    @Override
    public Set<String> findProcessedInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(String questionnaireId, LocalDateTime sinceDate, LocalDateTime endDate) {
        return Set.of();
    }

    @Override
    public void resetProcessDates(String questionnaireId, Set<String> interrogationIds) {
        for (int i = 0; i < mongoStub.size(); i++) {
            LunaticJsonRawDataDocument document = mongoStub.get(i);

            if (document.questionnaireId().equals(questionnaireId)
                    && interrogationIds.contains(document.interrogationId())) {

                LunaticJsonRawDataDocument newDocument = LunaticJsonRawDataDocument.builder()
                        .id(document.id())
                        .campaignId(document.campaignId())
                        .questionnaireId(document.questionnaireId())
                        .interrogationId(document.interrogationId())
                        .idUE(document.idUE())
                        .mode(document.mode())
                        .data(document.data())
                        .recordDate(document.recordDate())
                        .processDate(null)
                        .build();

                mongoStub.set(i, newDocument);
            }
        }
    }

}
