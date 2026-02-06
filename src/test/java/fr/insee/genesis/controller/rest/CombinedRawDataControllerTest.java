package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.dto.rawdata.CombinedRawDataDto;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.service.rawdata.CombinedRawDataService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CombinedRawDataControllerTest {

    private CombinedRawDataService combinedRawDataService;

    private CombinedRawDataController combinedRawDataController;

    @BeforeEach
    void setUp() {
        combinedRawDataService = mock(CombinedRawDataService.class);
        combinedRawDataController = new CombinedRawDataController(
                combinedRawDataService
        );
    }

    @Test
    void getCombinedRawData_test() {
        //GIVEN
        String interrogationId = "test";

        CombinedRawDataDto combinedRawDataDto = new CombinedRawDataDto(
                List.of(
                        RawResponseModel.builder().build()
                ),
                List.of(
                        LunaticJsonRawDataModel.builder().build()
                )
        );
        doReturn(combinedRawDataDto).when(combinedRawDataService).getCombinedRawDataByInterrogationId(any());

        //WHEN
        ResponseEntity<CombinedRawDataDto> response = combinedRawDataController.getCombinedRawData(interrogationId);

        //THEN
        verify(combinedRawDataService, times(1))
                .getCombinedRawDataByInterrogationId(interrogationId);
        Assertions.assertThat(response.getBody()).isEqualTo(combinedRawDataDto);
    }

    @Test
    void getCombinedRawData_not_found_test() {
        //GIVEN
        String interrogationId = "test";

        CombinedRawDataDto combinedRawDataDto = new CombinedRawDataDto(
                new ArrayList<>(), new ArrayList<>()
        );
        doReturn(combinedRawDataDto).when(combinedRawDataService).getCombinedRawDataByInterrogationId(any());

        //WHEN
        ResponseEntity<CombinedRawDataDto> response = combinedRawDataController.getCombinedRawData(interrogationId);

        //THEN
        verify(combinedRawDataService, times(1))
                .getCombinedRawDataByInterrogationId(interrogationId);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}