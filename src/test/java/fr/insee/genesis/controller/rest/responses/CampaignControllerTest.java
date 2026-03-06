package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CampaignControllerTest {

    @Mock
    private SurveyUnitApiPort surveyUnitApiPort;

    @InjectMocks
    private CampaignController campaignController;

    @Test
    void getCampaignsWithQuestionnaires_test() {
        //WHEN
        campaignController.getCampaignsWithQuestionnaires();

        //THEN
        verify(surveyUnitApiPort, times(1)).findCampaignsWithQuestionnaires();
    }
}