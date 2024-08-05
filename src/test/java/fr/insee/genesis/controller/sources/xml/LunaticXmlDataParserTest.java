package fr.insee.genesis.controller.sources.xml;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class LunaticXmlDataParserTest {

    static LunaticXmlCampaign campaign;

    @BeforeAll
    static void setUp() throws Exception {
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        Path path = Path.of("src/test/resources/data_test_parser_xml.xml");
        campaign = parser.parseDataFile(path);
    }

    @Test
    void campaignHasGoodLabelAndId()  {
        Assertions.assertThat(campaign.getLabel()).isEqualTo("Enquête Test Unitaire");
        Assertions.assertThat(campaign.getIdCampaign()).isEqualTo("TEST2023X01");
    }

    @Test
    void campaignHasOneUE() {
        Assertions.assertThat(campaign.getSurveyUnits()).hasSize(1);
    }

    @Test
    void checkNumberOfCollectedVariables() {
        Assertions.assertThat(campaign.getSurveyUnits().getFirst().getData().getCollected()).hasSize(26);
    }

    @Test
    void checkNumberOfExternalVariables()  {
        Assertions.assertThat(campaign.getSurveyUnits().getFirst().getData().getExternal()).hasSize(11);
    }


}
