package cucumber.functional_tests;

public class RawDataDefinitions {
/*    LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = new LunaticJsonRawDataService(lunaticJsonRawDataPersistanceStub);
    Config config = new ConfigStub();
    FileUtils fileUtils = new FileUtils(config);
    SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();
    RawResponseController rawResponseController = new RawResponseController(
            lunaticJsonRawDataApiPort,
            new ControllerUtils(fileUtils),
            new MetadataService(),
            new SurveyUnitService(surveyUnitPersistencePortStub),
            surveyUnitQualityService,
            fileUtils
    );
    Path rawDataFilePath;
    ResponseEntity<Object> response;

    @Before
    public void init(){
        this.lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
    }


    @Given("We have raw data file in {string}")
    public void set_input_file(String rawDataFile){
        this.rawDataFilePath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,rawDataFile);
    }

    @When("We save that raw data for web campaign {string}, questionnaire {string}, interrogation {string}")
    public void save_raw_data(String campaignId, String questionnaireId, String interrogationId) throws IOException {
        if(rawDataFilePath == null){
            throw new RuntimeException("Raw data file path is null !");
        }

        response = rawResponseController.saveRawResponsesFromJsonBody(
                campaignId,
                questionnaireId,
                interrogationId,
                null,
                Mode.WEB,
                Files.readString(rawDataFilePath)
        );
    }

    @When("We process raw data for campaign {string}, questionnaire {string} and interrogation {string}")
    public void process_raw_data(
            String campaignId,
            String questionnaireId,
            String interrogationId

    ) {
        List<String> interrogationIdList = Collections.singletonList(interrogationId);

        response = rawResponseController.processJsonRawData(
                campaignId,
                questionnaireId,
                interrogationIdList
        );
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Then("We should have {int} raw data document")
    public void check_document_count(int expectedCount){
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(expectedCount);
    }

    @Then("We should have {int} collected variables and {int} " +
            "external variables for campaign {string}, questionnaire {string}, interrogation {string}")
    public void check_variables_count(
            int expectedCollectedVariablesCount,
            int expectedExternalVariablesCount,
            String campaignId,
            String questionnaireId,
            String interrogationId
    ){
        List<LunaticJsonDataDocument> lunaticJsonDataDocuments = lunaticJsonRawDataPersistanceStub.getMongoStub()
                .stream().filter(
                        lunaticJsonDataDocument -> lunaticJsonDataDocument.campaignId().equals(campaignId)
                        && lunaticJsonDataDocument.questionnaireId().equals(questionnaireId)
                        && lunaticJsonDataDocument.interrogationId().equals(interrogationId)
                ).toList();

        Assertions.assertThat(lunaticJsonDataDocuments).hasSize(1);
        Assertions.assertThat(lunaticJsonDataDocuments.getFirst().data().collectedVariables()).hasSize(expectedCollectedVariablesCount);
        Assertions.assertThat(lunaticJsonDataDocuments.getFirst().data().externalVariables()).hasSize(expectedExternalVariablesCount);
    }

    @Then("For {string} collected variable, state {string}, campaign {string}, questionnaire {string}, interrogation " +
            "{string} we should have {string} as value number {int} in array")
    public void check_collected_variable_content_array(
            String variableName,
            String datastate,
            String campaignId,
            String questionnaireId,
            String interrogationId,
            String expectedValue,
            int index
    ) {
        List<LunaticJsonDataDocument> lunaticJsonDataDocuments = lunaticJsonRawDataPersistanceStub.getMongoStub()
                .stream().filter(
                        lunaticJsonDataDocument -> lunaticJsonDataDocument.campaignId().equals(campaignId)
                                && lunaticJsonDataDocument.questionnaireId().equals(questionnaireId)
                                && lunaticJsonDataDocument.interrogationId().equals(interrogationId)
                ).toList();


        Assertions.assertThat(lunaticJsonDataDocuments).hasSize(1);
        Assertions.assertThat(lunaticJsonDataDocuments.getFirst().data().collectedVariables()).containsKey(variableName);

        LunaticJsonRawDataCollectedVariable lunaticJsonRawDataCollectedVariable =
                lunaticJsonDataDocuments.getFirst().data().collectedVariables().get(variableName);
        Assertions.assertThat(lunaticJsonRawDataCollectedVariable.collectedVariableByStateMap()).containsKey(DataState.valueOf(datastate));

        LunaticJsonRawDataVariable lunaticJsonRawDataVariable =
                lunaticJsonRawDataCollectedVariable.collectedVariableByStateMap().get(DataState.valueOf(datastate));

        Assertions.assertThat(lunaticJsonRawDataVariable.value()).isNull();
        Assertions.assertThat(lunaticJsonRawDataVariable.valuesArray()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataVariable.valuesArray().get(index)).isNotNull().isEqualTo(expectedValue);
    }

    @Then("For {string} external variable, campaign {string}, questionnaire {string}, interrogation {string} we " +
            "should have {string} as value number {int} in array")
    public void check_external_variable_content_array(
            String variableName,
            String campaignId,
            String questionnaireId,
            String interrogationId,
            String expectedValue,
            int index
    ) {
        List<LunaticJsonDataDocument> lunaticJsonDataDocuments = lunaticJsonRawDataPersistanceStub.getMongoStub()
                .stream().filter(
                        lunaticJsonDataDocument -> lunaticJsonDataDocument.campaignId().equals(campaignId)
                                && lunaticJsonDataDocument.questionnaireId().equals(questionnaireId)
                                && lunaticJsonDataDocument.interrogationId().equals(interrogationId)
                ).toList();

        Assertions.assertThat(lunaticJsonDataDocuments).hasSize(1);
        Assertions.assertThat(lunaticJsonDataDocuments.getFirst().data().externalVariables()).containsKey(variableName);

        LunaticJsonRawDataVariable lunaticJsonRawDataVariable =
                lunaticJsonDataDocuments.getFirst().data().externalVariables().get(variableName);

        Assertions.assertThat(lunaticJsonRawDataVariable.value()).isNull();
        Assertions.assertThat(lunaticJsonRawDataVariable.valuesArray()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataVariable.valuesArray().get(index)).isNotNull().isEqualTo(expectedValue);
    }

    @Then("For {string} external variable, campaign {string}, questionnaire {string}, interrogation {string} we should have {string} as value")
    public void check_external_variable_content_no_array(
            String variableName,
            String campaignId,
            String questionnaireId,
            String interrogationId,
            String expectedValue
    ) {
        List<LunaticJsonDataDocument> lunaticJsonDataDocuments = lunaticJsonRawDataPersistanceStub.getMongoStub()
                .stream().filter(
                        lunaticJsonDataDocument -> lunaticJsonDataDocument.campaignId().equals(campaignId)
                                && lunaticJsonDataDocument.questionnaireId().equals(questionnaireId)
                                && lunaticJsonDataDocument.interrogationId().equals(interrogationId)
                ).toList();

        Assertions.assertThat(lunaticJsonDataDocuments).hasSize(1);
        Assertions.assertThat(lunaticJsonDataDocuments.getFirst().data().externalVariables()).containsKey(variableName);

        LunaticJsonRawDataVariable lunaticJsonRawDataVariable =
                lunaticJsonDataDocuments.getFirst().data().externalVariables().get(variableName);

        Assertions.assertThat(lunaticJsonRawDataVariable.valuesArray()).isNull();
        Assertions.assertThat(lunaticJsonRawDataVariable.value()).isNotNull().isEqualTo(expectedValue);
    }

    @Then("We should have {int} status code")
    public void check_response_status_code(int expectedStatusCode){
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(expectedStatusCode);
    }

    @Then("For collected variable {string} in survey unit {string} we should have {string} for " +
            "iteration {int}")
    public void check_collected_variable_content_in_mongo(
            String collectedVariableName,
            String interrogationId,
            String expectedValue,
            Integer iteration
    ) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistencePortStub.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedCollectedVariables =
                surveyUnitModel.getCollectedVariables().stream().filter(variableModel ->
                        variableModel.varId().equals(collectedVariableName)
                                && variableModel.iteration().equals(iteration)
                ).toList();
        Assertions.assertThat(concernedCollectedVariables).hasSize(1);

        VariableModel variableModel = concernedCollectedVariables.getFirst();

        //Value content assertion
        Assertions.assertThat(variableModel.value()).isEqualTo(expectedValue);
    }

    @Then("For external variable {string} in survey unit {string} we should have {string} for " +
            "iteration {int}")
    public void check_external_variable_content_in_mongo(
            String externalVariableName,
            String interrogationId,
            String expectedValue,
            Integer iteration
    ) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistencePortStub.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedExternalVariables =
                surveyUnitModel.getExternalVariables().stream().filter(variableModel ->
                        variableModel.varId().equals(externalVariableName)
                                && variableModel.iteration().equals(iteration)
                ).toList();
        Assertions.assertThat(concernedExternalVariables).hasSize(1);

        VariableModel variableModel = concernedExternalVariables.getFirst();

        //Value content assertion
        Assertions.assertThat(variableModel.value()).isEqualTo(expectedValue);
    }*/
}
