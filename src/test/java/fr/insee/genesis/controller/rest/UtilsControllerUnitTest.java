package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.volumetry.VolumetryLogService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UtilsControllerUnitTest {

    static UtilsController utilsController;
    static LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    static RawResponseApiPort rawResponseApiPort;
    static SurveyUnitApiPort surveyUnitApiPort;
    static VolumetryLogService volumetryLogService;

    @BeforeEach
    void setUp() {
        lunaticJsonRawDataApiPort = mock(LunaticJsonRawDataApiPort.class);
        rawResponseApiPort = mock(RawResponseApiPort.class);
        volumetryLogService = mock(VolumetryLogService.class);
        surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        utilsController = new UtilsController(
                volumetryLogService,
                surveyUnitApiPort,
                lunaticJsonRawDataApiPort,
                rawResponseApiPort
        );
    }

    @Test
    @SneakyThrows
    void saveVolumetry_test() {
        //WHEN
        utilsController.saveVolumetry();

        //THEN
        verify(volumetryLogService, times(1)).writeVolumetries(surveyUnitApiPort);
        verify(volumetryLogService, times(1)).writeRawDataVolumetries(
                lunaticJsonRawDataApiPort, rawResponseApiPort
        );
        verify(volumetryLogService, times(1)).cleanOldFiles();
    }
}