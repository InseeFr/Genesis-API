package cucumber.functional_tests;

import cucumber.TestConstants;
import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.controller.rest.responses.ApiError;
import fr.insee.genesis.controller.rest.responses.ResponseController;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistancePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MainDefinitions {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    String directory;
    Path inDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_WEB_DIRECTORY);
    final Path ddiDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_DDI_DIRECTORY);

    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();
    SurveyUnitPersistencePortStub surveyUnitPersistence = new SurveyUnitPersistencePortStub();
    static QuestionnaireMetadataPersistancePortStub questionnaireMetadataPersistancePortStub =
            new QuestionnaireMetadataPersistancePortStub();
    static QuestionnaireMetadataService questionnaireMetadataService =
            new QuestionnaireMetadataService(questionnaireMetadataPersistancePortStub);
    DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub =
            new DataProcessingContextPersistancePortStub();
    DataProcessingContextApiPort dataProcessingContextApiPort = new DataProcessingContextService(
            dataProcessingContextPersistancePortStub,
            surveyUnitPersistence
    );

    Config config = new ConfigStub();
    ResponseEntity<List<SurveyUnitModel>> surveyUnitModelResponse;
    ResponseEntity<Object> surveyUnitLatestStatesResponse;

    ResponseController responseController = new ResponseController(
            new SurveyUnitService(surveyUnitPersistence, new QuestionnaireMetadataService(questionnaireMetadataPersistancePortStub), new FileUtils(config)),
            surveyUnitQualityService,
            new FileUtils(config),
            new ControllerUtils(new FileUtils(config)),
            new AuthUtils(config),
            questionnaireMetadataService,
            dataProcessingContextApiPort
        );

    List<SurveyUnitModel> surveyUnitModels;

    //BEFOREs

    @Before
    public void init() {
        this.surveyUnitPersistence.getMongoStub().clear();
    }

    @Before("@NeedsLogPrepare")
    public void prepare_log_check(){
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    //GIVENs

    @Given("We have data in directory {string}")
    public void init_folder(String directory) throws IOException {
        this.directory = directory;
        inDirectory = inDirectory.resolve(directory);
        Files.createDirectories(inDirectory);
    }

    @Given("We copy data file {string} to that directory")
    public void copy_data_file(String dataFile) throws IOException {
        Path dataFilePath = Paths.get(TestConstants.TEST_RESOURCES_DIRECTORY).resolve(dataFile);
        Files.copy(dataFilePath, this.inDirectory.resolve(dataFilePath.getFileName().toString()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Given("We have a context in database for that data with review {string}")
    public void create_context(String withReviewString) {
        dataProcessingContextPersistancePortStub.getMongoStub().add(
                new DataProcessingContextDocument(
                        directory,
                        new ArrayList<>(),
                        Boolean.parseBoolean(withReviewString)
                )
        );
    }

    @Given("We have a context in database for partitionId {string} with review {string}")
    public void create_context(String partitionId, String withReviewString) {
        dataProcessingContextPersistancePortStub.getMongoStub().add(
                new DataProcessingContextDocument(
                        partitionId,
                        new ArrayList<>(),
                        Boolean.parseBoolean(withReviewString)
                )
        );
    }

    @Given("We have a survey unit with campaignId {string} and interrogationId {string}")
    public void create_light_surveyUnit(String campaignId, String interrogationId) {
        surveyUnitPersistence.getMongoStub().add(SurveyUnitModel.builder()
                        .campaignId(campaignId)
                        .interrogationId(interrogationId)
                .build());
    }

    //WHENs

    @When("We create survey unit models from file {string} with DDI {string}")
    public void get_models(String fileName, String ddiName) throws IOException, ParserConfigurationException,
            SAXException, GenesisException, MetadataParserException {
        Path filePath = inDirectory.resolve(fileName);
        Path ddiFilePath = ddiDirectory.resolve(directory).resolve(ddiName);

        if(fileName.endsWith(".xml")){
            LunaticXmlDataParser parser = new LunaticXmlDataParser();
            LunaticXmlCampaign campaign;
            campaign = parser.parseDataFile(filePath);
            List<QuestionnaireMetadataModel> questionnaireMetadataModels =
                    questionnaireMetadataPersistancePortStub.find(directory, Mode.WEB);
            if(questionnaireMetadataModels.isEmpty()){
                MetadataModel metadataModel = DDIReader.getMetadataFromDDI(
                        ddiFilePath.toFile().toURI().toURL().toString(),
                        new FileInputStream(ddiFilePath.toFile())
                );
                questionnaireMetadataPersistancePortStub.save(new QuestionnaireMetadataModel(
                        directory,
                        Mode.WEB,
                        metadataModel
                ));
                questionnaireMetadataModels = questionnaireMetadataPersistancePortStub.find(directory, Mode.WEB);
            }
            List<SurveyUnitModel> surveyUnitModels1 = new ArrayList<>();
            for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
                surveyUnitModels1.addAll(LunaticXmlAdapter.convert(su,
                        questionnaireMetadataModels.getFirst().metadataModel().getVariables(), campaign.getCampaignId(),
                        Mode.WEB));
            }
            surveyUnitQualityService.verifySurveyUnits(surveyUnitModels1,
                    questionnaireMetadataModels.getFirst().metadataModel().getVariables());
            surveyUnitModels = surveyUnitModels1;
        }
    }

    @When("We save data from that directory")
    public void get_su_models_from_folder() throws Exception {
        responseController.saveResponsesFromXmlCampaignFolder(this.inDirectory.getFileName().toString(), null);
    }

    @When("We allow review for that partition")
    public void set_review_to_true() {
        String partitionId = this.inDirectory.getFileName().toString();
        dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(
                dataProcessingContextDocument -> dataProcessingContextDocument.getPartitionId().equals(partitionId)
        ).toList().forEach(
                dataProcessingContextDocument -> dataProcessingContextDocument.setWithReview(true)
        );
    }

    @When("We delete that directory")
    public void delete_directory() throws IOException {
        org.springframework.util.FileSystemUtils.deleteRecursively(inDirectory);
    }

    @When("We extract survey unit data with questionnaireId {string} and interrogationId {string}")
    public void extract_survey_data(String questionnaireId, String interrogationId) {
        this.surveyUnitModelResponse = responseController.getLatestByInterrogation(interrogationId, questionnaireId.toUpperCase());
    }

    @When("We extract survey unit latest states with questionnaireId {string} and interrogationId {string}")
    public void extract_survey_unit_latest_states(String questionnaireId, String interrogationId){
        try {
            this.surveyUnitLatestStatesResponse =
                    responseController.findResponsesByInterrogationAndQuestionnaireLatestStates(interrogationId,
                    questionnaireId.toUpperCase());
        } catch (GenesisException e) {
            this.surveyUnitLatestStatesResponse = ResponseEntity.status(e.getStatus()).body(new ApiError(e.getMessage()));
        }
    }

    //THENs

    @Then("There should be {int} {string} SurveyUnit in database")
    public void check_surveyunits_by_state(int expectedCount, String expectedStatus) {
        Assertions.assertThat(this.surveyUnitPersistence.getMongoStub().stream().filter(
                surveyUnitModel -> surveyUnitModel.getState().name().equals(expectedStatus)
        ).toList()).hasSize(expectedCount);
    }



    @Then("For SurveyUnit {string} there should be at least one {string} SurveyUnit Model")
    public void check_expected_datastate_model(String interrogationId, String expectedDataState) {
        Assertions.assertThat(this.surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().toString().equals(expectedDataState)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).isNotEmpty();
    }

    @Then("For SurveyUnit {string} there shouldn't be a {string} SurveyUnit Model")
    public void check_unexpected_datastate_model(String interrogationId, String unexpectedDataState) {
        Assertions.assertThat(this.surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().toString().equals(unexpectedDataState)
                && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).isEmpty();
    }

    @Then("We should have a {string} Survey Unit model for survey unit {string} with {string} filled with {string} for iteration " +
            "{int}")
    public void check_survey_unit_model_content(String dataState, String interrogationId, String variableName,
                                              String expectedValue, int iteration) {
        //Get model
       List<SurveyUnitModel> concernedSurveyUnitModels = this.surveyUnitModels.stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().toString().equals(dataState)
                && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();

        Assertions.assertThat(concernedSurveyUnitModels).isNotEmpty();
        SurveyUnitModel concernedSurveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedCollectedVariables =
                concernedSurveyUnitModel.getCollectedVariables().stream().filter(variable ->
                    variable.varId().equals(variableName)
                    && variable.iteration().equals(iteration)
        ).toList();

        Assertions.assertThat(concernedCollectedVariables).isNotEmpty().hasSize(1);

        VariableModel concernedCollectedVariable = concernedCollectedVariables.getFirst();

        //Value assertion
        Assertions.assertThat(concernedCollectedVariable.value()).isEqualTo(expectedValue);
    }

    @Then("For collected variable {string} in survey unit {string} we should have {string} and scope {string} for " +
            "iteration {int}")
    public void check_collected_variable_content_in_mongo(
            String collectedVariableName,
            String interrogationId,
            String expectedValue,
            String expectedScope,
            Integer iteration
    ) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistence.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedCollectedVariables =
                surveyUnitModel.getCollectedVariables().stream().filter(variableModel ->
                        variableModel.varId().equals(collectedVariableName)
                                && Objects.equals(variableModel.scope(), expectedScope)
                                && variableModel.iteration().equals(iteration)
                ).toList();
        Assertions.assertThat(concernedCollectedVariables).hasSize(1);

        VariableModel variableModel = concernedCollectedVariables.getFirst();

        //Value content assertion
        Assertions.assertThat(variableModel.value()).isEqualTo(expectedValue);
    }

    @Then("For external variable {string} in survey unit {string} we should have {string} and scope {string} for " +
            "iteration {int}")
    public void check_external_variable_content_in_mongo(
            String externalVariableName,
            String interrogationId,
            String expectedValue,
            String expectedScope,
            Integer iteration
    ) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistence.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedExternalVariables =
                surveyUnitModel.getExternalVariables().stream().filter(variableModel ->
                variableModel.varId().equals(externalVariableName)
                        && Objects.equals(variableModel.scope(), expectedScope)
                        && variableModel.iteration().equals(iteration)
        ).toList();
        Assertions.assertThat(concernedExternalVariables).hasSize(1);

        VariableModel variableModel = concernedExternalVariables.getFirst();

        //Value content assertion
        Assertions.assertThat(variableModel.value()).isEqualTo(expectedValue);
    }

    @Then("If we get latest states for {string} in collected variable {string}, survey unit {string} we should have {string} for iteration {int}")
    public void check_latest_state_collected(String questionnaireId, String variableName, String interrogationId, String expectedValue, int iteration) throws GenesisException {
        ResponseEntity<Object> response =
                responseController.findResponsesByInterrogationAndQuestionnaireLatestStates(interrogationId, questionnaireId.toUpperCase());
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);

        SurveyUnitQualityToolDto surveyUnitQualityToolDto = (SurveyUnitQualityToolDto) response.getBody();

        List<VariableQualityToolDto> variableQualityToolDtos = surveyUnitQualityToolDto.getCollectedVariables().stream().filter(
                variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(variableName)
                        && variableQualityToolDto.getIteration().equals(iteration)
        ).toList();

        Assertions.assertThat(variableQualityToolDtos).hasSize(1);

        List<VariableStateDto> variableStateDtoList =
                variableQualityToolDtos.getFirst().getVariableStateDtoList().stream().filter(
                        variableStateDto -> variableStateDto.getState().equals(DataState.COLLECTED)
                ).toList();
        Assertions.assertThat(variableStateDtoList).hasSize(1);

        Assertions.assertThat(variableStateDtoList.getFirst().getValue()).isEqualTo(expectedValue);
    }

    @Then("If we get latest states for {string} in external variable {string}, survey unit {string} we should have {string} for iteration {int}")
    public void check_latest_state_external(String questionnaireId, String variableName, String interrogationId, String expectedValue, int iteration) throws GenesisException {
        ResponseEntity<Object> response =
                responseController.findResponsesByInterrogationAndQuestionnaireLatestStates(interrogationId, questionnaireId.toUpperCase());
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        SurveyUnitQualityToolDto surveyUnitQualityToolDto = (SurveyUnitQualityToolDto) response.getBody();

        List<VariableQualityToolDto> variableQualityToolDtos = surveyUnitQualityToolDto.getExternalVariables().stream().filter(
                variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(variableName)
                && variableQualityToolDto.getIteration().equals(iteration)
        ).toList();

        Assertions.assertThat(variableQualityToolDtos).hasSize(1);

        List<VariableStateDto> variableStateDtoList =
                variableQualityToolDtos.getFirst().getVariableStateDtoList().stream().filter(
                        variableStateDto -> variableStateDto.getState().equals(DataState.COLLECTED)
                ).toList();
        Assertions.assertThat(variableStateDtoList).hasSize(1);

        Assertions.assertThat(variableStateDtoList.getFirst().getValue()).isEqualTo(expectedValue);
    }

    @Then("The extracted survey unit data response should have a survey unit model with interrogationId {string}")
    public void check_su_model_interrogationId(String interrogationId) {
        Assertions.assertThat(surveyUnitModelResponse).isNotNull();
        Assertions.assertThat(surveyUnitModelResponse.getBody()).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitModelResponse.getBody().getFirst().getInterrogationId()).isEqualTo(interrogationId);
    }

    @Then("The extracted survey unit data response should have a survey unit model for interrogationId {string} with " +
            "{int} collected variables")
    public void check_su_model_collected_variables_volumetry(
            String interrogationId,
            int expectedVolumetry
    ){
        Assertions.assertThat(surveyUnitModelResponse).isNotNull();
        Assertions.assertThat(surveyUnitModelResponse.getBody()).isNotNull();

        List<SurveyUnitModel> filteredList = surveyUnitModelResponse.getBody().stream().filter(
                surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();

        Assertions.assertThat(filteredList)
                .isNotNull().hasSize(1);

        Assertions.assertThat(filteredList.getFirst().getCollectedVariables()).hasSize(expectedVolumetry);
    }

    @Then("The extracted survey unit data response should have a survey unit model for interrogationId {string} with " +
            "{int} external variables")
    public void check_su_model_external_variables_volumetry(
            String interrogationId,
            int expectedVolumetry
    ){
        Assertions.assertThat(surveyUnitModelResponse).isNotNull();
        Assertions.assertThat(surveyUnitModelResponse.getBody()).isNotNull();

        List<SurveyUnitModel> filteredList = surveyUnitModelResponse.getBody().stream().filter(
                surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();

        Assertions.assertThat(filteredList)
                .isNotNull().hasSize(1);

        Assertions.assertThat(filteredList.getFirst().getExternalVariables()).hasSize(expectedVolumetry);
    }

    @Then("The extracted survey unit latest states response should have a survey unit DTO has interrogationId " +
            "{string}" +
            " with {int} collected variables")
    public void check_su_latest_states_collected_variables_volumetry(String interrogationId, int expectedVolumetry) {
        SurveyUnitQualityToolDto response = (SurveyUnitQualityToolDto) surveyUnitLatestStatesResponse.getBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(surveyUnitLatestStatesResponse.getStatusCode().value()).isEqualTo(200);

        Assertions.assertThat(response.getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(response.getCollectedVariables()).hasSize(expectedVolumetry);
    }

    @Then("The extracted survey unit latest states response should have a survey unit DTO has interrogationId " +
            "{string}" +
            " with {int} external variables")
    public void check_su_latest_states_external_variables_volumetry(String interrogationId, int expectedVolumetry) {
        Assertions.assertThat(surveyUnitLatestStatesResponse).isNotNull();
        Assertions.assertThat(surveyUnitLatestStatesResponse.getBody()).isNotNull();
        SurveyUnitQualityToolDto surveyUnitQualityToolDto = (SurveyUnitQualityToolDto) surveyUnitLatestStatesResponse.getBody();

        Assertions.assertThat(surveyUnitQualityToolDto.getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(surveyUnitQualityToolDto.getExternalVariables()).hasSize(expectedVolumetry);
    }

    @Then("We shouldn't have any response for campaign {string}")
    public void check_response_not_present(String campaignId) {
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistence.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getCampaignId().equals(campaignId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).isEmpty();
    }

    @Then("We should have {string} in the logs")
    public void check_log(String expectedLogContent) {
        Assertions.assertThat(outputStreamCaptor.toString()).contains(expectedLogContent);
    }
    @Then("The response of get latest states should have {int} status code")
    public void check_latest_status_status_code(int expectedStatusCode) {
        Assertions.assertThat(surveyUnitLatestStatesResponse.getStatusCode().value()).isEqualTo(expectedStatusCode);
    }

    @Then("The extracted survey unit data latest states response dto should have a {string} collected variable named {string} with {string} as value for iteration {int}")
    public void check_latest_states_variable_type(String variableType, String expectedVariableName, String expectedValue, int iteration){
        Assertions.assertThat(surveyUnitLatestStatesResponse).isNotNull();
        Assertions.assertThat(surveyUnitLatestStatesResponse.getBody()).isNotNull();
        Assertions.assertThat(surveyUnitLatestStatesResponse.getStatusCode().value()).isEqualTo(200);

        SurveyUnitQualityToolDto surveyUnitQualityToolDto = (SurveyUnitQualityToolDto) surveyUnitLatestStatesResponse.getBody();
        List<VariableQualityToolDto> variableQualityToolDtos = surveyUnitQualityToolDto.getCollectedVariables().stream().filter(variable ->
                variable.getVariableName().equals(expectedVariableName)
                && variable.getIteration().equals(iteration)).toList();
        Assertions.assertThat(variableQualityToolDtos).hasSize(1);

        switch (variableType.toLowerCase()){
            case "integer" -> Assertions.assertThat(variableQualityToolDtos.getFirst().getVariableStateDtoList().getFirst().getValue()).isInstanceOf(Integer.class).isEqualTo(Integer.parseInt(expectedValue));
            case "float" -> Assertions.assertThat(variableQualityToolDtos.getFirst().getVariableStateDtoList().getFirst().getValue()).isInstanceOf(Float.class).isEqualTo(Float.parseFloat(expectedValue));
            case "boolean" -> Assertions.assertThat(variableQualityToolDtos.getFirst().getVariableStateDtoList().getFirst().getValue()).isInstanceOf(Boolean.class).isEqualTo(Boolean.parseBoolean(expectedValue));
            case "string" -> Assertions.assertThat(variableQualityToolDtos.getFirst().getVariableStateDtoList().getFirst().getValue()).isInstanceOf(String.class).isEqualTo(expectedValue);
            default -> Assertions.fail("incorrect variable type %s".formatted(variableType));
        }
    }
    //AFTERs
    @After
    public void clean() throws IOException {
        System.setOut(standardOut);
        //Move from DONE to IN
        Path doneDirectory = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "DONE");
        if (doneDirectory
                .resolve(inDirectory.getParent().getFileName())
                .resolve(inDirectory.getFileName()).toFile().exists()
        ){
            try (Stream<Path> stream = Files.list(doneDirectory
                    .resolve(inDirectory.getParent().getFileName())
                    .resolve(inDirectory.getFileName())
            )){
                for (Path filePath : stream.filter(path -> !path.toFile().isDirectory()).toList()) {
                    Files.copy(filePath, inDirectory.resolve(filePath.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        //Clean DONE test folder
        org.springframework.util.FileSystemUtils.deleteRecursively(doneDirectory);
    }

    @After("@NeedsLogPrepare")
    public void print_log(){
        System.setOut(standardOut);
        System.out.println(outputStreamCaptor);
    }
}
