package cucumber.functional_tests;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.controller.rest.responses.SurveyMetadataController;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.surveymetadata.SurveyMetadataService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.surveymetadata.SurveyMetadataDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyMetadataDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.VariableDocumentMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.SurveyMetadataPersistanceStub;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SurveyMetadataDefinitions {
    SurveyMetadataPersistanceStub surveyMetadataPersistanceStub = new SurveyMetadataPersistanceStub();

    SurveyMetadataController surveyMetadataController = new SurveyMetadataController(new SurveyMetadataService(surveyMetadataPersistanceStub));

    String campaignId;
    Mode mode;
    private static final String DDI_REGEX = "ddi[\\w,\\s-]+\\.xml";
    public static final String S_S = "%s/%s";
    FileUtils fileUtils = new FileUtils(new ConfigStub());

    ResponseEntity<SurveyMetadataModel> response;

    @Given("We import metadata in database from {string} spec folder")
    public void import_metadatas(String specDirectory) throws IOException, GenesisException {
        ControllerUtils controllerUtils = new ControllerUtils(new FileUtils(new ConfigStub()));
        List<Mode> modesList = controllerUtils.getModesList(specDirectory, null);
        Assertions.assertThat(modesList).isNotEmpty();
        mode = modesList.getFirst();

        VariablesMap variablesMap;
        //DDI first, then lunatic
        try {
            Path ddiFilePath = fileUtils.findFile(String.format(S_S, fileUtils.getSpecFolder(specDirectory),
                    mode.getModeName()), DDI_REGEX);
            variablesMap = DDIReader.getMetadataFromDDI(ddiFilePath.toUri().toURL().toString(),
                    new FileInputStream(ddiFilePath.toString())).getVariables();
        } catch (MetadataParserException |FileNotFoundException e) {
            Path lunaticFilePath = fileUtils.findFile(String.format(S_S, fileUtils.getSpecFolder(specDirectory),
                    mode.getModeName()), "lunatic[\\w," + "\\s-]+\\.json");
            variablesMap = LunaticReader.getMetadataFromLunatic(new FileInputStream(lunaticFilePath.toString())).getVariables();
        }
        Assertions.assertThat(variablesMap).isNotNull();

        campaignId = specDirectory;

        SurveyMetadataDocument surveyMetadataDocument = SurveyMetadataDocumentMapper.INSTANCE.modelToDocument(
                SurveyMetadataModel.builder()
                        .campaignId(campaignId)
                        .questionnaireId(campaignId)
                        .mode(mode)
                        .variableDocumentMap(new LinkedHashMap<>())
                        .build()
        );

        for(Map.Entry<String, Variable> variable : variablesMap.getVariables().entrySet()){
            surveyMetadataDocument.getVariableDefinitions().put(
                    variable.getKey(),
                    VariableDocumentMapper.INSTANCE.bpmToDocument(variable.getValue())
            );
        }

        surveyMetadataPersistanceStub.getMongoStub().add(surveyMetadataDocument);
    }

    @When("We get metadata from database")
    public void get_metadata() {
        response = surveyMetadataController.getSurveyMetadata(campaignId,campaignId,mode);
    }


    @Then("There should be a variable {string} with type {string}")
    public void check_variable_type(String expectedVariableName, String expectedVariableType) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SurveyMetadataModel surveyMetadataModel = response.getBody();
        Assertions.assertThat(surveyMetadataModel).isNotNull();
        Assertions.assertThat(surveyMetadataModel.variableDocumentMap()).isNotNull().containsKey(expectedVariableName);
        Assertions.assertThat(surveyMetadataModel.variableDocumentMap().get(expectedVariableName).getType()).isNotNull().asString().isEqualTo(expectedVariableType);
    }
}
