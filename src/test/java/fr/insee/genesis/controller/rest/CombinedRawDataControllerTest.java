package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.dto.rawdata.CombinedRawDataDto;
import fr.insee.genesis.controller.utils.platine.PlatinePermissionHelper;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.service.rawdata.CombinedRawDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CombinedRawDataControllerStandaloneTest {

    private CombinedRawDataService combinedRawDataService;
    private PlatinePermissionHelper platinePermissionHelper;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        combinedRawDataService = mock(CombinedRawDataService.class);
        platinePermissionHelper = mock(PlatinePermissionHelper.class);

        CombinedRawDataController controller =
                new CombinedRawDataController(combinedRawDataService, platinePermissionHelper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    @DisplayName("Should return 403 when PlatinePermissionHelper denies permission")
    void shouldReturn403_whenPermissionDenied() throws Exception {
        when(platinePermissionHelper.hasExportDataPermission("INT-1")).thenReturn(false);

        mockMvc.perform(get("/combined-raw-data")
                        .param("interrogationId", "INT-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(platinePermissionHelper).hasExportDataPermission("INT-1");
        verifyNoInteractions(combinedRawDataService);
    }

    @Test
    @DisplayName("Should return 404 when service returns empty rawResponseModels")
    void shouldReturn404_whenEmptyRawResponses() throws Exception {
        when(platinePermissionHelper.hasExportDataPermission("INT-1")).thenReturn(true);

        CombinedRawDataDto dto = new CombinedRawDataDto(
                List.of(),  // rawResponseModels empty => 404
                List.of()
        );

        when(combinedRawDataService.getCombinedRawDataByInterrogationId("INT-1"))
                .thenReturn(dto);

        mockMvc.perform(get("/combined-raw-data")
                        .param("interrogationId", "INT-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(platinePermissionHelper).hasExportDataPermission("INT-1");
        verify(combinedRawDataService).getCombinedRawDataByInterrogationId("INT-1");
        verifyNoMoreInteractions(combinedRawDataService);
    }

    @Test
    @DisplayName("Should return 200 and JSON body when rawResponseModels is not empty")
    void shouldReturn200_whenNonEmptyRawResponses() throws Exception {
        when(platinePermissionHelper.hasExportDataPermission("INT-1")).thenReturn(true);

        RawResponseModel raw = mock(RawResponseModel.class);
        LunaticJsonRawDataModel lunatic = mock(LunaticJsonRawDataModel.class);

        CombinedRawDataDto dto = new CombinedRawDataDto(
                List.of(raw),
                List.of(lunatic)
        );

        when(combinedRawDataService.getCombinedRawDataByInterrogationId("INT-1"))
                .thenReturn(dto);

        mockMvc.perform(get("/combined-raw-data")
                        .param("interrogationId", "INT-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // on vérifie la structure : 2 champs, et tailles des tableaux
                .andExpect(jsonPath("$.rawResponseModels").isArray())
                .andExpect(jsonPath("$.rawResponseModels.length()").value(1))
                .andExpect(jsonPath("$.lunaticRawDataModels").isArray())
                .andExpect(jsonPath("$.lunaticRawDataModels.length()").value(1));

        verify(platinePermissionHelper).hasExportDataPermission("INT-1");
        verify(combinedRawDataService).getCombinedRawDataByInterrogationId("INT-1");
        verifyNoMoreInteractions(combinedRawDataService);
    }
}
