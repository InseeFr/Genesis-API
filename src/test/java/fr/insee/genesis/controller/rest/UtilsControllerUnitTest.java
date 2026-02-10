package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.volumetry.VolumetryLogService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UtilsControllerUnitTest {

    @Mock
    private static LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    @Mock
    private static RawResponseApiPort rawResponseApiPort;
    @Mock
    private static SurveyUnitApiPort surveyUnitApiPort;
    @Mock
    private static VolumetryLogService volumetryLogService;

    @InjectMocks
    private static UtilsController utilsController;

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