package fr.insee.genesis.controller.sources.xml;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
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
    @BeforeEach
    void setUp() throws Exception {
        Path path = Path.of("src/test/resources/data_test_parser_xml.xml");
        stream = new FileInputStream(path.toFile());
        parser = new LunaticXmlDataSequentialParser(path, stream);

        campaign = parser.getCampaign();
        surveyUnit = parser.readNextSurveyUnit();
    }

    // Then

    @Test
    void campaignHasGoodLabelAndId()  {
        Assertions.assertThat(campaign.getLabel()).isEqualTo("Enquête Test Unitaire");
        Assertions.assertThat(campaign.getIdCampaign()).isEqualTo("TEST2023X01");
    }

    @Test
    void hasOneUE()  {
        Assertions.assertThat(surveyUnit).isNotNull();
        Assertions.assertThat(surveyUnit.getId()).isEqualTo("UE0000000001");
        Assertions.assertThat(surveyUnit.getQuestionnaireModelId()).isEqualTo("TEST2023X01");
    }

    @Test
    void checkNumberOfCollectedVariables()  {
        Assertions.assertThat(surveyUnit.getData().getCollected()).hasSize(26);
    }

    @Test
    void checkNumberOfExternalVariables()  {
        Assertions.assertThat(surveyUnit.getData().getExternal()).hasSize(11);
    }

    @Test
    void checkCollectedVariableValues(){
        Assertions.assertThat(surveyUnit.getData().getCollected().get(0)).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getCollected().get(0).getCollected()).isEmpty();

        Assertions.assertThat(surveyUnit.getData().getCollected().get(1)).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getCollected().get(1).getCollected()).isNotEmpty();
        Assertions.assertThat(surveyUnit.getData().getCollected().get(1).getCollected()).hasSize(2);
        Assertions.assertThat(surveyUnit.getData().getCollected().get(1).getCollected().getFirst().getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getCollected().get(1).getCollected().getFirst().getValue()).isEqualTo("012");

        Assertions.assertThat(surveyUnit.getData().getCollected().get(4)).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getCollected().get(4).getCollected()).hasSize(1);
        Assertions.assertThat(surveyUnit.getData().getCollected().get(4).getCollected().getFirst().getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getCollected().get(4).getCollected().getFirst().getValue()).isEqualTo("2");
    }

    @Test
    void checkExternalVariableValue(){
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues()).isNotEmpty();
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues()).hasSize(1);
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues().getFirst().getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues().getFirst().getValue()).isEqualTo("BOB");
    }

    @Test
    void checkExternalVariableValue_loops() throws IOException, XMLStreamException {
        //Given
        Path path = Path.of("src/test/resources/data_test_parser_xml_external_loops.xml");
        stream = new FileInputStream(path.toFile());
        parser = new LunaticXmlDataSequentialParser(path, stream);

        //When
        campaign = parser.getCampaign();
        surveyUnit = parser.readNextSurveyUnit();

        //Then
        //Not null & volumetry
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues()).isNotEmpty();
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues()).hasSize(3);

        //Content
        //1
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues().getFirst().getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues().getFirst().getValue()).isEqualTo("BOB1");
        //2
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues().get(1).getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues().get(1).getValue()).isEqualTo("BOB2");
        //3
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues().get(2).getType()).isEqualTo("string");
        Assertions.assertThat(surveyUnit.getData().getExternal().getFirst().getValues().get(2).getValue()).isEqualTo("BOB3");
    }

    @AfterEach
    void closeStream() throws IOException {
        stream.close();
    }


}
