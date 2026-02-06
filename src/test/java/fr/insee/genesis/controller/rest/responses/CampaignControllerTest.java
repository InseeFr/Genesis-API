package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CampaignControllerTest {
    private SurveyUnitApiPort surveyUnitApiPort;
    private CampaignController campaignController;

    @BeforeEach
    void setUp() {
        surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        campaignController = new CampaignController(
                surveyUnitApiPort
        );
    }

    @Test
    void getCampaignsWithQuestionnaires_test() {
        //WHEN
        campaignController.getCampaignsWithQuestionnaires();

        //THEN
        verify(surveyUnitApiPort, times(1)).findCampaignsWithQuestionnaires();
    }
}