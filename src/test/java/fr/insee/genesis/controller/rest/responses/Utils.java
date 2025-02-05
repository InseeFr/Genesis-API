package fr.insee.genesis.controller.rest.responses;

import cucumber.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


class Utils {

    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

    static void reset(SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) throws IOException {
        //MongoDB stub management
        surveyUnitPersistencePortStub.getMongoStub().clear();

        addAdditionalDtoToMongoStub("TESTIDCAMPAIGN", defaultIdQuest ,
                LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                surveyUnitPersistencePortStub);


        //Test file management
        //Clean DONE folder
        Path testResourcesPath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY);
        if (testResourcesPath.resolve("DONE").toFile().exists())
            Files.walk(testResourcesPath.resolve("DONE"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        //Recreate data files
        //SAMPLETEST-PARADATA-v1
        //Root
        //Partial
        Path source = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("reponse-platine")
                .resolve("data.complete.partial.STPDv1.20231122164209.xml");
        Path dest = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("data.complete.partial.STPDv1.20231122164209.xml");
        if (!dest.toFile().exists()) {
            Files.copy(source, dest);
        }

        //Validated
        source = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("reponse-platine")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml");
        dest = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml");
        if (!dest.toFile().exists()) {
            Files.copy(source, dest);
        }

        //Differential data
        //Partial
        source = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("reponse-platine")
                .resolve("data.complete.partial.STPDv1.20231122164209.xml");
        dest = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("differential")
                .resolve("data")
                .resolve("data.complete.partial.STPDv1.20231122164209.xml");
        if (!dest.toFile().exists()) {
            Files.copy(source, dest);
        }

        source = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("reponse-platine")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml");
        dest = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("differential")
                .resolve("data")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml");
        if (!dest.toFile().exists()) {
            Files.copy(source, dest);
        }

        //SAMPLETEST-PARADATA-v2
        //Partial
        source = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v2")
                .resolve("reponse-platine")
                .resolve("data.complete.partial.STPDv2.20231122164209.xml");
        dest = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v2")
                .resolve("data.complete.partial.STPDv2.20231122164209.xml");
        if (!dest.toFile().exists()) {
            Files.copy(source, dest);
        }

        //Validated
        source = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v2")
                .resolve("reponse-platine")
                .resolve("data.complete.validated.STPDv2.20231122164209.xml");
        dest = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v2")
                .resolve("data.complete.validated.STPDv2.20231122164209.xml");
        if (!dest.toFile().exists()) {
            Files.copy(source, dest);
        }

        //SAMPLETEST-NO-COLLECTED
        source = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-NO-COLLECTED")
                .resolve("data_diff_no_collected.xml");

        dest = testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-NO-COLLECTED")
                .resolve("differential")
                .resolve("data")
                .resolve("data_diff_no_collected.xml");
        if (!dest.toFile().exists()) {
            Files.copy(source, dest);
        }
    }

    static void addAdditionalDtoToMongoStub(SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        addAdditionalDtoToMongoStub(defaultIdQuest, surveyUnitPersistencePortStub);
    }

    static void addAdditionalDtoToMongoStub(String idQuestionnaire, SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        addAdditionalDtoToMongoStub("TESTIDCAMPAIGN",idQuestionnaire, surveyUnitPersistencePortStub);
    }

    static void addAdditionalDtoToMongoStub(String idCampaign, String idQuestionnaire, SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        addAdditionalDtoToMongoStub(idCampaign,idQuestionnaire,
                LocalDateTime.of(2023, 2, 2, 0, 0, 0),
                LocalDateTime.of(2024, 2, 2, 0, 0, 0),
                surveyUnitPersistencePortStub);
    }

    static void addAdditionalDtoToMongoStub(String idCampaign, String idQuestionnaire,
                                             LocalDateTime fileDate, LocalDateTime recordDate,
                                             SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel externalVariable = VariableModel.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(externalVariable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"}))
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .build();


        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId(idCampaign)
                .mode(Mode.WEB)
                .interrogationId(defaultIdUE)
                .questionnaireId(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }



    static void addAdditionalDtoToMongoStub(DataState state,
                                            String collectedVariableValue,
                                            String externalVariableValue,
                                            LocalDateTime fileDate,
                                            LocalDateTime recordDate,
                                            SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel variable = VariableModel.builder().varId("TESTIDVAR").values(List.of(new String[]{externalVariableValue})).build();
        externalVariableList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{collectedVariableValue}))
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .build();

        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .interrogationId(defaultIdUE)
                .questionnaireId(defaultIdQuest)
                .state(state)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

}
