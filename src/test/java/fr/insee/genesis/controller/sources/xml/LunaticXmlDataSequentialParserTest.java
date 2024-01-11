package fr.insee.genesis.controller.sources.xml;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

class LunaticXmlDataSequentialParserTest {

    static InputStream stream;

    static LunaticXmlDataSequentialParser parser;
    static LunaticXmlCampaign campaign;

    static LunaticXmlSurveyUnit surveyUnit;

    // Given + When
    @BeforeAll
    static void setUp() throws Exception {
        Path path = Path.of("src/test/resources/data_test_parser_xml.xml");
        stream = new FileInputStream(path.toFile());
        parser = new LunaticXmlDataSequentialParser(path, stream);

        campaign = parser.getCampaign();
        surveyUnit = parser.readNextSurveyUnit();
    }

    // Then

    @Test
    void campaignHasGoodLabelAndId() throws Exception {
        Assertions.assertThat(campaign.getLabel()).isEqualTo("Enquête Test Unitaire");
        Assertions.assertThat(campaign.getIdCampaign()).isEqualTo("TEST2023X01");
    }

    @Test
    void hasOneUE() throws Exception {
        Assertions.assertThat(surveyUnit).isNotNull();
        Assertions.assertThat(surveyUnit.getId()).isEqualTo("UE0000000001");
    }

    @Test
    void checkNumberOfCollectedVariables() throws Exception {
        Assertions.assertThat(surveyUnit.getData().getCollected()).hasSize(26);
    }

    @Test
    void checkNumberOfExternalVariables() throws Exception {
        Assertions.assertThat(surveyUnit.getData().getExternal()).hasSize(11);
    }

    @Test
    void checkCollectedVariableValues(){
        Assertions.assertThat(surveyUnit.getData().getCollected().get(0)).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getCollected().get(0).getCollected()).isEmpty();

        Assertions.assertThat(surveyUnit.getData().getCollected().get(1)).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getCollected().get(1).getCollected()).isNotEmpty();
        Assertions.assertThat(surveyUnit.getData().getCollected().get(1).getCollected()).hasSize(2);
        Assertions.assertThat(surveyUnit.getData().getCollected().get(1).getCollected().get(0).getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getCollected().get(1).getCollected().get(0).getValue()).isEqualTo("012");

        Assertions.assertThat(surveyUnit.getData().getCollected().get(4)).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getCollected().get(4).getCollected()).hasSize(1);
        Assertions.assertThat(surveyUnit.getData().getCollected().get(4).getCollected().get(0).getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getCollected().get(4).getCollected().get(0).getValue()).isEqualTo("2");
    }

    @Test
    void checkExternalVariableValue(){
        Assertions.assertThat(surveyUnit.getData().getExternal().get(0)).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getExternal().get(0).getValues()).isNotEmpty();
        Assertions.assertThat(surveyUnit.getData().getExternal().get(0).getValues()).hasSize(1);
        Assertions.assertThat(surveyUnit.getData().getExternal().get(0).getValues().get(0).getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getExternal().get(0).getValues().get(0).getValue()).isEqualTo("BOB");
    }

    @AfterAll
    static void closeStream() throws IOException {
        stream.close();
    }


}
