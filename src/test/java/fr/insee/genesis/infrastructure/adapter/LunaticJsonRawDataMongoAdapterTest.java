package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.stubs.LunaticJsonMongoDBRepositoryStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.time.LocalDateTime;
import java.util.List;


class LunaticJsonRawDataMongoAdapterTest {

    private LunaticJsonMongoDBRepositoryStub repository;
    @InjectMocks
    private LunaticJsonRawDataMongoAdapter adapter;
    private LunaticJsonRawDataModel rawData;
    private LunaticJsonRawDataDocument doc;

    @BeforeEach
    void setUp() {
        repository = new LunaticJsonMongoDBRepositoryStub();
        adapter = new LunaticJsonRawDataMongoAdapter(repository, null);
        rawData = LunaticJsonRawDataModel.builder()
                .campaignId("campaign01")
                .questionnaireId("questionnaire01")
                .interrogationId("interrogation01")
                .idUE("idUE01")
                .mode(Mode.WEB)
                .build();
        doc = LunaticJsonRawDataDocument.builder()
                .campaignId("campaign01")
                .questionnaireId("questionnaire01")
                .interrogationId("interrogation01")
                .idUE("idUE01")
                .mode(Mode.WEB)
                .build();
    }

    @Test
    void testSave() {
        //WHEN
        adapter.save(rawData);
        //THEN
        Assertions.assertThat(repository.getDocuments()).hasSize(1);
    }

    @Test
    void getAllUnprocessedDataTest(){
        //WHEN
        repository.getDocuments().add(doc);
        LunaticJsonRawDataDocument rawData2 = LunaticJsonRawDataDocument.builder()
                .campaignId("campaign01")
                .questionnaireId("questionnaire01")
                .interrogationId("interrogation01")
                .idUE("idUE01")
                .mode(Mode.WEB)
                .processDate(LocalDateTime.now())
                .build();
        repository.getDocuments().add(rawData2);
        //THEN
        Assertions.assertThat(adapter.getAllUnprocessedData()).hasSize(1);
    }

    @Test
    void findRawDataTest(){
        //WHEN
        repository.getDocuments().add(doc);
        //THEN
        List<LunaticJsonRawDataModel> rawdatas = adapter.findRawData("campaign01",Mode.WEB,List.of("interrogation01"));
        Assertions.assertThat(rawdatas).hasSize(1);
    }

}