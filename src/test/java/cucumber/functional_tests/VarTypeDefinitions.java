package cucumber.functional_tests;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.controller.rest.responses.VarTypeController;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.variabletype.VariableTypeModel;
import fr.insee.genesis.domain.service.variabletype.VariableTypeService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.mappers.VariableTypeDocumentMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.VariableTypePersistanceStub;
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
import java.util.List;

public class VarTypeDefinitions {
    VariableTypePersistanceStub variableTypePersistanceStub = new VariableTypePersistanceStub();

    VarTypeController varTypeController = new VarTypeController(new VariableTypeService(variableTypePersistanceStub));

    String campaignId;
    Mode mode;
    private static final String DDI_REGEX = "ddi[\\w,\\s-]+\\.xml";
    public static final String S_S = "%s/%s";
    FileUtils fileUtils = new FileUtils(new ConfigStub());

    ResponseEntity<VariablesMap> response;

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
        variableTypePersistanceStub.getMongoStub().add(
                VariableTypeDocumentMapper.INSTANCE.modelToDocument(
                        VariableTypeModel.builder()
                                .campaignId(campaignId)
                                .questionnaireId(campaignId)
                                .mode(mode)
                                .variablesMap(variablesMap)
                                .build()
                )
        );
    }

    @When("We get metadata from database")
    public void get_metadata() {
        response = varTypeController.getVarType(campaignId,campaignId,mode);
    }


    @Then("There should be a variable {string} with type {string}")
    public void check_variable_type(String expectedVariableName, String expectedVariableType) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        VariablesMap variablesMap = response.getBody();
        Assertions.assertThat(variablesMap).isNotNull();
        Assertions.assertThat(variablesMap.getVariables()).isNotNull().containsKey(expectedVariableName);
        Assertions.assertThat(variablesMap.getVariables().get(expectedVariableName).getType()).isNotNull().asString().isEqualTo(expectedVariableType);
    }
}
