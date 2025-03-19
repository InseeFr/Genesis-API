package fr.insee.genesis.controller.rest.responses;


class RawResponseControllerTest {
/*    private final FileUtils fileUtils = new FileUtils(new ConfigStub());
    private final LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    private final LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = new LunaticJsonRawDataService(lunaticJsonRawDataPersistanceStub);
    private final SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    private final RawResponseController rawResponseController = new RawResponseController(
            lunaticJsonRawDataApiPort,
            new ControllerUtils(fileUtils),
            new MetadataService(),
            new SurveyUnitService(surveyUnitPersistencePortStub),
            new SurveyUnitQualityService(),
            fileUtils
    );
    
    
    @Test
    void saveJsonRawDataFromStringTest(){
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "testIdQuest";
        String interrogationId = "testinterrogationId";

        //WHEN
        ResponseEntity<Object> response = rawResponseController.saveRawResponsesFromJsonBody(
                campaignId
                , questionnaireId
                , interrogationId
                , null
                , Mode.WEB
                , "{\"COLLECTED\": {\"testdata\": {\"COLLECTED\": [\"test\"]}}}"
        );

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).isNotEmpty().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isNotNull().isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isNotNull().isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isNotNull().isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().mode()).isEqualTo(Mode.WEB);

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get(
                "testdata")).isNotNull();
        Object value = lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get(
                "testdata").get(DataState.COLLECTED);
        Assertions.assertThat(value).isNotNull();
        Assertions.assertThat(value).isInstanceOf(List.class);
        List<?> list = (List<?>) value;
        Assertions.assertThat(list).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().recordDate()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().processDate()).isNull();
    }

    @Test
    void getUnprocessedDataTest(){
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST1";
        String interrogationId = "testinterrogationId1";
        addJsonRawDataDocumentToStub(campaignId, interrogationId, null);

        //WHEN
        List<LunaticJsonRawDataUnprocessedDto> dtos = rawResponseController.getUnproccessedJsonRawData().getBody();

        //THEN
        Assertions.assertThat(dtos).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(dtos.getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(dtos.getFirst().interrogationId()).isEqualTo(interrogationId);
    }

    @Test
    void getUnprocessedDataTest_processDate_present(){
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST2";
        String interrogationId = "testinterrogationId2";
        addJsonRawDataDocumentToStub(campaignId, interrogationId, LocalDateTime.now());

        //WHEN
        List<LunaticJsonRawDataUnprocessedDto> dtos = rawResponseController.getUnproccessedJsonRawData().getBody();

        //THEN
        Assertions.assertThat(dtos).isNotNull().isEmpty();
    }

    //raw data process
    //json
    @Test
    void processJsonRawDataTest() {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v2";
        String questionnaireId = campaignId + "_quest";
        String interrogationId = "testinterrogationId1";
        String varName = "AVIS_MAIL";
        String varValue = "TEST";
        addJsonRawDataDocumentToStub(campaignId, questionnaireId, interrogationId, null, varName, varValue);

        List<String> interrogationIdList = new ArrayList<>();
        interrogationIdList.add(interrogationId);

        //WHEN
        rawResponseController.processJsonRawData(campaignId, questionnaireId, interrogationIdList);


        //THEN
        //Genesis model survey unit created successfully
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getQuestionnaireId()).isNotNull().isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getMode()).isNotNull().isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getFileDate()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getRecordDate()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().varId()).isNotNull().isEqualTo(varName);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().value()).isNotNull().isEqualTo(varValue);

        //Process date check
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().processDate()).isNotNull();

        //Var/type check
        // TODO Enable when mapping problem solved for get metadatas step
        //Assertions.assertThat(variableTypePersistanceStub.getMongoStub()).isNotNull().isNotEmpty().hasSize(1);
        //Assertions.assertThat(variableTypePersistanceStub.getMongoStub().getFirst().getCampaignId()).isEqualTo
        // (campaignId);
        //Assertions.assertThat(variableTypePersistanceStub.getMongoStub().getFirst().getQuestionnaireId()).isEqualTo
        // (questionnaireId);
        //Assertions.assertThat(variableTypePersistanceStub.getMongoStub().getFirst().getMode()).isEqualTo(Mode.WEB);
        //Assertions.assertThat(variableTypePersistanceStub.getMongoStub().getFirst().getVariablesMap()).isNotNull();
        //Assertions.assertThat(variableTypePersistanceStub.getMongoStub().getFirst().getVariablesMap().getVariables
        // ()).isNotNull().isNotEmpty().containsKey(varName);
        //Assertions.assertThat(variableTypePersistanceStub.getMongoStub().getFirst().getVariablesMap().getVariables
        // ().get(varName).getType()).isEqualTo(VariableType.STRING);
    }


    //Utils
    private void addJsonRawDataDocumentToStub(String campaignId, String interrogationId,
                                                     LocalDateTime processDate) {
        LunaticJsonDataDocument lunaticJsonDataDocument = LunaticJsonDataDocument.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .interrogationId(interrogationId)
                .recordDate(LocalDateTime.now())
                .processDate(processDate)
                .build();

        lunaticJsonRawDataPersistanceStub.getMongoStub().add(lunaticJsonDataDocument);
    }
    private void addJsonRawDataDocumentToStub(String campaignId, String questionnaireId, String interrogationId,
                                                     LocalDateTime processDate,
                                                     String variableName, String variableValue) {
        //COLLECTED
        LunaticJsonRawDataDocument lunaticJsonRawDataDoc = LunaticJsonRawDataDocument.builder()
                .collectedVariables(new HashMap<>())
                .externalVariables(new HashMap<>())
                .build();

        lunaticJsonRawDataDoc.collectedVariables().put(variableName,new HashMap<>());
        lunaticJsonRawDataDoc.collectedVariables().get(variableName).put(DataState.COLLECTED,variableValue);

        //EXTERNAL
        lunaticJsonRawDataDoc.externalVariables().put(variableName+"_EXTERNAL",variableValue+"_EXTERNAL");

        LunaticJsonDataDocument lunaticJsonDataDocument = LunaticJsonDataDocument.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .mode(Mode.WEB)
                .interrogationId(interrogationId)
                .recordDate(LocalDateTime.now())
                .processDate(processDate)
                .data(lunaticJsonRawDataDoc)
                .build();

        lunaticJsonRawDataPersistanceStub.getMongoStub().add(lunaticJsonDataDocument);
    }*/
}
