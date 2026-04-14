package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistenceRouter;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.exceptions.InvalidDateIntervalException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LunaticRawDataReprocessServiceTest {

    private ReprocessRawResponseService reprocessRawResponseService;

    @BeforeEach
    void freshStart() {
        RawResponseReprocessPersistenceRouter rawResponseReprocessPersistenceRouter =
                mock(RawResponseReprocessPersistenceRouter.class);
        when(rawResponseReprocessPersistenceRouter.resolve(any()))
                .thenReturn(mock(RawResponseReprocessPersistencePort.class));
        reprocessRawResponseService = new ReprocessRawResponseService(
                mock(SurveyUnitPersistencePort.class),
                null,
                mock(LunaticJsonRawDataService.class),
                rawResponseReprocessPersistenceRouter
        );
    }

    @Test
    void reprocessRawData_should_return_empty_result_when_no_processed_interrogation_ids_found() throws Exception {
        // GIVEN
        String questionnaireId = "TESTIDQUEST";

        // WHEN
        DataProcessResult result = reprocessRawResponseService.reprocessRawResponses(
                RawDataModelType.LEGACY, questionnaireId, null, null);

        // THEN
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.dataCount()).isZero();
        Assertions.assertThat(result.formattedDataCount()).isZero();
        Assertions.assertThat(result.errors()).isEmpty();
    }

    @Test
    void reprocessRawData_should_throw_when_endDate_is_provided_without_sinceDate() {
        String questionnaireId = "TESTIDQUEST";
        Instant endDate = Instant.now();

        Assertions.assertThatThrownBy(() ->
                        reprocessRawResponseService.reprocessRawResponses(
                                RawDataModelType.LEGACY, questionnaireId, null, endDate))
                .isInstanceOf(InvalidDateIntervalException.class)
                .hasMessage("'endDate' cannot be provided without 'sinceDate'.");
    }

    @Test
    void reprocessRawData_should_throw_when_endDate_is_before_sinceDate() {
        String questionnaireId = "TESTIDQUEST";
        Instant sinceDate = LocalDateTime.of(2024, 1, 10, 10, 0).toInstant(ZoneOffset.UTC);
        Instant endDate = LocalDateTime.of(2024, 1, 9, 10, 0).toInstant(ZoneOffset.UTC);

        Assertions.assertThatThrownBy(() ->
                reprocessRawResponseService.reprocessRawResponses(
                        RawDataModelType.LEGACY, questionnaireId, sinceDate, endDate))
                .isInstanceOf(InvalidDateIntervalException.class)
                .hasMessage("'endDate' value cannot be before 'sinceDate'.");
    }

}
